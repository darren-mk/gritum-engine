(ns gritum.engine.api.middlewares
  (:require
   [clojure.string :as cstr]
   [gritum.engine.db.api-key :as db.api-key]
   [jsonista.core :as json]
   [taoensso.timbre :as log]
   [ring.middleware.session :as ses]
   [ring.middleware.session.memory :as smem]
   [ring.util.http-response :as resp]))

(def json-mapper
  (json/object-mapper
   {:decode-key-fn true}))

(defn inject-headers-in-resp [handler]
  (let [m {"Content-Type"
           "application/json; charset=utf-8"}]
    (fn [req]
      (let [resp (handler req)]
        (if resp
          (update resp :headers merge m)
          resp)))))

(defn read-body [handler]
  (fn [{:keys [content-type request-method] :as request}]
    (if (and (= request-method :post)
             (cstr/starts-with? (or content-type "")
                                "application/json"))
      (let [body-str (slurp (:body request))
            json-params (if-not (empty? body-str)
                          (json/read-value body-str json-mapper)
                          {})
            new-request (assoc request :body json-params)]
        (handler new-request))
      (handler request))))

(defn write-body [handler]
  (let [f #(json/write-value-as-bytes
            % json/default-object-mapper)]
    (fn [req]
      (let [{:keys [body] :as resp} (handler req)]
        (if body
          (update resp :body f)
          resp)))))

(defn wrap-exception [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable e
        (log/error e "Unhandled exception occurred during request")
        (let [error-data (ex-data e)
              status (:status error-data 500)]
          {:status status
           :body {:error (if (= status 500) "Internal Server Error" "Client Error")
                  :message (.getMessage e)
                  :type (.getSimpleName (class e))}})))))

(defn wrap-public-cors [handler]
  (fn [req]
    (let [headers {"Access-Control-Allow-Origin" "*"
                   "Access-Control-Allow-Methods"
                   "GET, POST, PUT, DELETE, OPTIONS"
                   "Access-Control-Allow-Headers"
                   "Content-Type, Authorization, x-api-key"}]
      (if (= (:request-method req) :options)
        {:status 200 :headers headers :body ""}
        (let [resp (handler req)]
          (if resp (update resp :headers merge headers) resp))))))

(defn wrap-dashboard-cors [handler]
  (fn [req]
    (let [origin (get-in req [:headers "origin"])
          headers {"Access-Control-Allow-Origin" origin
                   "Access-Control-Allow-Methods"
                   "GET, POST, PUT, DELETE, OPTIONS"
                   "Access-Control-Allow-Headers"
                   "Content-Type, Authorization, x-api-key"
                   "Access-Control-Allow-Credentials" "true"}]
      (if (= (:request-method req) :options)
        {:status 200 :headers headers :body ""}
        (let [resp (handler req)]
          (if resp (update resp :headers merge headers) resp))))))

(defn wrap-api-key-auth [ds]
  (fn [handler]
    (fn [request]
      (let [api-key (get-in request [:headers "x-api-key"])]
        (if-let [key-info (and api-key (db.api-key/verify! ds api-key))]
          (handler (assoc request :identity key-info))
          (resp/unauthorized {:error "Invalid or missing API key"}))))))

(def session-options
  {:store (smem/memory-store)
   :cookie-name "bitem-session"
   :cookie-attrs {:http-only true
                  :secure false ; 개발 환경(HTTP)에서는 false, 프로덕션(HTTPS)은 true
                  :same-site :lax}})

(defn wrap-session [handler]
  (ses/wrap-session handler session-options))
