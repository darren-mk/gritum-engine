(ns gritum.engine.frontend.routes
  (:require
   [gritum.engine.frontend.pages.dashboard :as pg.dashboard]
   [gritum.engine.frontend.pages.docs :as pg.docs]
   [gritum.engine.frontend.pages.home :as pg.home]
   [gritum.engine.frontend.pages.login :as pg.login]
   [gritum.engine.frontend.pages.pricing :as pg.pricing]
   [gritum.engine.frontend.pages.signup :as pg.signup]
   [gritum.engine.frontend.pages.lab :as pg.lab]
   [gritum.engine.frontend.handlers :as handlers]))

(defn pages [mws]
  ["" {:middleware mws}
   ["/" {:get pg.home/handler}]
   ["/docs" {:get pg.docs/handler}]
   ["/login" {:get pg.login/handler}]
   ["/pricing" {:get pg.pricing/handler}]
   ["/signup" {:get pg.signup/handler}]
   ["/dashboard" {:get pg.dashboard/handler}]
   ["/lab" {:get pg.lab/handler}]])

(defn hypermedia [mws ds]
  ["/hypermedia" {:middleware mws}
   ["/hello" {:get handlers/hello}]
   ["/login" {:post (handlers/login ds)}]
   ["/logout" {:post handlers/logout}]
   ["/signup" {:post (handlers/signup ds)}]])
