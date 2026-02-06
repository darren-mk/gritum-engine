(ns gritum.engine.external.gemini
  (:require
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.java.io :as io]))

(defn get-all-models [api-key]
  (let [url (str "https://generativelanguage.googleapis.com/v1beta/models?key=" api-key)]
    (try
      (-> (client/get url {:as :json})
          (get-in [:body :models]))
      (catch Exception e
        (println "Could not even list models. Error:" (:body (ex-data e)))))))

(comment
  (require '[gritum.engine.configs :as configs])
  (get-all-models (:ai-api-key (configs/get-gemini-config))))

(defn get-prompt [file]
  (let [path (str "prompts/" file)]
    (slurp (io/resource path))))

(defn pdf-to-base64 [file-path]
  (with-open [in (io/input-stream file-path)
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.encodeToString
     (java.util.Base64/getEncoder)
     (.toByteArray out))))

(defn call!
  ([api-key ai-model prompt]
   (call! api-key ai-model prompt nil))
  ([api-key ai-model prompt pdf-file-path]
   (let [url (str "https://generativelanguage.googleapis.com/v1beta/models/"
                  ai-model ":generateContent?key=" api-key)
         parts (into [{:text prompt}]
                     (when pdf-file-path
                       [{:inline_data {:mime_type "application/pdf"
                                       :data (pdf-to-base64 pdf-file-path)}}]))
         payload {:contents [{:parts parts}]
                  :generationConfig {:response_mime_type "application/json"}}]
     (-> (client/post url {:body (json/generate-string payload)
                           :content-type :json
                           :as :json})
         :body :candidates first :content :parts first :text
         (json/parse-string true)))))