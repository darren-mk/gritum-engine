(ns gritum.engine.domain.parse
  (:require
   [cheshire.core :as json]
   [clojure.string :as cstr]
   [gritum.engine.configs :as configs]
   [gritum.engine.domain.model :as model]
   [gritum.engine.external.gemini :as gemini]
   [gritum.engine.external.utils :as eut]))

(def le-extraction-template "extract-le-pdf.txt")
(def cd-extraction-template "extract-cd-pdf.txt")
(def le-and-cd-combination-template "match-le-and-cd.txt")

(def category-list
  (->> (rest model/Category)
       (map #(str "- " (name %)))
       (cstr/join "\n")))

(defn extract! [api-key model kind file]
  (let [txt-file (case kind
                   :le le-extraction-template
                   :cd cd-extraction-template)
        prompt (eut/inject-into-txt
                (gemini/get-prompt txt-file)
                :standard_categories category-list)]
    (gemini/call! api-key model prompt file)))

(defn combine! [ai-api-key ai-model le-result cd-result]
  (let [template (gemini/get-prompt le-and-cd-combination-template)
        prompt (-> template
                   (eut/inject-into-txt :standard-categories category-list)
                   (eut/inject-into-txt :le_json_string (json/generate-string le-result))
                   (eut/inject-into-txt :cd_json_string (json/generate-string cd-result)))]
    (gemini/call! ai-api-key ai-model prompt nil)))

(defn extract-and-combine! [api-key model le-file cd-file]
  (let [le-result (extract! api-key model :le le-file)
        cd-result (extract! api-key model :cd cd-file)]
    (combine! api-key model le-result cd-result)))

(comment
  (def cd
    (let [{:keys [ai-api-key ai-model]} (configs/get-gemini-config)
          file "/Users/darrenkim/Desktop/201311_cfpb_kbyo_closing-disclosure.pdf"]
      (extract! ai-api-key ai-model :cd file)))
  (def le
    (let [{:keys [ai-api-key ai-model]} (configs/get-gemini-config)
          file "/Users/darrenkim/Desktop/201311_cfpb_kbyo_loan-estimate.pdf"]
      (extract! ai-api-key ai-model :le file)))
  (def combined
    (combine! (:ai-api-key (configs/get-gemini-config))
              (:ai-model (configs/get-gemini-config))
              le cd))
  (def extract-and-combined
    (extract-and-combine! (:ai-api-key (configs/get-gemini-config))
                          (:ai-model (configs/get-gemini-config))
                          "/Users/darrenkim/Desktop/201311_cfpb_kbyo_loan-estimate.pdf"
                          "/Users/darrenkim/Desktop/201311_cfpb_kbyo_closing-disclosure.pdf")))