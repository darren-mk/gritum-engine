(ns gritum.engine.api.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.engine.auth :as auth]
   [gritum.engine.api.router :as router]
   [gritum.engine.db.core]
   [gritum.engine.infra :as inf]
   [taoensso.timbre :as log]))

(defn get-port []
  (:port (inf/->context)))

(def config
  {:gritum.engine.db/pool
   (->> (inf/->context) :env
        (get (inf/->config)) :db)
   :gritum.engine.api/auth {}
   :gritum.engine.api/app
   {:prod? (inf/prod?)
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
