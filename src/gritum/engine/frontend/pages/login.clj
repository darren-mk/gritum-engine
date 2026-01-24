(ns gritum.engine.frontend.pages.login
  (:require
   [gritum.engine.frontend.layout :as l]))

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
   [:input {:type "email"
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
   [:input {:type "password"
            :name "password"
            :data-bind "password"
            :class ["w-full" "px-4" "py-3" "rounded-xl" "border" "border-gray-200"
                    "focus:ring-2" "focus:ring-blue-500" "focus:border-transparent"
                    "outline-none" "transition-all"]
            :placeholder "••••••••"
            :required true}]])

(def submit-button
  [:button
   {:type "submit"
    :data-attr-disabled "$$loading"
    :class ["w-full" "py-3" "px-6" "text-white" "font-bold" "bg-blue-600" "rounded-xl"
            "hover:bg-blue-700" "transform" "active:scale-[0.98]" "transition-all"
            "disabled:opacity-50" "disabled:cursor-not-allowed" "shadow-lg" "shadow-blue-200"]}
   [:span {:data-show "!$$loading"} "Sign In"]
   [:span {:data-show "$$loading"} "Verifying..."]])

(def content
  [:div {:class ["flex" "items-center" "justify-center"
                 "min-h-[calc(100vh-64px)]"]}
   [:div {:id "login-container"
          :class ["w-full" "max-w-md" "p-8" "bg-white" "rounded-2xl"
                  "shadow-xl" "border" "border-gray-100"]
          :data-signals "{email: '', password: '', loading: false}"}
    login-header
    [:form {:class ["space-y-6"]
            ;; Datastar: 전송 시 loading을 true로 바꾸고 POST 요청
            :data-on-submit (str "$$loading = true; "
                                 "@post('/api/dashboard/login')")}
     ;; 에러 메시지가 들어올 자리 (서버에서 에러 시 이 부분을 교체해서 내려줌)
     [:div#error-message]
     email-field
     password-field
     submit-button]]])

(defn handler [_]
  (l/base "Login | TRID Check" content))
