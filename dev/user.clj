(ns user
  (:require
   [integrant.repl :refer [set-prep! go halt reset]]
   [gritum.engine.api.core :as w]
   [malli.dev :as mdev]
   [malli.dev.pretty :as pretty]))

(defn inst []
  (mdev/start!
   {:report (pretty/reporter)}))

(defn unst []
  (mdev/stop!))

(set-prep! (fn [] w/config))

(comment
  (inst)
  (unst)
  (go)
  (halt)
  (reset))
