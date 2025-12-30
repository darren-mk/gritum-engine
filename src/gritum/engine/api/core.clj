(ns gritum.engine.api.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as http]
   [gritum.engine.api.router :as router]
   [taoensso.timbre :as log]))

(defn get-port []
  (Integer/parseInt
   (or (System/getenv "PORT") "3000")))

(def config
  {:gritum.web/app {}
   :gritum.web/server
   {:port (get-port)
    :handler (ig/ref :gritum.web/app)}})

(defmethod ig/init-key :gritum.web/app [_ _]
  (log/info "Initializing Web Router...")
  router/app)

(defmethod ig/init-key :gritum.web/server [_ {:keys [handler port]}]
  (log/info "Starting HTTP server on port:" port)
  (http/run-server handler {:port port}))

(defmethod ig/halt-key! :gritum.web/server [_ stop-fn]
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
