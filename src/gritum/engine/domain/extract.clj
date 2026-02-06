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