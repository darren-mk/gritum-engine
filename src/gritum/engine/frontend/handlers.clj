(ns gritum.engine.frontend.handlers
  (:require
   [gritum.engine.db.client :as db.client]
   [gritum.engine.frontend.pages.lab :as pg.lab]
   [gritum.engine.frontend.pages.login :as pg.login]
   [gritum.engine.frontend.signal :as sg]
   [hiccup2.core :as h]
   [starfederation.datastar.clojure.api :as ds]
   [starfederation.datastar.clojure.adapter.http-kit :as dshk]))

(defn hello [request]
  (dshk/->sse-response
   request
   {dshk/on-open
    (fn [sse]
      (ds/patch-elements! sse (-> pg.lab/stream-basic-resp-1 h/html str))
      (Thread/sleep 2000)
      (ds/patch-elements! sse (-> pg.lab/stream-basic-resp-2 h/html str)))}))

(defn login [ds]
  (fn [{:keys [body] :as _request}]
    (let [{:keys [email password]} body]
      (if-let [client (db.client/authenticate ds email password)]
        ;; --- SUCCESS CASE ---
        {:status 200
         :headers {"Content-Type" "text/javascript"}
         :body "window.location.href = '/dashboard'"
         :session {:identity (:id client)}}
        ;; --- FAILURE CASE ---
        {:status 200
         :headers {"Content-Type" "text/vnd.datastar+html"}
         :body (str "event: datastar-fragment\n"
                    "data: fragment " (h/html pg.login/fail-msg) "\n\n"
                    "event: datastar-execute-script\n"
                    "data: script " (format "datastar.applySignals({%s: false})"
                                            (sg/bind pg.login/is-processing-k)) "\n\n")}))))

(defn logout
  "Provides nil to session key, mw removes expire the cookie."
  [_req]
  {:status 200
   :headers {"Content-Type" "text/javascript"}
   :session nil
   :body "window.location.href = '/login'"})
