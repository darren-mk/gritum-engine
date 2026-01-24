(ns gritum.engine.frontend.pages.lab
  (:require
   [gritum.engine.frontend.layout :as l]))

;; --- UI Helpers ---

(defn section [title description content]
  [:section {:class ["mb-12" "p-10" "bg-white" "rounded-[40px]"
                     "border" "border-slate-100" "shadow-2xl"
                     "shadow-slate-200/40"]}
   [:div {:class ["mb-8"]}
    [:h2 {:class ["text-2xl" "font-black"
                  "text-slate-900" "tracking-tight"]}
     title]
    [:p {:class ["text-slate-500" "mt-2"
                 "text-sm" "leading-relaxed"]}
     description]]
   [:div {:class ["p-8" "bg-slate-50" "rounded-3xl"
                  "border" "border-slate-100" "inner-shadow-sm"]}
    content]])

;; --- Lab Items ---

(def data-bind-data-text
  (let [k :foo-bar]
    [:div {:class ["space-y-6"]}
     [:div
      [:label {:class ["block" "text-xs" "font-bold" "text-slate-400"
                       "uppercase" "tracking-widest" "mb-3"]}
       "Signal Input"]
      [:input {:data-bind (name k)
               :placeholder "Type to see magic..."
               :class ["w-full" "max-w-md" "px-5" "py-4" "rounded-2xl"
                       "border-0" "ring-1" "ring-slate-200"
                       "focus:ring-2" "focus:ring-blue-600" "bg-white"
                       "outline-none" "transition-all" "font-semibold"
                       "text-slate-700"]}]]

     [:div {:class ["pt-4" "border-t" "border-slate-200/60"]}
      [:label {:class ["block" "text-xs" "font-bold" "text-slate-400"
                       "uppercase" "tracking-widest" "mb-3"]}
       "Reactive Output"]
      [:h2 {:class ["text-4xl" "font-black" "text-blue-600"
                    "tracking-tighter" "break-all"]
            :data-text (str "$" (name k))}
       "System Ready..."]]]))

(def data-show
  (let [k :jack-rabbit]
    [:div {:class ["space-y-6"]}
     [:div
      [:label {:class ["block" "text-xs" "font-bold" "text-slate-400"
                       "uppercase" "tracking-widest" "mb-3"]}
       "Requirement: Type something to save"]
      [:input {:data-bind (name k)
               :placeholder "Enter your name..."
               :class ["w-full" "max-w-md" "px-5" "py-4" "rounded-2xl"
                       "border-0" "ring-1" "ring-slate-200" "focus:ring-2"
                       "focus:ring-blue-600" "bg-white" "outline-none"
                       "transition-all" "font-semibold"]}]]
     [:div {:class ["flex" "items-center" "gap-4"]}
      (comment "only shown when $foo-bar has value")
      [:button {:data-show (str "$" (name k))
                :style {:display "none"}
                :class ["px-8" "py-3" "bg-green-600" "text-white"
                        "font-bold" "rounded-xl" "hover:bg-green-700"
                        "shadow-lg" "shadow-green-200" "transition-all"]}
       "Save Changes"]
      (comment "only shown when $foo-bar has no value")
      [:p {:data-show (str "!" "$" (name k))
           :class ["text-sm" "text-slate-400" "italic"]}
       "The save button is hidden until you type."]]]))

(def data-class
  (let [k :mini-donut]
    [:div {:class ["space-y-8"]}
     [:div
      [:p {:class ["text-xs" "text-slate-400" "mb-2"]}
       "Type 'blue' to trigger styles"]
      [:input {:data-bind (name k)
               :placeholder "Try typing 'blue'..."
               :class ["w-full" "max-w-md" "px-5" "py-3" "rounded-xl"
                       "border" "border-slate-200" "outline-none"]}]]
     [:div {:class ["space-y-4"]}
      [:h3 {:class ["text-sm" "font-bold" "text-slate-500"]}
       "1. Single Class Toggle"]
      [:button {:class ["px-6" "py-2" "border-2" "rounded-xl"
                        "transition-all" "duration-500"]
                ;; $fooBar가 'blue'일 때만 text-blue-600 및 border-blue-600 클래스 추가
                "data-class:text-blue-600"
                (str "$" (name k) " == 'blue'")
                "data-class:border-blue-600"
                (str "$" (name k) " == 'blue'")}
       "Color Me Blue"]]
     [:div {:class ["space-y-4" "mt-8"]}
      [:h3 {:class ["text-sm" "font-bold" "text-slate-500"]}
       "2. Multi-Class Object"]
      [:div {:class ["p-6" "rounded-2xl" "bg-white" "border"
                     "transition-all" "duration-700"]
             ;; 객체 구문: {클래스명: 조건}
             ;; 하이픈이 있는 클래스는 반드시 따옴표로 감싸야 합니다.
             "data-class" (str "{'bg-blue-600': $" (name k) " == 'blue', "
                               "'text-white': $" (name k) " == 'blue', "
                               "'scale-110': $" (name k) " == 'blue'}")}
       "I react to the word 'blue'"]]]))

(def data-attr
  (let [k :egg]
    [:div {:class ["space-y-8"]}
     [:div
      [:label {:class ["block" "text-xs" "font-bold" "text-slate-400"
                       "uppercase" "tracking-widest" "mb-3"]}
       "Input (Signal: $egg)"]
      [:input {:data-bind (name k)
               :placeholder "Type something to enable the button..."
               :class ["w-full" "max-w-md" "px-5" "py-4" "rounded-2xl" "border-0"
                       "ring-1" "ring-slate-200" "focus:ring-2" "focus:ring-blue-600"
                       "bg-white" "outline-none" "transition-all" "font-semibold"]}]]
     [:div {:class ["flex" "flex-col" "gap-3"]}
      [:button {:data-attr:disabled (str "$" (name k) " == ''")
                :class ["px-8" "py-3" "bg-blue-600" "text-white"
                        "font-bold" "rounded-xl"
                        "hover:bg-blue-700" "transition-all"
                        "disabled:bg-slate-200" "disabled:text-slate-400"
                        "disabled:cursor-not-allowed"]}
       "Save Changes"]
      [:p {:class ["text-[10px]" "text-slate-400" "uppercase" "font-bold"]}
       "Status: "
       [:span {:data-show (str "$" (name k) " == ''")
               :class "text-red-400"} "Locked"]
       [:span {:data-show (str "$" (name k) " != ''")
               :class "text-green-500"} "Ready to Save"]]]]))

(def data-on
  (let [k :fluffy-souffle]
    [:div {:class ["space-y-2"]}
     [:div {:class ["space-y-4"]}
      [:label {:class ["block" "text-xs" "font-bold"
                       "text-slate-400" "uppercase"
                       "tracking-widest" "mb-3"]}
       "Interactive Input"]
      [:div {:class ["relative" "max-w-md"]}
       [:input {:data-bind (name k)
                :placeholder "Type something and click reset..."
                :class ["w-full" "px-5" "py-4" "rounded-2xl" "border-0"
                        "ring-1" "ring-slate-200" "focus:ring-2"
                        "focus:ring-blue-600" "bg-white" "outline-none"
                        "transition-all" "font-semibold"]}]
       [:div {:data-show (str "$" (name k))
              :class ["absolute" "right-4" "top-1/2" "-translate-y-1/2"]}
        [:span {:class ["flex" "h-2" "w-2" "rounded-full"
                        "bg-green-500" "animate-pulse"]}]]]]
     [:div {:class ["flex" "items-center" "gap-3" "mt-6"]}
      [:button {:data-on:click (str "$" (name k) " = ''")
                :class ["px-6" "py-3" "bg-slate-900" "text-white" "text-sm"
                        "font-bold" "rounded-xl" "hover:bg-black"
                        "active:scale-95" "transition-all"
                        "disabled:opacity-50" "disabled:cursor-not-allowed"]
                :data-attr:disabled (str "$" (name k) " == ''")}
       "Reset Value"]]]))

(def stream-basic
  [:div {:class ["flex" "flex-col" "gap-6"]}
   [:div {:class ["flex" "items-center" "gap-4"]}
    [:button {:data-on:click "@get('/hypermedia/hello')"
              :class ["px-6" "py-3" "bg-slate-900" "text-white"
                      "rounded-xl" "hover:bg-black"]}
     "Get Greeting from Server"]
    [:button {:data-on:click "$helloMsg = 'Waiting...'"
              :class "text-xs text-slate-400 underline"}
     "Reset"]]
   [:div {:id "hal"
          :class ["p-6" "bg-slate-50" "rounded-2xl" "border"
                  "border-dashed" "border-slate-200" "text-center"]}
    "Click the button to talk to the server."]])

(def stream-basic-resp-1
  [:div {:id "hal"
         :class ["text-center" "font-bold" "text-xl" "text-slate-700"]}
   "Abc"])

(def stream-basic-resp-2
  [:div {:id "hal"
         :class ["text-center" "font-bold" "text-xl" "text-slate-700"]}
   "Def"])

;; --- Main Page Assembly ---

(def header
  [:header {:class ["mb-20" "text-left" "border-l-8"
                    "border-blue-600" "pl-8"]}
   [:span {:class ["text-sm" "font-bold" "text-blue-600"
                   "uppercase" "tracking-[0.3em]"]}
    "Internal Testing Environment"]
   [:h1 {:class ["mt-4" "text-6xl" "font-black"
                 "text-slate-900" "tracking-tighter"]}
    "Datastar Lab"]
   [:p {:class ["mt-6" "text-xl" "text-slate-500"
                "max-w-3xl" "leading-relaxed"]}
    "반응형 시그널과 하이퍼미디어 상호작용을 위한 실험실입니다.
     모든 컴포넌트는 단일 달러($) 접두사 문법을 준수합니다."]])

(def content
  [:main {:class ["max-w-5xl" "mx-auto" "px-6" "py-24"]}
   header
   (section "Reactive Signals ($)"
            "data-bind로 시그널 값을 변경하고, $prefix를 사용한 data-text 표현식으로 값을 출력합니다."
            data-bind-data-text)
   (section "Conditional Visibility (data-show)"
            "표현식이 true일 때만 요소를 표시합니다. 초기 깜빡임을 방지하기 위해 display: none 스타일을 권장합니다."
            data-show)
   (section "Dynamic Classes (data-class)"
            "시그널 조건에 따라 클래스를 동적으로 추가/제거합니다. 단일 속성 방식과 객체(Object) 방식을 모두 지원합니다."
            data-class)
   (section "Attribute Binding (data-attr)"
            "HTML 요소의 모든 속성을 시그널에 바인딩합니다. 대표적으로 버튼의 비활성화(disabled) 상태 제어에 사용됩니다."
            data-attr)
   (section "Event Listeners (data-on)"
            "클릭, 키보드 입력 등 브라우저 이벤트를 가로채 시그널을 수정하거나 자바스크립트를 실행합니다."
            data-on)
   (section "Simple Server Request (@get)"
            "버튼을 누르면 서버가 SSE 포맷으로 HTML 조각을 보내고, 브라우저는 해당 ID의 요소를 즉시 교체합니다."
            stream-basic)])

(defn handler [_]
  (l/base "Datastar Lab | TRID Check" content))
