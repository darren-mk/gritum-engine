(ns gritum.core-test
  (:require
   [clojure.edn :as edn]
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
   [clojure.test :as t]
   [gritum.core :as sut]))

(defn load-edn [filename]
   (if-let [resource-url (io/resource filename)]
     (-> resource-url slurp edn/read-string)
     (throw (Exception. (str "Resource not found: " filename)))))

(defn load-xml [filename]
  (if-let [resource-url (io/resource filename)]
    (-> resource-url slurp xml/parse-str)
    (throw (Exception. (str "Resource not found: " filename)))))

#_#_#_
(def sample-fee-xml
  {:tag :FEE :attrs {}
   :content
   [{:tag :FEE_DETAIL :attrs {}
     :content
     [{:tag :FeePaidToType :attrs {} :content ["Lender"]}
      {:tag :FeeTotalPercent :attrs {} :content ["0.2500"]}
      {:tag :IntegratedDisclosureSectionType
       :attrs {} :content ["OriginationCharges"]}
      {:tag :RegulationZPointsAndFeesIndicator
       :attrs {} :content ["true"]}
      {:tag :EXTENSION :attrs {}
       :content
       [{:tag :OTHER :attrs {}
         :content [{:tag :FEE_DETAIL_EXTENSION :attrs {}
                    :content
                    [{:tag :FeeItemType :attrs {}
                      :content ["LoanDiscountPoints"]}]}]}]}]}
    {:tag :FEE_PAYMENTS :attrs {}
     :content
     [{:tag :FEE_PAYMENT :attrs {}
       :content
       [{:tag :FeeActualPaymentAmount :attrs {} :content ["360.00"]}
        {:tag :FeePaymentPaidByType :attrs {} :content ["Buyer"]}
        {:tag :FeePaymentPaidOutsideOfClosingIndicator :attrs {}
         :content ["false"]}]}]}]})

(t/deftest normalize-test
  (t/testing "fee"
    (t/is (= #:fee{:type "LoanDiscountPoints",
                   :amount 360.0,
                   :tolerance 0.0
                   :section "OriginationCharges",
                   :in-apr? true
                   :paid-by "Buyer"}
             (sut/normalize sample-fee-xml :fee)))))

(t/deftest xml->edn-test
  (t/testing "unwrap sequence"
    (let [single-xml [{:tag :FEE :content ["FeeA"]}]
          multi-xml  [{:tag :FEE :content ["FeeA"]}
                      {:tag :FEE :content ["FeeB"]}]]
      (t/testing "vector with single item returns a vector"
        (let [result (sut/xml->edn single-xml)]
          (t/is (vector? result))
          (t/is (= [{:FEE "FeeA"}] result))))
      (t/testing "vector with multiple item returns a vector"
        (let [result (sut/xml->edn multi-xml)]
          (t/is (vector? result))
          (t/is (= [{:FEE "FeeA"} {:FEE "FeeB"}] result))))))
  (t/testing "1. Leaf Node Handling (Values)"
    (t/testing "Strings are unwrapped from vector"
      (let [input ["Lender"]]
        (t/is (= "Lender" (sut/xml->edn input)))))
    (t/testing "Booleans are parsed and unwrapped"
      (t/is (= true (sut/xml->edn ["true"])))
      (t/is (= false (sut/xml->edn ["false"])))))
  (t/testing "2. Single Item Consistency (The Main Fix)"
    (t/testing "Single structural node is KEPT as a Vector (unlike before)"
      (let [input [{:tag :LOAN :content ["Data"]}]]
        ;; Your new logic: It has a :tag, so it stays in a vector.
        (t/is (= [{:LOAN "Data"}] (sut/xml->edn input)))
        (t/is (vector? (sut/xml->edn input)))))
    (t/testing "Single leaf node is UNWRAPPED"
      (let [input [{:tag :Amount :content ["100"]}]]
        ;; Inside Amount, content is ["100"].
        ;; "100" has no tag, so it unwrap to "100".
        ;; Result: {:Amount "100"} wrapped in vector?
        ;; No, wait. xml->edn on the ROOT returns the vector.
        (t/is (= [{:Amount "100"}] (sut/xml->edn input))))))
  (t/testing "3. Multiple Items Handling"
    (t/testing "Multiple same-tag nodes remain a Vector"
      (let [input [{:tag :FEE :content ["A"]}
                   {:tag :FEE :content ["B"]}]]
        (t/is (= [{:FEE "A"} {:FEE "B"}] (sut/xml->edn input)))))
    (t/testing "Multiple unique-tag nodes become a Map (siblings)"
      (let [input [{:tag :Name :content ["John"]}
                   {:tag :Age  :content ["30"]}]]
        ;; (all-unique?) branch triggers here
        (t/is (= {:Name "John", :Age "30"} (sut/xml->edn input))))))
  (t/testing "4. Mixed/Nested Integration Test (FEE Data)"
    ;; This tests the behavior of nested children
    (let [input {:tag :FEE
                 :content [{:tag :FEE_DETAIL
                            :content [{:tag :Type :content ["Origination"]}]}]}]
      ;; Logic Trace:
      ;; 1. Root :FEE -> Map Key
      ;; 2. Content is [{:tag :FEE_DETAIL...}] (Count 1)
      ;; 3. Item has :tag, so it becomes a VECTOR: [{:FEE_DETAIL ...}]
      ;; 4. Inside FEE_DETAIL, Content is [{:tag :Type...}] (Count 1)
      ;; 5. Item has :tag, so it becomes a VECTOR: [{:Type ...}]
      ;; 6. Inside Type, Content is ["Origination"] (Count 1)
      ;; 7. Item is String (no tag), so it UNWRAPS: "Origination"
      (t/is (= {:FEE [{:FEE_DETAIL [{:Type "Origination"}]}]}
               (sut/xml->edn input)))
      ;; Note for Developer:
      ;; Because of the new logic, even unique children like :FEE_DETAIL
      ;; are now returned as Vectors of 1 item, not Maps.
      (t/is (vector?
             (-> [{:tag :Container :content
                   [{:tag :Child :content ["Val"]}]}]
                 sut/xml->edn first :Container))))))
#_
(t/deftest domain-test
  (t/testing "purchase fixed ucd 2.0"
    (let [folder-base "data/UCD v2.0 Sample XML Files"
          filename "Purchase Fixed UCD v2.0."]
      (t/is (= (load-edn (str folder-base " Extraction/" filename "edn"))
               (->> (str folder-base "/" filename "xml") load-xml sut/->domain))))))
