(ns gritum.engine.frontend.pages.signup
  (:require [gritum.engine.frontend.layout :as layout]))

;; --- UI Partials ---

#_(def signup-header
    [:div {:class ["sm:mx-auto" "sm:w-full" "sm:max-w-md" "text-center"]}
     [:h2 {:class ["mt-6" "text-3xl" "font-black" "tracking-tighter" "text-gray-900" "uppercase"]}
      "Start your free trial"]
     [:p {:class ["mt-2" "text-sm" "text-gray-600"]}
      "Already have an account? "
      [:a {:href "/login" :class ["font-bold" "text-blue-600" "hover:text-blue-500"]}
       "Log in here"]]])

(defn input-group [label-text type-name placeholder-text bind-key]
  [:div
   [:label {:class ["block" "text-xs" "font-bold" "uppercase" "tracking-wider" "text-gray-500" "mb-2"]}
    label-text]
   [:input {:type (name type-name)
            :placeholder placeholder-text
            :required true
            :data-bind bind-key
            :class ["block" "w-full" "rounded-xl" "border-0" "px-4" "py-3" "text-gray-900"
                    "shadow-sm" "ring-1" "ring-inset" "ring-gray-200"
                    "focus:ring-2" "focus:ring-inset" "focus:ring-blue-600"
                    "transition-all" "font-medium" "sm:text-sm"]}]])

(def terms-checkbox
  [:div {:class ["flex" "items-center"]}
   [:input {:id "terms" :type "checkbox"
            :data-bind "termsAccepted"
            :class ["h-4" "w-4" "rounded" "border-gray-300" "text-blue-600"
                    "focus:ring-blue-600" "cursor-pointer"]}]
   [:label {:for "terms" :class ["ml-3" "block" "text-sm" "text-gray-500"]}
    "I agree to the "
    [:a {:href "#" :class ["font-bold" "text-gray-900" "hover:underline"]} "Terms"]
    " and "
    [:a {:href "#" :class ["font-bold" "text-gray-900" "hover:underline"]} "Privacy Policy"] "."]])

(def error-display
  [:div {:data-show "$$error"
         :class ["text-red-500" "text-xs" "font-bold" "bg-red-50" "px-4" "py-3"
                 "rounded-xl" "border" "border-red-100" "animate-pulse"]}
   [:span {:data-text "$$error"}]])

;; --- Main Page Assembly ---

(defn submit-button []
  [:button
   {:type "submit"
    :data-attr-disabled "$$isLoading"
    ;; 모든 클래스를 하나의 벡터로 합쳤습니다.
    :class ["flex" "w-full" "justify-center" "items-center"
            "rounded-2xl" "px-4" "py-4" "text-sm" "font-bold"
            "text-white" "transition-all" "active:scale-[0.98]"
            "bg-slate-900" "hover:bg-black"
            "shadow-xl" "shadow-slate-200"
            "disabled:bg-gray-400" "disabled:cursor-not-allowed"]}
   [:span {:data-show "!$$isLoading"} "Create Account"]
   [:span {:data-show "$$isLoading" :class ["flex" "items-center" "gap-2"]}
    ;; 로딩 시 간단한 텍스트 처리
    "Creating account..."]])

;; 페이지 전체 구조 재정렬
(def content
  [:div {:class ["min-h-[calc(100vh-64px)]" "flex" "flex-col" "justify-center"
                 "bg-white" "py-12" "px-6" "font-sans"]
         :data-signals "{fullName: '', email: '', password: '', termsAccepted: false, error: '', isLoading: false}"}

   ;; 상단 타이틀 섹션 (image_356765.png 디자인 반영)
   [:div {:class ["sm:mx-auto" "sm:w-full" "sm:max-w-md" "text-center" "mb-10"]}
    [:h2 {:class ["text-4xl" "font-black" "tracking-tight" "text-gray-900" "uppercase"]}
     "Start your free trial"]
    [:p {:class ["mt-3" "text-gray-600"]}
     "Already have an account? "
     [:a {:href "/login" :class ["font-bold" "text-blue-600" "hover:text-blue-500"]}
      "Log in here"]]]

   [:div {:class ["sm:mx-auto" "sm:w-full" "sm:max-w-[480px]"]}
    [:div {:class ["bg-white" "px-10" "py-12" "border" "border-slate-100" "rounded-[32px]"
                   "shadow-2xl" "shadow-slate-200/60"]}

     [:form {:class ["space-y-7"]
             :data-on-submit (str "if (!$$termsAccepted) { $$error = 'Please agree to the Terms.'; return; }; "
                                  "$$error = ''; $$isLoading = true; "
                                  "@post('/api/dashboard/signup')")}

      ;; Input Groups
      (input-group "Full Name" "text" "John Doe" "fullName")
      (input-group "Email address" "email" "john@company.com" "email")
      (input-group "Password" "password" "••••••••" "password")
      terms-checkbox
      error-display
      (submit-button)]]]])

(defn handler [req]
  (layout/base "Create Account | TRID Check"
               content (get-in req [:session :identity])))