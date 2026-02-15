(ns gritum.engine.domain.parse
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as cstr]
   [gritum.engine.configs :as configs]
   [gritum.engine.domain.model :as model :refer [Cost Side]]
   [gritum.engine.external.llm :as llm]
   [gritum.engine.external.utils :as eut]
   [malli.core :as m]))

(def le-extraction-template
  "extract-le.md")

(def cd-extraction-template
  "extract-cd.md")

(def category-list
  (->> (rest model/Category)
       (map #(str "- " (name %)))
       (cstr/join "\n")))

(def extraction-json-schema
  {:type "object"
   :properties
   {:doctype {:type "string" :enum ["LE" "CD"]}
    :items
    {:type "array"
     :items {:type "object"
             :properties {:section {:type "string"
                                    :enum ["A" "B" "C"]}
                          :category {:type "string"}
                          :description {:type "string"}
                          :amount {:type "number"}}
             :required ["section" "category" "description" "amount"]}}}
   :required ["doctype" "items"]})

(defn zero-or-nil? [x]
  (or (zero? x) (nil? x)))

(defn remove-zero-or-nil [items]
  (remove #(zero-or-nil? (:amount %)) items))

(defn keywordify-category [items]
  (mapv #(update % :category csk/->kebab-case-keyword) items))

(defn keywordify-section [items]
  (mapv #(update % :section
                 (comp csk/->kebab-case-keyword cstr/lower-case))
        items))

(defn include-side [side items]
  (mapv #(assoc % :side side) items))

(defn ->costs [side items]
  (let [include-side-f (partial include-side side)]
    (-> items
        remove-zero-or-nil
        keywordify-section
        keywordify-category
        include-side-f)))

(defn extract!
  {:malli/schema [:=> [:cat :string :string Side :string]
                  [:vector Cost]]}
  [api-key ai-model side file]
  (let [txt-file (case side
                   :le le-extraction-template
                   :cd cd-extraction-template)
        prompt (eut/inject-into-txt
                (llm/get-prompt txt-file)
                :standard-categories category-list)
        {:keys [items]} (llm/call! api-key ai-model prompt
                                   extraction-json-schema file)]
    (->costs side items)))

(comment
  (let [{:keys [ai-api-key ai-model]} (configs/get-llm-config)]
    (extract! ai-api-key ai-model :le "data/le-a.pdf"))
  [{:section :a, :category :application-fee, :description "Application Fees", :amount 300.0, :side :le}]
  (let [{:keys [ai-api-key ai-model]} (configs/get-llm-config)]
    (extract! ai-api-key ai-model :cd "data/cd-a.pdf"))
  [{:section :a, :category :application-fee, :description "App fees", :amount 320.0, :side :cd}])
