(ns gritum.engine.frontend.components.navbar
  (:require
   [gritum.engine.frontend.signal :as sg]))

(def burger-open-k
  :is-navbar-burger-open)

(def logo
  [:a {:href "/"
       :class ["text-xl" "font-black"
               "text-gray-900"
               "tracking-tighter"]}
   "TRID Check"])

(defn nav-link [href label]
  [:a {:href href
       :class ["text-md" "font-semibold"
               "text-gray-600"
               "hover:text-gray-900"
               "transition-colors"]}
   label])

(def burger-icon
  [:svg {:data-show (sg/cite-not burger-open-k)
         :class ["w-6" "h-6"]
         :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
   [:path {:stroke-linecap "round" :stroke-linejoin "round"
           :stroke-width "2" :d "M4 6h16M4 12h16m-7 6h7"}]])

(def close-icon
  "Only shown when burger open is true"
  [:svg {:data-show (sg/cite burger-open-k)
         :class ["w-6" "h-6"]
         :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
   [:path {:stroke-linecap "round" :stroke-linejoin "round"
           :stroke-width "2" :d "M6 18L18 6M6 6l12 12"}]])

(defn desktop-auth [client-id]
  [:div {:class ["hidden" "md:flex" "items-center" "gap-4"]}
   (if client-id
     (list
      [:a {:href "/dashboard"
           :class ["text-sm" "font-semibold" "text-gray-700" "hover:text-gray-900"]}
       "Console"]
      [:button {:data-on:click (sg/post "/hypermedia/logout")
                :class ["px-4" "py-2" "border" "border-gray-200" "text-sm" "font-bold"
                        "rounded-lg" "hover:bg-gray-50" "transition-all"]}
       "Log out"])
     (list
      [:a {:href "/login"
           :class ["text-sm" "font-semibold" "text-gray-700" "hover:text-gray-900"]}
       "Log in"]
      [:a {:href "/signup"
           :class ["px-4" "py-2" "bg-gray-900" "text-white" "text-sm" "font-bold"
                   "rounded-lg" "hover:bg-gray-800" "transition-all"]}
       "Get Started"]))])

(defn mobile-menu [client-id]
  [:div {:data-show (sg/cite burger-open-k)
         :class ["md:hidden" "bg-white" "border-t" "border-gray-50"
                 "px-6" "py-8" "space-y-6" "shadow-xl"]}
   [:div {:class ["flex" "flex-col" "gap-6"]}
    [:a {:href "/pricing" :class ["text-lg" "font-bold" "text-gray-900"]} "Pricing"]
    [:a {:href "/docs" :class ["text-lg" "font-bold" "text-gray-900"]} "Docs"]]
   [:hr {:class ["border-gray-100"]}]
   [:div {:class ["flex" "flex-col" "gap-4"]}
    (if client-id
      (list
       [:a {:href "/dashboard"
            :class ["text-center" "py-3" "text-sm" "font-bold" "text-gray-700"
                    "border" "border-gray-200" "rounded-xl"]}
        "Console"]
       [:button {:data-on:click (sg/post "/hypermedia/logout")
                 :class ["text-center" "py-3" "text-sm" "font-bold"
                         "text-white" "bg-red-500" "rounded-xl"]}
        "Log out"])
      (list
       [:a {:href "/login"
            :class ["text-center" "py-3" "text-sm" "font-bold" "text-gray-700"
                    "border" "border-gray-200" "rounded-xl"]}
        "Log in"]
       [:a {:href "/signup"
            :class ["text-center" "py-3" "text-sm" "font-bold" "text-white"
                    "bg-gray-900" "rounded-xl"]}
        "Get Started"]))]])

(defn basic [client-id]
  [:nav {:class ["sticky" "top-0" "z-50" "w-full" "bg-white/80"
                 "backdrop-blur-md" "border-b" "border-gray-50"]}
   [:div {:class ["max-w-7xl" "mx-auto" "px-6" "h-16"
                  "flex" "items-center" "justify-between"]}
    logo
    [:div {:class ["hidden" "md:flex" "items-center" "gap-8"]}
     (nav-link "/pricing" "Pricing")
     (nav-link "/docs" "Docs")]
    [:div {:class ["flex" "items-center" "gap-4"]}
     (desktop-auth client-id)
     [:button {:data-on:click (sg/toggle burger-open-k)
               :class ["md:hidden" "p-2" "text-gray-600"
                       "hover:text-gray-900" "focus:outline-none"]}
      burger-icon close-icon]]]
   (mobile-menu client-id)])
