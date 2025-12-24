(ns gritum.core)

(defn jump-map [xml k]
  (when (= k (:tag xml)) (:content xml)))

(defn jump-vec [xml k]
  (let [picked (filter #(= k (:tag %)) xml)]
    (if (= 1 (count picked))
       (:content (first picked))
       (throw (ex-info "multiple nodes with same tag"
                       {:key k
                        :xml xml})))))

(defn jump [xml k]
  (cond (map? xml) (jump-map xml k)
        (sequential? xml) (jump-vec xml k)
        :else (throw (ex-info "unknown structure in xml" {}))))

(defn traverse [xml ks]
  (reduce jump xml ks))

(def loan-path
  [:MESSAGE :DOCUMENT_SETS
   :DOCUMENT_SET :DOCUMENTS
   :DOCUMENT :DEAL_SETS :DEAL_SET
   :DEALS :DEAL :LOANS :LOAN])

(def zero-tolerance-fee-types
  #{"AppraisalFee" "CreditReportFee"
    "FloodDeterminationFee" 
    "AppraisalFieldReviewFee"
    "TaxServiceFee"})

(defn ->tolerance [section fee-type]
  (case section
    ("OriginationCharges" "ServicesYouCannotShopFor") 0.0
    ("ServicesYouCanShopFor") 0.1
    ("ServicesBorrowerDidNotShopFor") (if (zero-tolerance-fee-types fee-type) 0.0 0.1)
    ("TaxesAndOtherGovernmentFees") (if (= fee-type "RecordingFee") 0.1 0.0)
    ("Prepaids" "InitialEscrowPaymentAtClosing"
     "ServicesBorrowerDidShopFor" "OtherCosts") nil))

(defmulti normalize
  (fn [_ k] k))

(defmethod normalize :fee [xml _]
  (let [detail (traverse xml [:FEE :FEE_DETAIL])
        fee-content (:content xml)
        payments-node (first (filter #(= :FEE_PAYMENTS (:tag %)) fee-content))
        payments (when payments-node
                   (filter #(= :FEE_PAYMENT (:tag %)) (:content payments-node)))
        section (->> [:IntegratedDisclosureSectionType] (traverse detail) first)
        fee-type (->> [:EXTENSION :OTHER :FEE_DETAIL_EXTENSION :FeeItemType]
                      (traverse detail) first)]
    #:fee{:type fee-type :section section
          :tolerance (->tolerance section fee-type)
          :amount (if (seq payments)
                    (->> payments
                         (filter #(= "Buyer" (first (traverse % [:FEE_PAYMENT :FeePaymentPaidByType]))))
                         (map #(->> [:FEE_PAYMENT :FeeActualPaymentAmount] (traverse %) first parse-double))
                         (reduce + 0.0))
                    0.0)
          :paid-by "Buyer"}))

(defn ->domain [xml]
  (let [loan (traverse xml loan-path)
        fees (traverse loan [:FEE_INFORMATION :FEES])]
    {:fees (mapv #(normalize % :fee) fees)}))

(defn all-unique? [coll]
  (= (count coll)
     (count (distinct coll))))

(defn xml->edn
  "connect by tag and content, ignore attrs"
  [xml]
  (prn xml)
  (cond (nil? xml) nil
        (map? xml) {(:tag xml) (xml->edn (:content xml))}
        (seq? xml) (xml->edn (apply vector xml))
        (vector? xml) (cond (= 1 (count xml))
                            (let [fst (first xml)
                                  calc (xml->edn (first xml))]
                              (if (:tag fst) [calc] calc))
                            (all-unique? (map :tag xml))
                            (reduce (fn [a {:keys [tag content]}]
                                      (assoc a tag (xml->edn content))) {} xml)
                            :else (mapv xml->edn xml))
        (contains? #{"true" "false"} xml) (read-string xml)
        :else xml))
