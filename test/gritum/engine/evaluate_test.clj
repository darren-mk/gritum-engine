(ns gritum.engine.evaluate-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.domain.model :as domain]
   [gritum.engine.domain.evaluate :as sut]
   [gritum.engine.test-helper :as h]))

(defn- mock-fee
  "Generates a mock FEE node for testing."
  [section category amount _id]
  {:tag :FEE
   :content [{:tag :FEE_DETAIL
              :content [{:tag :IntegratedDisclosureSectionType :content [(name section)]}
                        {:tag :EXTENSION
                         :content [{:tag :OTHER
                                    :content [{:tag :ucd:FEE_DETAIL_EXTENSION
                                               :content [{:tag :ucd:FeeItemType
                                                          :attrs {:DisplayLabelText "Test"}
                                                          :content [(name category)]}]}]}]}]}
             {:tag :FEE_PAYMENTS
              :content [{:tag :FEE_PAYMENT
                         :content [{:tag :FeeActualPaymentAmount :content [(str amount)]}
                                   {:tag :FeePaymentPaidByType :content ["Buyer"]}]}]}]})

(deftest evaluate-perform-test
  (testing "Combined evaluation of 0% and 10% tolerance rules"
    (let [le-fees [(mock-fee :origination-charges :admin-fee 500.0 "a")
                   (mock-fee :services-shop :title-search 1000.0 "b")]
          le-xml  (h/populate-xml le-fees domain/path-to-fees)
          ;; CD: 0% increases by 50 (Cure 50), 10% increases by 150 (Total 1150)
          ;; 10% Rule: 1000 * 1.1 = 1100 threshold. 1150 - 1100 = 50 cure.
          ;; Expected Total Cure = 50 + 50 = 100.
          cd-fees [(mock-fee :origination-charges :admin-fee 550.0 "a")
                   (mock-fee :services-shop :title-search 1150.0 "b")]
          cd-xml  (h/populate-xml cd-fees domain/path-to-fees)
          result (sut/perform le-xml cd-xml)]
      (is (= 100.0 (:total-cure result))
          "Total cure should sum both 0% and 10% violations")
      (is (false? (:valid? result))
          "Result should be invalid when cure is required")
      (is (contains? (:breakdown result) :zero-percent))
      (is (contains? (:breakdown result) :ten-percent)))))

(deftest missing-fees-path-test
  (testing "Robustness when the FEES path is missing in XML"
    (let [empty-xml {:tag :MESSAGE :content []}
          result (sut/perform empty-xml empty-xml)]
      (is (= 0.0 (:total-cure result)))
      (is (true? (:valid? result)))
      (is (empty? (get-in result [:breakdown :zero-percent :violations]))))))
