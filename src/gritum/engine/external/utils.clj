(ns gritum.engine.external.utils
  (:require
   [clojure.string :as cstr]))

(defn inject-into-txt [s key snippet]
  (cstr/replace s (str "{{" (name key) "}}") snippet))
