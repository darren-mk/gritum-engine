(ns gritum.core)

#_#_#_#_#_
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
                      (traverse detail) first)
        in-apr? (contains? apr-mandatory-fees fee-type)]
    #:fee{:type fee-type :section section
          :tolerance (->tolerance section fee-type)
          :amount (if (seq payments)
                    (->> payments
                         (filter #(= "Buyer" (first (traverse % [:FEE_PAYMENT :FeePaymentPaidByType]))))
                         (map #(->> [:FEE_PAYMENT :FeeActualPaymentAmount] (traverse %) first parse-double))
                         (reduce + 0.0))
                    0.0)
          :in-apr? in-apr?
          
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
