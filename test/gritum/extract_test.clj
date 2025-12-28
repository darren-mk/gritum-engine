(ns gritum.extract-test
  (:require
   [clojure.test :as t]
   [gritum.extract :as sut]
   [gritum.test-helper :as h]))

(t/deftest jump-map-test
  (let [sample {:tag :A :content ["val"]}]
    (t/testing "returns content when tag matches"
      (t/is (= ["val"] (sut/jump-map sample :A))))
    (t/testing "returns nil when tag does not match"
      (t/is (nil? (sut/jump-map sample :B))))))

(t/deftest jump-vec-test
  (let [sample [{:tag :A :content [1]}
                {:tag :B :content [2]}]]
    (t/testing "returns content when exactly one matching tag exists in the list"
      (t/is (= [1] (sut/jump-vec sample :A))))
    (t/testing "throws exception if tag is missing or duplicated"
      (t/is (nil? (sut/jump-vec sample :C)))
      (let [dups [{:tag :A} {:tag :A}]]
        (t/is (thrown? clojure.lang.ExceptionInfo (sut/jump-vec dups :A)))))))

(t/deftest jump-test
  (t/testing "dispatches to appropriate logic based on input type (Map/Vector)"
    (t/is (= ["val"] (sut/jump {:tag :root :content ["val"]} :root)))
    (t/is (= ["val"] (sut/jump [{:tag :child :content ["val"]}] :child)))))

(t/deftest traverse-test
  (let [sample {:tag :root
                :content [{:tag :mid
                           :content [{:tag :leaf :content ["END"]}]}]}]
    (t/testing "traverses deep down the path and returns the final content"
      (t/is (= ["END"]
               (sut/traverse sample [:root :mid :leaf]))))))

(t/deftest ->edn-test
  (t/testing "unwrap sequence"
    (let [single-xml [{:tag :FEE :content ["FeeA"]}]
          multi-xml  [{:tag :FEE :content ["FeeA"]}
                      {:tag :FEE :content ["FeeB"]}]]
      (t/testing "vector with single item returns a vector"
        (let [result (sut/->edn single-xml)]
          (t/is (vector? result))
          (t/is (= [{:FEE "FeeA"}] result))))
      (t/testing "vector with multiple item returns a vector"
        (let [result (sut/->edn multi-xml)]
          (t/is (vector? result))
          (t/is (= [{:FEE "FeeA"} {:FEE "FeeB"}] result))))))
  (t/testing "1. Leaf Node Handling (Values)"
    (t/testing "Strings are unwrapped from vector"
      (let [input ["Lender"]]
        (t/is (= "Lender" (sut/->edn input)))))
    (t/testing "Booleans are parsed and unwrapped"
      (t/is (= true (sut/->edn ["true"])))
      (t/is (= false (sut/->edn ["false"])))))
  (t/testing "2. Single Item Consistency (The Main Fix)"
    (t/testing "Single structural node is KEPT as a Vector (unlike before)"
      (let [input [{:tag :LOAN :content ["Data"]}]]
        ;; Your new logic: It has a :tag, so it stays in a vector.
        (t/is (= [{:LOAN "Data"}] (sut/->edn input)))
        (t/is (vector? (sut/->edn input)))))
    (t/testing "Single leaf node is UNWRAPPED"
      (let [input [{:tag :Amount :content ["100"]}]]
        ;; Inside Amount, content is ["100"].
        ;; "100" has no tag, so it unwrap to "100".
        ;; Result: {:Amount "100"} wrapped in vector?
        ;; No, wait. ->edn on the ROOT returns the vector.
        (t/is (= [{:Amount "100"}] (sut/->edn input))))))
  (t/testing "3. Multiple Items Handling"
    (t/testing "Multiple same-tag nodes remain a Vector"
      (let [input [{:tag :FEE :content ["A"]}
                   {:tag :FEE :content ["B"]}]]
        (t/is (= [{:FEE "A"} {:FEE "B"}] (sut/->edn input)))))
    (t/testing "Multiple unique-tag nodes become a Map (siblings)"
      (let [input [{:tag :Name :content ["John"]}
                   {:tag :Age  :content ["30"]}]]
        ;; (all-unique?) branch triggers here
        (t/is (= {:Name "John", :Age "30"} (sut/->edn input))))))
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
               (sut/->edn input)))
      ;; Note for Developer:
      ;; Because of the new logic, even unique children like :FEE_DETAIL
      ;; are now returned as Vectors of 1 item, not Maps.
      (t/is (vector?
             (-> [{:tag :Container :content
                   [{:tag :Child :content ["Val"]}]}]
                 sut/->edn first :Container))))))

(t/deftest ucd-xml-tests
  (let [xml (h/load-xml "data/Purchase ARM UCD v2.0.xml")
        path [:MESSAGE :DOCUMENT_SETS :DOCUMENT_SET :DOCUMENTS
              :DOCUMENT :DEAL_SETS :DEAL_SET :DEALS :DEAL :LOANS
              :LOAN :FEE_INFORMATION :FEES]
        fee-xmls (sut/traverse xml path)]
    (t/is (= 24 (count fee-xmls)))))
