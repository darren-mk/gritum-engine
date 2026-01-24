(ns gritum.engine.domain.rules.ten-percent-tolerance-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [gritum.engine.domain.rules.ten-percent-tolerance :as sut]))

(defn- make-test-fee [section category amount]
  {:id (str (name section) "_" (name category))
   :section section
   :category category
   :label "fake-label"
   :payee {:name "fake-lender" :kind :lender}
   :payments [{:amount amount :payer :buyer :timing :at-closing}]})

(deftest ten-percent-item-selection-test
  (testing "Selection logic for 10% tolerance items"
    (is (sut/ten-percent-item? (make-test-fee :services-shop :title-search 100.0))
        "Any item in Section C should be included")
    (is (sut/ten-percent-item? (make-test-fee :taxes :recording-fee-for-deed 50.0))
        "Recording fees in Section E should be included")
    (is (not (sut/ten-percent-item? (make-test-fee :origination-charges :admin-fee 500.0)))
        "Section A items are 0% tolerance, not 10%")))

(deftest aggregate-total-calculation-test
  (testing "Total aggregation of buyer-paid 10% items"
    (let [fees [(make-test-fee :services-shop :title-search 500.0)
                (make-test-fee :taxes :recording-fee-for-deed 100.0)
                (make-test-fee :origination-charges :points 1000.0)]] ; Should be ignored
      (is (= 600.0 (sut/aggregate-total fees)) "Should only sum Section C and Recording fees"))))

(deftest calculate-cure-test
  (testing "Cure calculation for 10% aggregate rule"
    (let [le-fees [(make-test-fee :services-shop :title-search 1000.0)] ; Total LE: 1000
          cd-fees-safe [(make-test-fee :services-shop :title-search 1050.0)] ; 105% (Safe)
          cd-fees-violated [(make-test-fee :services-shop :title-search 1150.0)]] ; 115% (Violation)
      (testing "Scenario: Within 10% threshold"
        (let [result (sut/calculate-cure le-fees cd-fees-safe)]
          (is (not (:violated? result)))
          (is (= 0.0 (:cure-amount result)))))
      (testing "Scenario: Exceeds 10% threshold"
        (let [result (sut/calculate-cure le-fees cd-fees-violated)]
          (is (:violated? result))
          ;; Threshold is 1100.0. CD is 1150.0. Cure = 50.0
          (is (= 50.0 (:cure-amount result))))))))
