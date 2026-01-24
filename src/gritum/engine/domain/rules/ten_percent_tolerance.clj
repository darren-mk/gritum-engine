(ns gritum.engine.domain.rules.ten-percent-tolerance
  "Define items subject to the 10% aggregate tolerance rule.
  Includes all of Section C and specific Recording Fees from Section E. "
  (:require
   [gritum.engine.domain.model :as dom]))

(def ^:private ten-percent-sections
  #{:services-shop})

(def ^:private recording-fee-categories
  #{:recording-fee-for-deed
    :recording-fee-for-mortgage})

(defn ten-percent-item?
  "Returns true if the fee is subject to the 10% aggregate tolerance rule."
  {:malli/schema [:=> [:cat dom/Fee] :boolean]}
  [{:keys [section category]}]
  (or (contains? ten-percent-sections section)
      (and (= section :taxes)
           (contains? recording-fee-categories category))))

(defn- sum-buyer-payments
  "Sums only the amounts where the payer is :buyer."
  {:malli/schema [:=> [:cat dom/Fee] dom/Money]}
  [fee]
  (->> (:payments fee)
       (filter #(= :buyer (:payer %)))
       (map :amount)
       (reduce + 0.0)))

(defn aggregate-total
  "Calculates the total aggregate amount for 10% target fees."
  {:malli/schema [:=> [:cat dom/Fees] dom/Money]}
  [fees]
  (->> fees
       (filter ten-percent-item?)
       (map sum-buyer-payments)
       (reduce + 0.0)))

(defn calculate-cure
  "Compares LE vs CD aggregate totals and calculates the required cure amount."
  {:malli/schema [:=> [:cat dom/Fees dom/Fees] :map]}
  [le-fees cd-fees]
  (let [le-total (aggregate-total le-fees)
        cd-total (aggregate-total cd-fees)
        threshold (* le-total 1.10)
        cure (max 0.0 (- cd-total threshold))]
    {:le-total le-total
     :cd-total cd-total
     :threshold threshold
     :cure-amount cure
     :violated? (> cure 0.0)}))
