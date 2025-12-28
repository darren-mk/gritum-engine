(ns gritum.evaluate
  "Orchestrates the evaluation of fee tolerance rules between LE and CD."
  (:require
   [gritum.domain :as dom]
   [gritum.extract :as ext]
   [gritum.rules.zero-percent-tolerance :as zero]
   [gritum.rules.ten-percent-tolerance :as ten]))

(def path-to-fees
  [:MESSAGE :DOCUMENT_SETS :DOCUMENT_SET
   :DOCUMENTS :DOCUMENT :DEAL_SETS :DEAL_SET :DEALS
   :DEAL :LOANS :LOAN :FEE_INFORMATION :FEES])

(defn- extract-fees
  "Locates all FEE nodes within the
  deeply nested MISMO/UCD structure."
  {:malli/schema [:=> [:cat dom/Xml]
                  [:vector dom/Fees]]}
  [xml]
  (->> path-to-fees
       (ext/traverse xml)
       (filter #(= :FEE (:tag %)))
       (mapv dom/->fee)))

(defn perform
  "Evaluates all tolerance rules and returns an aggregated result report."
  {:malli/schema [:=> [:cat dom/Xml dom/Xml] :map]}
  [le-xml cd-xml]
  (let [le-fees (extract-fees le-xml)
        cd-fees (extract-fees cd-xml)
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
