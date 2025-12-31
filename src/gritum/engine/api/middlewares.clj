(ns gritum.engine.api.middlewares
  (:require
   [jsonista.core :as json]
   [taoensso.timbre :as log]))

(defn inject-headers-in-resp [handler]
  (let [m {"Content-Type"
           "application/json; charset=utf-8"}]
    (fn [req]
      (let [resp (handler req)]
        (if resp
          (update resp :headers merge m)
          resp)))))

(defn turn-resp-body-to-bytes [handler]
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

(defn wrap-api-cors [handler]
  (let [headers {"Access-Control-Allow-Origin"
                 "*"
                 "Access-Control-Allow-Methods"
                 "GET, POST, PUT, DELETE, OPTIONS"
                 "Access-Control-Allow-Headers"
                 "Content-Type, Authorization, x-api-key"}]
    (fn [req]
      (if (= (:request-method req) :options)
        {:status 200 :headers headers :body ""}
        (let [resp (handler req)]
          (if resp (update resp :headers merge headers) resp))))))

(defn wrap-api-key-auth
  {:malli/schema [:=> [:cat :boolean :any] :any]}
  [prod? check-fn]
  (fn [handler]
    (fn [req]
      (let [api-key (get-in req [:headers "x-api-key"])]
        (if (and api-key (check-fn prod? api-key))
          (handler req)
          {:status 401
           :body {:error "Unauthorized"}})))))
