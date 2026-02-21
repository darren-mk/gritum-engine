(ns gritum.engine.domain.model)

(def Email
  [:re {:error/message "not correct email format"}
   #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])

(def ClientId
  :uuid)

(def Side
  [:enum :le :cd])

(def FullName
  [:string {:min 3
            :max 60}])

(def Password
  [:and
   [:string {:min 8
             :max 80}]
   [:re {:error/message "password cannot contain whitespace"}
    #"^\S+$"]])

(def ClientBase
  [:map
   [:email Email]
   [:full_name FullName]])

(def ClientSeed
  (into ClientBase
        [[:password Password]]))

(def Client
  (into
   ClientBase
   [[:id ClientId]
    [:password_hash {:optional true} :string]
    [:created_at inst?]]))

(def ApiKey
  [:map
   [:id :uuid]
   [:key_id :string]
   [:client_id :uuid]
   [:raw_key {:optional true} :string]
   [:hashed_key {:optional true} :string]
   [:usage_count pos-int?]
   [:usage_limit pos-int?]
   [:is_active :boolean]
   [:created_at inst?]])

(def Category
  [:enum
   :discount-points
   :application-fee
   :underwriting-fee
   :processing-fee
   :funding-commitment-fee
   :doc-prep-fee
   :technology-fee
   :broker-fee
   :appraisal-fee
   :credit-report-fee
   :flood-monitoring-fee
   :flood-determination-fee
   :tax-monitoring-fee
   :tax-status-research-fee
   :title-settlement-fee
   :title-search-fee
   :pest-inspection-fee])

(def Section
  [:enum :a :b :c])

(def Cost
  [:map
   [:side Side]
   [:section Section]
   [:category Category]
   [:description :string]
   [:amount :double]])

(def Costs
  [:sequential Cost])

(def Violation
  [:map
   [:rule :keyword]
   [:category Category]
   [:le-amount :double]
   [:cd-amount :double]
   [:related-costs [:vector Cost]]])

(def Violations
  [:sequential Violation])
