(ns gritum.engine.web.routes
  (:require
   [gritum.engine.web.pages.dashboard :as pg.dashboard]
   [gritum.engine.web.pages.docs :as pg.docs]
   [gritum.engine.web.pages.home :as pg.home]
   [gritum.engine.web.pages.login :as pg.login]
   [gritum.engine.web.pages.pricing :as pg.pricing]
   [gritum.engine.web.pages.signup :as pg.signup]
   [gritum.engine.web.pages.lab :as pg.lab]
   [gritum.engine.web.handlers :as handlers]))

(defn pages [mws]
  ["" {:middleware mws}
   ["/" {:get pg.home/handler}]
   ["/docs" {:get pg.docs/handler}]
   ["/login" {:get pg.login/handler}]
   ["/pricing" {:get pg.pricing/handler}]
   ["/signup" {:get pg.signup/handler}]
   ["/dashboard" {:get pg.dashboard/handler}]
   ["/lab" {:get pg.lab/handler}]])

(defn hypermedia [mws]
  ["/hypermedia" {:middleware mws}
   ["/hello" {:get handlers/hello}]])
