(ns gritum.engine.web.layout
  (:require
   [hiccup2.core :as h]))

(defn base [title content]
  (h/raw
   (str
    "<!DOCTYPE html>"
    (h/html
     [:html {:lang "en"}
      [:head
       [:meta {:charset "UTF-8"}]
       [:title title]
       [:script {:src "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"}]
       [:script {:type :module
                 :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.7/bundles/datastar.js"}]]
      [:body {:class "bg-slate-50"}
       content]]))))
