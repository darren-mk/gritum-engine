(ns gritum.engine.frontend.handlers
  (:require
   [gritum.engine.db.client :as db.client]
   [gritum.engine.frontend.pages.lab :as pg.lab]
   [gritum.engine.frontend.pages.login :as pg.login]
   [gritum.engine.frontend.pages.signup :as pg.signup]
   [gritum.engine.frontend.signal :as sg]
   [rum.core :as rum]
   [starfederation.datastar.clojure.adapter.http-kit :as dshk]
   [starfederation.datastar.clojure.api :as ds]
   [taoensso.timbre :as log]))

(defn patch! [sse]
  (fn [x]
    (ds/patch-elements!
     sse (rum/render-static-markup x))))

(defn hello [request]
  (dshk/->sse-response
   request
   {dshk/on-open
    (fn [sse]
      (->> pg.lab/stream-basic-resp-1
           rum/render-static-markup
           (ds/patch-elements! sse))
      (Thread/sleep 2000)
      (->> pg.lab/stream-basic-resp-2
           rum/render-static-markup
           (ds/patch-elements! sse)))}))

(defn login [ds]
  (fn [{:keys [body] :as _request}]
    (let [{:keys [email password]} body]
      (if-let [client (db.client/authenticate ds email password)]
        {:status 200
         :headers {"Content-Type" "text/javascript"}
         :body "window.location.href = '/dashboard'"
         :session {:identity (:id client)}}
        {:status 200
         :headers {"Content-Type" "text/vnd.datastar+html"}
         :body (str "event: datastar-fragment\n"
                    "data: fragment " (rum/render-static-markup pg.login/fail-msg) "\n\n"
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

(defn signup [ds]
  (fn [{:keys [body] :as req}]
    (let [{:keys [email password full_name]} body
          [result data] (try (db.client/register! ds email password full_name)
                             (catch Exception e (log/error e)))]
      (case result
        :success {:status 200
                  :headers {"Content-Type" "text/javascript"}
                  :body "window.location.href = '/login'"}
        :fail (dshk/->sse-response
               req
               {dshk/on-open
                (fn [sse]
                  (->> (pg.signup/fail-msg data)
                       rum/render-static-markup
                       (ds/patch-elements! sse))
                  (->> (sg/->json-obj
                        {pg.signup/is-processing-k false})
                       (ds/patch-signals! sse)))})
        nil (dshk/->sse-response
             req
             {dshk/on-open
              (fn [sse]
                (->> (pg.signup/fail-msg {:error ["Please retry"]})
                     rum/render-static-markup
                     (ds/patch-elements! sse))
                (->> (sg/->json-obj
                      {pg.signup/is-processing-k false})
                     (ds/patch-signals! sse)))})))))
