(ns gritum.engine.domain.extract)

(defn jump-map [xml k]
  (when (= k (:tag xml)) (:content xml)))

(defn jump-vec [xml k]
  (let [picked (filter #(= k (:tag %)) xml)]
    (case (count picked)
      0 nil
      1 (:content (first picked))
      (throw (ex-info "multiple nodes with same tag"
                      {:key k})))))

(defn jump [xml k]
  (cond (map? xml) (jump-map xml k)
        (sequential? xml) (jump-vec xml k)
        (nil? xml) xml
        :else (throw (ex-info "unknown structure in xml"
                              {:key k}))))

(defn traverse [xml ks]
  (reduce jump xml ks))

(defn all-unique? [coll]
  (= (count coll)
     (count (distinct coll))))

(defn ->edn
  "connect by tag and content, ignore attrs"
  [xml]
  (cond (nil? xml) nil
        (map? xml) {(:tag xml) (->edn (:content xml))}
        (seq? xml) (->edn (apply vector xml))
        (vector? xml) (cond (= 1 (count xml))
                            (let [fst (first xml)
                                  calc (->edn (first xml))]
                              (if (:tag fst) [calc] calc))
                            (all-unique? (map :tag xml))
                            (reduce (fn [a {:keys [tag content]}]
                                      (assoc a tag (->edn content))) {} xml)
                            :else (mapv ->edn xml))
        (contains? #{"true" "false"} xml) (read-string xml)
        :else xml))
