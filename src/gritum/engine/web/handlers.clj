(ns gritum.engine.web.handlers
  (:require
   [gritum.engine.web.pages.lab :as pg.lab]
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
