(ns gritum.rules.zero-percent-tolerance-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.rules.zero-percent-tolerance :as sut]))

(defn- make-fee [id section category amount]
  {:id id
   :section section
   :category category
   :label "Test Fee"
   :payee {:name "Test Payee" :kind :lender}
   :payments [{:amount amount :payer :buyer :timing :at-closing}]})

(deftest zero-percent-item-selection-test
  (testing "Identity logic for 0% tolerance items"
    (is (sut/zero-percent-item?
         (make-fee "1" :origination-charges :processing-fee 500.0))
        "Section A is always 0% tolerance")
    (is (sut/zero-percent-item?
         (make-fee "2" :services-not-shop :appraisal-fee 400.0))
        "Section B is always 0% tolerance")
    (is (sut/zero-percent-item?
         (make-fee "3" :taxes :transfer-taxes 1000.0))
        "Transfer taxes in Section E are 0% tolerance")
    (is (not (sut/zero-percent-item?
              (make-fee "4" :taxes :recording-fee 50.0)))
        "Recording fees are 10% tolerance, not 0%")))

(deftest calculate-total-cure-test
  (testing "Cumulative cure calculation for matched items"
    (let [le-fees [(make-fee "fee-1" :origination-charges :admin-fee 500.0)
                   (make-fee "fee-2" :services-not-shop :credit-report 50.0)
                   (make-fee "fee-3" :taxes :transfer-taxes 1000.0)]
          ;; fee-1: increased (violation), fee-2: same (safe), fee-3: decreased (safe)
          cd-fees [(make-fee "fee-1" :origination-charges :admin-fee 550.0) ; +$50
                   (make-fee "fee-2" :services-not-shop :credit-report 50.0)  ; $0
                   (make-fee "fee-3" :taxes :transfer-taxes 900.0)]
          result (sut/calculate-total-cure le-fees cd-fees)]
      (is (= 50.0 (:total-cure result)) "Total cure should only sum the increases")
      (is (= 1 (count (:violations result))) "Only one item should be flagged as a violation")
      (is (= "fee-1" (:id (first (:violations result))))))))

(deftest edge-cases-test
  (testing "Handling of missing items in LE"
    (let [le-fees []
          cd-fees [(make-fee "new-fee" :origination-charges :underwriting 800.0)]
          result (sut/calculate-total-cure le-fees cd-fees)]
      (is (= 0.0 (:total-cure result))
          "New items in CD are usually handled by other rules or ignored here")))

  (testing "Split payments with non-buyer payers"
    (let [le-fee (make-fee "id1" :origination-charges :fee 100.0)
          ;; CD has $100 Buyer + $50 Seller. Total is $150, but Buyer portion is same.
          cd-fee (assoc le-fee :payments [{:amount 100.0 :payer :buyer :timing :at-closing}
                                          {:amount 50.0 :payer :seller :timing :at-closing}])]
      (is (= 0.0 (sut/calculate-item-violation le-fee cd-fee))
          "Seller-paid increases should not trigger a cure for the borrower"))))
