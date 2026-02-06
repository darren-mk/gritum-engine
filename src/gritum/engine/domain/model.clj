(ns gritum.engine.domain.model
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as cstr]
   [gritum.engine.domain.extract :as ext]))

(def Xml
  [:map
   [:tag :keyword]
   [:content :any]])

(def StakeholderKind
  [:enum :buyer :seller :lender])

(def Email
  [:re {:error/message "not correct email format"}
   #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"])

(def ClientId
  :uuid)

(def Client
  [:map
   [:id ClientId]
   [:email Email]
   [:password_hash {:optional true} :string]
   [:full_name :string]
   [:created_at inst?]])

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

(def Payee
  [:map
   [:name :string]
   [:kind StakeholderKind]])

(def Section
  [:enum
   :origination-charges
   :services-not-shop
   :services-shop
   :taxes :prepaids
   :taxes-and-other-government-fees
   :services-borrower-did-shop-for
   :services-borrower-did-not-shop-for
   :initial-escrow
   :other-costs
   :unknown-section])

(def Timing
  [:enum :at-closing :before-closing])

(def Money
  [:and :double [:>= 0]])

(def Payment
  [:map
   [:amount Money]
   [:payer StakeholderKind]
   [:timing Timing]
   [:meta {:optional true} :map]])

(def Fee
  [:map
   [:id :string]
   [:section Section]
   [:category Category]
   [:label :string]
   [:payee Payee]
   [:payments [:vector Payment]]
   [:meta {:optional true} :map]])

(def Fees
  [:sequential Fee])

(def LenderCredit
  [:map
   [:amount Money]
   [:cure-amount Money]
   [:section-type :keyword]
   [:subsection-type :keyword]])

(def Document
  [:map
   [:fees Fees]
   [:lender-credit LenderCredit]])

(defn ->payment
  {:malli/schema [:=> [:cat Xml] Payment]}
  [payment-xml]
  (let [payer (or (some->> [:FEE_PAYMENT :FeePaymentPaidByType]
                           (ext/traverse payment-xml) first
                           cstr/lower-case keyword)
                  :buyer)
        poc-ind (some->> [:FEE_PAYMENT :FeePaymentPaidOutsideOfClosingIndicator]
                         (ext/traverse payment-xml) first)
        timing (case poc-ind "true" :before-closing "false" :at-closing :at-closing)
        amount (some->> [:FEE_PAYMENT :FeeActualPaymentAmount]
                        (ext/traverse payment-xml) first parse-double)]
    {:amount amount :timing timing :payer payer}))

(defn ->fee
  {:malli/schema [:=> [:cat Xml] Fee]}
  [fee-xml]
  (let [detail (ext/traverse fee-xml [:FEE :FEE_DETAIL])
        payments (mapv ->payment (ext/traverse fee-xml [:FEE :FEE_PAYMENTS]))
        payee-name (or (some->> [:EXTENSION :OTHER :ucd:FEE_DETAIL_EXTENSION
                                 :ucd:FeePaidToEntityName] (ext/traverse detail) first)
                       "Unknown Payee")
        payee-kind-str (->> [:FeePaidToType] (ext/traverse detail) first)
        payee-kind (case payee-kind-str "Lender" :lender "ThirdParty" :seller :lender)
        section-str (->> [:IntegratedDisclosureSectionType] (ext/traverse detail) first)
        section (or (some-> section-str csk/->kebab-case-keyword) :unknown-section)
        fee-item-type-node (->> [:EXTENSION :OTHER :ucd:FEE_DETAIL_EXTENSION]
                                (ext/traverse detail)
                                (filter #(= :ucd:FeeItemType (:tag %))) first)
        category-str (-> fee-item-type-node :content first)
        category (or (some-> category-str csk/->kebab-case-keyword) :unknown-category)
        label (or (-> fee-item-type-node :attrs :DisplayLabelText) category-str "unknown label")
        id (str (name section) "_" (name category))]
    {:id id
     :section section
     :category category
     :label label
     :payee {:name payee-name
             :kind payee-kind}
     :payments payments}))

(defn ->lender-credit
  "Maps a INTEGRATED_DISCLOSURE_SECTION_SUMMARY node to a LenderCredit record."
  {:malli/schema [:=> [:cat :any] LenderCredit]}
  [lender-credit-xml]
  (let [section-type (some->> [:IntegratedDisclosureSectionType]
                              (ext/traverse lender-credit-xml)
                              first csk/->kebab-case-keyword)
        subsection-type (some->> [:IntegratedDisclosureSubsectionType]
                                 (ext/traverse lender-credit-xml)
                                 first csk/->kebab-case-keyword)
        amount (or (some->> [:IntegratedDisclosureSectionTotalAmount]
                            (ext/traverse lender-credit-xml)
                            first parse-double) 0.0)
        cure (or (some->> [:LenderCreditToleranceCureAmount]
                          (ext/traverse lender-credit-xml)
                          first parse-double) 0.0)]
    {:amount amount
     :cure-amount cure
     :section-type section-type
     :subsection-type subsection-type}))

(def path-to-fees
  [:MESSAGE :DOCUMENT_SETS :DOCUMENT_SET
   :DOCUMENTS :DOCUMENT :DEAL_SETS :DEAL_SET :DEALS
   :DEAL :LOANS :LOAN :FEE_INFORMATION :FEES])

(defn extract-fees
  "Locates all FEE nodes within the
  deeply nested MISMO/UCD structure."
  {:malli/schema [:=> [:cat Xml] Fees]}
  [xml]
  (->> path-to-fees
       (ext/traverse xml)
       (filter #(= :FEE (:tag %)))
       (mapv ->fee)))

(def path-to-idss
  [:MESSAGE :DOCUMENT_SETS :DOCUMENT_SET :DOCUMENTS :DOCUMENT
   :DEAL_SETS :DEAL_SET :DEALS :DEAL :LOANS :LOAN
   :DOCUMENT_SPECIFIC_DATA_SETS
   :DOCUMENT_SPECIFIC_DATA_SET
   :INTEGRATED_DISCLOSURE
   :INTEGRATED_DISCLOSURE_SECTION_SUMMARIES])

(defn extract-lender-credit
  "Locates all FEE nodes within the
  deeply nested MISMO/UCD structure."
  {:malli/schema [:=> [:cat Xml] LenderCredit]}
  [xml]
  (some->> path-to-idss (ext/traverse xml)
           (map #(ext/traverse % [:INTEGRATED_DISCLOSURE_SECTION_SUMMARY
                                  :INTEGRATED_DISCLOSURE_SECTION_SUMMARY_DETAIL]))
           (filter #(= ["LenderCredits"]
                       (ext/traverse % [:IntegratedDisclosureSubsectionType])))
           first ->lender-credit))

(defn ->document
  {:malli/schema [:=> [:cat Xml] Document]}
  [xml]
  {:fees (extract-fees xml)
   :lender-credit (extract-lender-credit xml)})
