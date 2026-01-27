(ns gritum.engine.frontend.layout
  (:require
   [hiccup2.core :as h]
   [gritum.engine.frontend.components.navbar :as navbar]))

(defn base [title content client-id]
  (h/html
   (h/raw "<!DOCTYPE html>")
   [:html {:lang "en"}
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:title title]
     [:script {:type :module
               :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.7/bundles/datastar.js"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"}]]
    [:body {:class "bg-white"}
     (navbar/basic client-id)
     [:div {:class ["pt-16" "min-h-screen"]}
      content]]]))
