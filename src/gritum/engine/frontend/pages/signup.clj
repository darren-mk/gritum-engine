(ns gritum.engine.frontend.pages.signup
  (:require
   [clojure.string :as str]
   [gritum.engine.frontend.icons :as icons]
   [gritum.engine.frontend.layout :as layout]
   [gritum.engine.frontend.signal :as sg]))

(def full-name-k :full_name)
(def email-k :email)
(def password-k :password)
(def terms-accepted-k :terms_accepted)
(def is-processing-k :is_processing)
(def fail-msg-id :signup_fail_msg)

(def init-state
  {full-name-k ""
   email-k ""
   password-k ""
   terms-accepted-k false
   is-processing-k false})

(def header
  [:div {:class "sm:mx-auto sm:w-full sm:max-w-md text-center mb-10"}
   [:h2 {:class "text-4xl font-black tracking-tight text-gray-900 uppercase"}
    "Start your free trial"]
   [:p {:class "mt-3 text-gray-600"} "Already have an account? "
    [:a {:href "/login" :class "font-bold text-blue-600 hover:text-blue-500"} "Log in here"]]])

(defn input-group [label-text tp placeholder bind-key]
  [:div
   [:label {:class "block text-xs font-bold uppercase
                    tracking-wider text-gray-500 mb-2"}
    label-text]
   [:input {:type tp
            :placeholder placeholder
            :required true
            :data-bind (sg/bind bind-key)
            :class "block w-full rounded-xl border-0 px-4 py-3 text-gray-900
                    shadow-sm ring-1 ring-inset ring-gray-200 focus:ring-2
                    focus:ring-inset focus:ring-blue-600 transition-all
                    font-medium sm:text-sm"}]])

(def terms-checkbox
  [:div {:class "flex items-center"}
   [:input {:id "terms" :type :checkbox
            :required true
            :data-bind (sg/bind terms-accepted-k)
            :class "h-4 w-4 rounded border-gray-300 text-blue-600
                    focus:ring-blue-600 cursor-pointer"}]
   [:label {:for "terms" :class "ml-3 block text-sm text-gray-500"}
    "I agree to the "
    [:a {:href "#" ; TODO
         :class "font-bold text-gray-900 hover:underline"}
     "Terms"] " and "
    [:a {:href "#" ; TODO
         :class "font-bold text-gray-900 hover:underline"}
     "Privacy Policy"] "."]])

(def submit-button
  [:button
   {:type :submit
    :class "flex w-full justify-center items-center rounded-2xl
              px-4 py-4 text-sm font-bold text-white transition-all
              active:scale-[0.98] bg-slate-900 hover:bg-black shadow-xl
              shadow-slate-200 disabled:bg-gray-400
              disabled:cursor-not-allowed"}
   [:span {:data-show (sg/cite-not is-processing-k)}
    "Create Account"]
   [:span {:data-show (sg/cite is-processing-k)
           :class ["flex" "items-center" "gap-2"]}
    ;; TODO: spinner
    "Creating account..."]])

(def on-submit-sg
  (sg/combine
   (sg/assign is-processing-k true)
   (sg/post "/hypermedia/signup")))

(def form
  [:form {:class "space-y-7"
          :data-on:submit on-submit-sg}
   (input-group "Full Name" :text "John Doe" full-name-k)
   (input-group "Email address" :email "john@company.com" email-k)
   (input-group "Password" :password "••••••••" password-k)
   terms-checkbox
   [:div {:id fail-msg-id} [:span [:h1 "..."]]]
   submit-button])

(def content
  [:div {:class "min-h-[calc(100vh-64px)] flex flex-col
                 justify-center bg-white py-12 px-6 font-sans"
         :data-signals (sg/->json-obj init-state)}
   header
   [:div {:class "sm:mx-auto sm:w-full sm:max-w-[480px]"}
    [:div {:class "bg-white px-10 py-12 border border-slate-100
                   rounded-[32px] shadow-2xl shadow-slate-200/60"}
     form]]])

(defn fail-msg [violations]
  [:div {:id fail-msg-id
         :class "mb-6 p-4 rounded-2xl bg-red-50 border border-red-100 flex flex-col gap-2"}
   (if (seq violations)
     (for [[field msgs] violations]
       (for [msg msgs]
         [:div {:key (str (name field) "-" msg)
                :class "flex items-start gap-2.5 text-red-700"}
          icons/warning
          [:p {:class "text-sm font-medium leading-tight"}
           [:span {:class "font-bold uppercase text-[10px] tracking-wider mr-2 opacity-70"}
            (str/replace (name field) "_" " ")]
           msg]]))
     [:p {:class "text-sm text-red-600 font-medium"}
      "Something went wrong. Please try again."])])

(defn handler [req]
  (layout/base
   "Create Account | TRID Check"
   content (get-in req [:session :identity])))
