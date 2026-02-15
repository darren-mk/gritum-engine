(ns gritum.engine.domain.rules-test
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [gritum.engine.domain.model :refer [Costs]]
   [gritum.engine.domain.rules :as sut]
   [malli.core :as m]
   [user :refer [in]]))

(use-fixtures :once
  (fn [f] (in) (f)))

(def sample-cost-1
  {:section :a
   :category :application-fee
   :amount 10.0
   :side :le
   :description "foo"})

(def sample-cost-2
  {:section :a
   :category :application-fee
   :amount 20.0
   :side :cd
   :description "foo"})

(def sample-cost-3
  {:section :b
   :category :underwriting-fee
   :amount 30.0
   :side :le
   :description "foo"})

(def sample-cost-4
  {:section :b
   :category :underwriting-fee
   :amount 40.0
   :side :cd
   :description "foo"})

(def sample-cost-5
  {:section :b
   :category :underwriting-fee
   :amount 10.0
   :side :le
   :description "foo"})

(def sample-costs
  (m/coerce Costs
            [sample-cost-1
             sample-cost-2
             sample-cost-3
             sample-cost-4]))

(deftest filter-by-test
  (testing "filters by side and section"
    (is (= [sample-cost-1]
           (sut/filter-by :le :a sample-costs)))
    (is (= [sample-cost-4]
           (sut/filter-by :cd :b sample-costs))))
  (testing "returns empty when no match"
    (is (empty? (sut/filter-by :le :c sample-costs)))
    (is (empty? (sut/filter-by :cd :c sample-costs))))
  (testing "handles empty input"
    (is (empty? (sut/filter-by :le :a [])))))

(deftest condense-test
  (testing "accumulates amounts by category"
    (let [costs [sample-cost-1
                 sample-cost-3
                 sample-cost-5]]
      (is (= {:application-fee 10.0 :underwriting-fee 40.0}
             (reduce sut/condense {} costs))))))

(deftest rule-zero-tolerance-test
  (testing "identifies zero tolerance violations"
    (is (= [{:rule :zero-tolerance
             :category :application-fee
             :le-amount 10.0
             :cd-amount 20.0
             :related-costs [sample-cost-1 sample-cost-2]}]
           (sut/rule-zero-tolerance sample-costs)))))

(deftest pipeline-test
  (testing "applies all rules"
    (is (= [{:rule :zero-tolerance
             :category :application-fee
             :le-amount 10.0
             :cd-amount 20.0
             :related-costs [sample-cost-1 sample-cost-2]}]
           (sut/pipeline sample-costs)))))