(ns gritum.engine.frontend.pages.dashboard
  (:require
   [gritum.engine.frontend.layout :as layout]
   [jsonista.core :as j]))

(defn console-header [user]
  [:section {:class ["mb-12" "flex" "justify-between" "items-end"]}
   [:div
    [:h1 {:class ["text-4xl" "font-black" "text-gray-900" "tracking-tight"]}
     "Console"]
    [:p {:class ["text-gray-500" "mt-1"]}
     "Manage your API services for Bitem Labs."]]
   [:div {:class ["text-right"]}
    [:p {:class ["text-xs" "font-bold" "text-gray-400" "uppercase" "tracking-widest"]}
     "Logged In As"]
    [:p {:class ["text-sm" "font-bold" "text-blue-600"]}
     (or (:full_name user) (:email user))]]])

(defn api-key-row [key-data]
  [:tr {:class ["border-t" "border-gray-50" "hover:bg-gray-50/30"]}
   [:td {:class ["px-8" "py-5" "font-semibold" "text-gray-800"]}
    (or (:name key-data) "Untitled Key")]
   [:td {:class ["px-8" "py-5" "text-sm" "font-mono" "text-gray-500"]}
    (:key_id key-data)]
   [:td {:class ["px-8" "py-5" "text-sm" "text-gray-500"]}
    (:created_at key-data)]
   [:td {:class ["px-8" "py-5"]}
    [:span {:class ["px-2" "py-1" "bg-green-100" "text-green-800" "text-xs" "font-bold" "rounded-full"]}
     "Active"]]])

(defn api-keys-table [api-keys]
  [:section {:class ["bg-white" "border" "border-gray-100" "rounded-3xl" "shadow-sm" "overflow-hidden"]}
   ;; Table Header Actions
   [:div {:class ["p-8" "border-b" "border-gray-50" "flex" "justify-between" "items-center"]}
    [:h2 {:class ["text-xl" "font-bold" "text-gray-800"]} "API Keys"]
    [:button {:data-on-click "@post('/api/dashboard/auth/api-keys/new')" ;; Datastar 액션
              :class ["px-5" "py-2" "bg-blue-600" "text-white" "text-sm" "font-bold"
                      "rounded-xl" "transition-all" "hover:bg-blue-700"]}
     "+ Create New Key"]]

   ;; Table Body
   [:div {:class ["overflow-x-auto"]}
    [:table {:class ["w-full" "text-left" "border-collapse"]}
     [:thead
      [:tr {:class ["bg-gray-50/50"]}
       [:th {:class ["px-8" "py-4" "text-xs" "font-bold" "text-gray-400" "uppercase" "tracking-wider"]} "Name"]
       [:th {:class ["px-8" "py-4" "text-xs" "font-bold" "text-gray-400" "uppercase" "tracking-wider"]} "Key ID"]
       [:th {:class ["px-8" "py-4" "text-xs" "font-bold" "text-gray-400" "uppercase" "tracking-wider"]} "Created"]
       [:th {:class ["px-8" "py-4" "text-xs" "font-bold" "text-gray-400" "uppercase" "tracking-wider"]} "Status"]]]
     [:tbody
      (if (empty? api-keys)
        [:tr [:td {:colspan "4" :class ["p-8" "text-center" "text-gray-400"]} "No keys found."]]
        (for [k api-keys]
          (api-key-row k)))]]]])

;; --- Main Assembly ---

(defn dashboard-content [user api-keys]
  [:main {:class ["max-w-6xl" "mx-auto" "px-6" "py-12"]}
   (if-not user
     [:div {:class ["py-20" "text-center"]} "Verifying access..."]
     (list
      (console-header user)
      (api-keys-table api-keys)))])

(defn handler [req]
  (let [client-id (get-in req [:session :identity])
        mock-user {:full_name "Test User" :email "test@bitem.com"}
        mock-keys [{:name "Default Key" :key_id "trid_live_123" :created_at "2026-01-17"}]]
    (layout/base "Console | TRID Precheck"
                 (dashboard-content mock-user mock-keys)
                 client-id)))
