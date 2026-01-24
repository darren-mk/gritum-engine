(ns gritum.engine.domain.rules.lender-credit-tolerance
  "Implementation of the 0% tolerance rule for Lender Credits.
   A violation occurs if the total credit amount decreases from LE to CD."
  (:require [gritum.engine.domain.model :as dom]))

(defn- calculate-total-credit
  "Calculates the total lender credit by summing the summary
  amount and any individual fees paid by the Lender."
  [doc]
  (let [summary-amount (get-in doc [:lender-credit :amount] 0.0)
        ;; Sum all fee payments where the payer is :lender
        lender-paid-fees (->> (:fees doc)
                              (mapcat :payments)
                              (filter #(= :lender (:payer %)))
                              (map :amount)
                              (reduce + 0.0))]
    (+ summary-amount lender-paid-fees)))

(defn evaluate
  "Compares the total lender credit between LE and CD Documents.
   Calculates the required cure if credits decreased."
  {:malli/schema [:=> [:cat dom/Document dom/Document] :map]}
  [le-doc cd-doc]
  (let [le-total (calculate-total-credit le-doc)
        cd-total (calculate-total-credit cd-doc)
        ;; Existing cure already recorded in the CD XML summary
        existing-cure (get-in cd-doc [:lender-credit :cure-amount] 0.0)
        ;; Required cure is triggered if LE credit > CD credit
        required-cure (max 0.0 (- le-total cd-total))]
    {:rule :lender-credit
     :le-total-credit le-total
     :cd-total-credit cd-total
     :existing-cure existing-cure
     :required-cure required-cure
     ;; Violated if the engine's calculation exceeds what's already in the XML
     :violated? (> required-cure existing-cure)
     :description (if (> required-cure 0.0)
                    (format "Lender Credit decreased. Required Cure: %.2f" required-cure)
                    "Lender Credit is compliant.")}))
