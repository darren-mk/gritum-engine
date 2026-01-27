(ns gritum.engine.frontend.pages.login
  (:require
   [gritum.engine.frontend.layout :as l]
   [gritum.engine.frontend.signal :as sg]))

(def is-processing-k
  :isprocessing)

(def login-header
  [:div {:class ["mb-8"]}
   [:h2 {:class ["text-3xl" "font-extrabold" "text-gray-900" "mb-2"]}
    "Welcome Back"]
   [:p {:class ["text-gray-500"]}
    "Login to manage your Bitem Labs API keys"]])

(def email-field
  [:div
   [:label {:class ["block" "text-sm" "font-semibold" "text-gray-700" "mb-2"]}
    "Email Address"]
   [:input {:type :email
            :name "email"
            :data-bind "email"
            :class ["w-full" "px-4" "py-3" "rounded-xl" "border" "border-gray-200"
                    "focus:ring-2" "focus:ring-blue-500" "focus:border-transparent"
                    "outline-none" "transition-all"]
            :placeholder "name@company.com"
            :required true}]])

(def password-field
  [:div
   [:label {:class ["block" "text-sm" "font-semibold" "text-gray-700" "mb-2"]}
    "Password"]
   [:input {:type :password
            :name "password"
            :data-bind "password"
            :class ["w-full" "px-4" "py-3" "rounded-xl" "border" "border-gray-200"
                    "focus:ring-2" "focus:ring-blue-500" "focus:border-transparent"
                    "outline-none" "transition-all"]
            :placeholder "••••••••"
            :required true}]])

(def submit-button
  [:button
   {:type :submit
    :data-attr-disabled (sg/cite is-processing-k)
    :class ["w-full" "py-3" "px-6" "text-white" "font-bold" "bg-blue-600" "rounded-xl"
            "hover:bg-blue-700" "transform" "active:scale-[0.98]" "transition-all"
            "disabled:opacity-50" "disabled:cursor-not-allowed" "shadow-lg" "shadow-blue-200"]}
   [:span {:data-show (sg/cite-not is-processing-k)} "Sign In"]
   [:span {:data-show (sg/cite is-processing-k)} "Verifying..."]])

(def fail-msg-id
  "fail-message")

(def fail-msg
  [:div {:id fail-msg-id
         :class "p-3 mb-4 text-sm text-red-600 bg-red-50 rounded-lg"}
   "Invalid email or password. Please try again."])

;; login.clj (핵심 수정 부분)

(def content
  [:div {:class ["flex" "items-center" "justify-center"
                 "min-h-[calc(100vh-64px)]"]}
   [:div {:id "login-container"
          :class ["w-full" "max-w-md" "p-8" "bg-white"
                  "rounded-2xl" "shadow-xl" "border"
                  "border-gray-100"]
          ;; 1. 모든 시그널을 명시적으로 초기화 (매우 중요)
          :data-signals (format "{email: '', password: '', %s: false}"
                                (sg/bind is-processing-k))}
    login-header
    [:form {:class ["space-y-6"]
            ;; 2. __prevent 수식어를 붙여 브라우저의 기본 본능을 억제
            :data-on:submit (str (sg/assign is-processing-k true) ";"
                                 (sg/post "/hypermedia/login"))}
     [:div {:id fail-msg-id}]
     email-field
     password-field
     submit-button]]])

(defn handler [req]
  (l/base "Login | TRID Check"
          content (get-in req [:session :identity])))
