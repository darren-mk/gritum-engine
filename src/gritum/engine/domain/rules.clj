(ns gritum.engine.domain.rules
  (:require
   [gritum.engine.domain.model
    :refer [Cost Side Section Violation]]))

(defn filter-by
  {:malli/schema [:=> [:cat Side Section [:vector Cost]]
                  [:vector Cost]]}
  [side section costs]
  (filterv #(and (= side (:side %))
                 (= section (:section %)))
           costs))

(defn condense
  {:malli/schema [:=> [:cat :map Cost]
                  :map]}
  [acc {:keys [category amount]}]
  (if (get acc category)
    (update acc category + amount)
    (assoc acc category amount)))

(defn zero-tolerance
  {:malli/schema [:=> [:cat [:vector Cost]]
                  [:vector Violation]]}
  [costs]
  (let [cd-section-a-costs (->> costs
                                (filter-by :cd :a)
                                (reduce condense {}))
        le-section-a-costs (->> costs
                                (filter-by :le :a)
                                (reduce condense {}))
        violations (reduce (fn [acc [category amount]]
                             (let [le-amt (get le-section-a-costs category)]
                               (when (or (not le-amt)
                                         (< le-amt amount))
                                 (conj acc
                                       {:category category
                                        :le-amount le-amt
                                        :cd-amount amount
                                        :related-costs (filterv #(= (:category %) category) costs)}))))
                           [] cd-section-a-costs)]
    violations))