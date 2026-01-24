(ns gritum.engine.domain.evaluate
  "Orchestrates the evaluation of fee tolerance rules between LE and CD."
  (:require
   [gritum.engine.domain.model :as dom]
   [gritum.engine.domain.rules.zero-percent-tolerance :as zero]
   [gritum.engine.domain.rules.ten-percent-tolerance :as ten]))

(defn perform
  "Evaluates all tolerance rules and returns an aggregated result report."
  {:malli/schema [:=> [:cat dom/Xml dom/Xml] :map]}
  [le-xml cd-xml]
  (let [le-fees (dom/extract-fees le-xml)
        cd-fees (dom/extract-fees cd-xml)
        ;; Evaluate 0% tolerance (Item-by-item check)
        zero-report (zero/calculate-total-cure le-fees cd-fees)
        ;; Evaluate 10% tolerance (Aggregate check)
        ten-report  (ten/calculate-cure le-fees cd-fees)
        ;; Sum all calculated cure amounts
        total-cure (+ (:total-cure zero-report)
                      (:cure-amount ten-report))]
    {:total-cure total-cure
     :breakdown {:zero-percent zero-report
                 :ten-percent  ten-report}
     :valid? (zero? total-cure)
     :evaluated-at (java.time.Instant/now)}))
