(ns gritum.engine.api.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.engine.auth :as auth]
   [gritum.engine.api.router :as router]
   [taoensso.timbre :as log]))

(defn get-env []
  (let [env-str (System/getenv "GRITUM_ENV")]
    (case env-str
      ("prod" "production") :prod
      "staging" :staging
      :local)))

(defn prod? []
  (= :prod (get-env)))

(defn get-port []
  (Integer/parseInt
   (or (System/getenv "PORT") "3000")))

(def config
  {:gritum.engine.api/auth {}
   :gritum.engine.api/app
   {:prod? (prod?)
    :auth-fn (ig/ref :gritum.engine.api/auth)}
   :gritum.engine.api/server
   {:port (get-port)
    :handler (ig/ref :gritum.engine.api/app)}})

(defmethod ig/init-key :gritum.engine.api/auth
  [_ _]
  (auth/create-auth-service {}))

(defmethod ig/init-key :gritum.engine.api/app
  [_ injection]
  (log/info "Initializing Web Router...")
  (router/app injection))

(defmethod ig/init-key :gritum.engine.api/server
  [_ {:keys [handler port]}]
  (log/info "Starting HTTP server on port:" port)
  (http/run-server handler {:port port}))

(defmethod ig/halt-key! :gritum.engine.api/server
  [_ stop-fn]
  (log/info "Stopping HTTP server...")
  (stop-fn :timeout 100))

(defn -main
  [& _args]
  (let [system (ig/init config)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(ig/halt! system)))
    (log/info "gritum engine is running on port "
              (get-port))))
