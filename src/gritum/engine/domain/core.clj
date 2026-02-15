(ns gritum.engine.domain.core
  (:require
   [gritum.engine.domain.extract :as extract]
   [gritum.engine.domain.model :refer [Violations]]
   [gritum.engine.domain.rules :as rules]))

(defn process!
  {:malli/schema [:=> [:cat :string :string :string :string]
                  Violations]}
  [api-key ai-model le-file cd-file]
  (let [le-costs (future (extract/proceed! api-key ai-model :le le-file))
        cd-costs (future (extract/proceed! api-key ai-model :cd cd-file))]
    (rules/pipeline (vec (concat @le-costs @cd-costs)))))

(comment
  (require '[gritum.engine.configs :as configs])
  (let [{:keys [ai-api-key ai-model]} (configs/get-llm-config)]
    (process! ai-api-key ai-model "data/le-a.pdf" "data/cd-a.pdf"))
  ;;=> [{:category :discount-points,
  ;;     :le-amount nil,
  ;;     :cd-amount 320.0,
  ;;     :related-costs
  ;;     [{:section :a, :category :discount-points, :description "% of Loan Amount (Points)", :amount 320.0, :side :cd}]}
  ;;    {:category :application-fee,
  ;;     :le-amount 300.0,
  ;;     :cd-amount 320.0,
  ;;     :related-costs
  ;;     [{:section :a, :category :application-fee, :description "Application Fees", :amount 300.0, :side :le}
  ;;      {:section :a, :category :application-fee, :description "App fees", :amount 320.0, :side :cd}]}
  ;;    {:category :underwriting-fee,
  ;;     :le-amount 300.0,
  ;;     :cd-amount 320.0,
  ;;     :related-costs
  ;;     [{:section :a, :category :underwriting-fee, :description "Underwriting Fees", :amount 300.0, :side :le}
  ;;      {:section :a, :category :underwriting-fee, :description "Underwriting fees", :amount 320.0, :side :cd}]}]
  )
