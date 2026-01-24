(ns gritum.engine.domain-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.domain.model :as sut]
   [gritum.engine.test-helper :as h]
   [malli.core :as m]
   [malli.generator :as mg]))

(defn- mock-fee-xml
  "Generates a complete FEE XML structure supporting multiple payments."
  [{:keys [section fee-type label payments payee-name payee-kind]
    :or {label "Default Fee Label"
         payee-name "Default Payee Inc."
         payee-kind "Lender"
         payments [{:amount 500.0 :paid-by "Buyer" :timing :at-closing}]}}]
  {:tag :FEE
   :content
   [{:tag :FEE_DETAIL
     :content [{:tag :IntegratedDisclosureSectionType :content [section]}
               {:tag :FeePaidToType :content [payee-kind]}
               {:tag :EXTENSION
                :content [{:tag :OTHER
                           :content [{:tag :ucd:FEE_DETAIL_EXTENSION
                                      :content [{:tag :ucd:FeeItemType
                                                 :attrs {:DisplayLabelText label}
                                                 :content [fee-type]}
                                                {:tag :ucd:FeePaidToEntityName :content [payee-name]}]}]}]}]}
    {:tag :FEE_PAYMENTS
     :content (map (fn [{:keys [amount paid-by timing]}]
                     {:tag :FEE_PAYMENT
                      :content [{:tag :FeeActualPaymentAmount :content [(str amount)]}
                                {:tag :FeePaymentPaidByType :content [paid-by]}
                                {:tag :FeePaymentPaidOutsideOfClosingIndicator
                                 :content [(if (= :before-closing timing) "true" "false")]}]})
                   payments)}]})

(deftest fee-plural-payments-test
  (testing "Extraction of multiple payments within a single fee"
    (let [payments [{:amount 1000.0 :paid-by "Buyer"
                     :timing :at-closing}
                    {:amount 250.0  :paid-by "Seller"
                     :timing :at-closing}]
          xml (mock-fee-xml {:section "ServicesBorrowerDidShopFor"
                             :fee-type "TitleSearch"
                             :payments payments})
          result (sut/->fee xml)]
      (is (= 2 (count (:payments result)))
          "Should capture both payment entries")
      (testing "Individual payment details"
        (let [p1 (first (:payments result))
              p2 (second (:payments result))]
          (is (= 1000.0 (:amount p1)))
          (is (= :buyer (:payer p1)))
          (is (= 250.0 (:amount p2)))
          (is (= :seller (:payer p2))))))))

(deftest id-and-label-test
  (testing "ID generation and label fallback"
    (let [xml (mock-fee-xml {:section "OriginationCharges"
                             :fee-type "Points"
                             :label "Discount Points"})
          {:keys [id label]} (sut/->fee xml)]
      (is (= "origination-charges_points" id))
      (is (= "Discount Points" label)))))

(deftest timing-detection-test
  (testing "POC (Paid Outside of Closing) mapping"
    (let [xml (mock-fee-xml {:payments [{:amount 100.0 :paid-by "Buyer"
                                         :timing :before-closing}]})
          result (sut/->fee xml)]
      (is (= :before-closing (:timing (first (:payments result))))))))

(deftest payee-extraction-test
  (testing "Payee entity and kind mapping"
    (let [xml (mock-fee-xml {:payee-name "Fast Title LLC"
                             :payee-kind "ThirdParty"})
          result (sut/->fee xml)]
      (is (= "Fast Title LLC" (get-in result [:payee :name])))
      (is (= :seller (get-in result [:payee :kind]))
          "ThirdParty maps to :seller per case logic"))))

(deftest generative-schema-validation
  (testing "Output must always satisfy the Malli Fee schema"
    (dotimes [_ 50]
      (let [random-fee (mg/generate sut/Fee)
            mock-xml (mock-fee-xml
                      {:section "OriginationCharges"
                       :fee-type "ApplicationFee"
                       :payments (mapv (fn [p]
                                         {:amount (:amount p)
                                          :paid-by "Buyer"
                                          :timing (:timing p)})
                                       (:payments random-fee))})
            result (sut/->fee mock-xml)]
        (is (m/validate sut/Fee result))))))

(deftest ucd-xml-extract-fees-test
  (testing "Integration test with real MISMO/UCD sample file"
    (let [xml (h/load-xml "data/Purchase ARM UCD v2.0.xml")
          fees (#'sut/extract-fees xml)]
      (is (= 24 (count fees))
          "should extract exactly 24 fee nodes"))))

(deftest ucd-xml-extract-lender-credit-test
  (testing "Integration test with real MISMO/UCD sample file"
    (let [xml (h/load-xml "data/Refinance Fixed UCD v2.0.xml")
          lender-credit (#'sut/extract-lender-credit xml)]
      (is (= {:amount 0.0,
              :cure-amount 1100.0,
              :section-type :total-closing-costs,
              :subsection-type :lender-credits}
             lender-credit)))))

(deftest ucd-xml->document-test
  (testing "Integration test with real MISMO/UCD sample file"
    (let [xml (h/load-xml "data/Purchase ARM UCD v2.0.xml")
          {:keys [fees lender-credit]} (#'sut/->document xml)]
      (is (= 24 (count fees)))
      (is (map? lender-credit)))))
