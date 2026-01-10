(ns gritum.engine.api.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.engine.api.router :as router]
   [gritum.engine.db.core]
   [gritum.engine.configs :as configs]
   [taoensso.timbre :as log]))

(def config
  {:gritum.engine.db/pool (configs/get-db-config)
   :gritum.engine.api/app {:ds (ig/ref :gritum.engine.db/pool)}
   :gritum.engine.api/server {:port (configs/get-port)
                              :handler (ig/ref :gritum.engine.api/app)}})

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
              (configs/get-port))))
