(ns gritum.engine.domain.rules.zero-percent-tolerance
  "Defines items subject to the 0% tolerance rule.
   These items cannot increase from LE to CD at all."
  (:require
   [gritum.engine.domain.model :as dom]))

(def ^:private zero-percent-sections
  #{:origination-charges :services-not-shop})

(defn zero-percent-item?
  "Returns true if the fee is subject to the 0% tolerance rule."
  {:malli/schema [:=> [:cat dom/Fee] :boolean]}
  [{:keys [section category]}]
  (or (contains? zero-percent-sections section)
      ;; Special case for Section E
      (and (= section :taxes)
           (= category :transfer-taxes))))

(defn- sum-buyer-payments
  "Extracts the total amount paid by the buyer for a specific fee."
  {:malli/schema [:=> [:cat dom/Fee] dom/Money]}
  [fee]
  (->> (:payments fee)
       (filter #(= :buyer (:payer %)))
       (map :amount)
       (reduce + 0.0)))

(defn calculate-item-violation
  "Compares a single LE fee against a CD fee and returns the cure amount if violated."
  {:malli/schema [:=> [:cat dom/Fee dom/Fee] dom/Money]}
  [le-fee cd-fee]
  (let [le-amt (sum-buyer-payments le-fee)
        cd-amt (sum-buyer-payments cd-fee)]
    (max 0.0 (- cd-amt le-amt))))

(defn calculate-total-cure
  "Calculates the total cure amount by matching LE and CD fees by ID."
  {:malli/schema [:=> [:cat dom/Fees dom/Fees]
                  [:map
                   [:total-cure dom/Money]
                   [:violations [:vector [:map
                                          [:id :string]
                                          [:cure dom/Money]]]]]]}
  [le-fees cd-fees]
  (let [le-map (into {} (map (juxt :id identity) le-fees))
        ;; Only check items that are subject to 0% tolerance
        violations (->> cd-fees
                        (filter zero-percent-item?)
                        (keep (fn [cd-fee]
                                (when-let [le-fee (get le-map (:id cd-fee))]
                                  (let [cure (calculate-item-violation le-fee cd-fee)]
                                    (when (> cure 0.0)
                                      {:id (:id cd-fee) :cure cure})))))
                        vec)]
    {:total-cure (reduce + 0.0 (map :cure violations))
     :violations violations}))
