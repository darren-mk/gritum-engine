(ns gritum.engine.frontend.pages.home
  (:require [gritum.engine.frontend.layout :as layout]))

(def hero-section
  [:header {:class ["max-w-5xl" "mx-auto" "px-6" "pt-24" "pb-32" "text-center"]}
   [:span {:class ["inline-block" "px-4" "py-1.5" "mb-6" "text-xs" "font-bold"
                   "tracking-widest" "uppercase" "bg-blue-50" "text-blue-700" "rounded-full"]}
    "Bitem Labs Product"]
   [:h1 {:class ["text-7xl" "md:text-8xl" "font-black" "tracking-tighter" "mb-4" "leading-tight"]}
    "TRID Precheck"]
   [:p {:class ["text-2xl" "md:text-3xl" "font-bold" "text-blue-600" "mb-12" "tracking-tight"]}
    "Catch TRID violations before they happen."]
   [:p {:class ["text-xl" "text-gray-500" "max-w-2xl" "mx-auto" "mb-12" "leading-relaxed"]}
    (str "The high-performance pre-check API that identifies tolerance errors "
         "and compliance risks in real-time. Stop wasting time on re-disclosures.")]
   [:div {:class ["flex" "flex-col" "sm:flex-row" "justify-center" "gap-4"]}
    [:a {:href "/dashboard"
         :class ["px-8" "py-4" "bg-blue-600" "text-white" "font-bold" "rounded-2xl"
                 "shadow-xl" "shadow-blue-200" "hover:bg-blue-700" "hover:-translate-y-1" "transition-all"]}
     "Start Pre-checking for Free"]
    [:button {:class ["px-8" "py-4" "bg-white" "border" "border-gray-200" "text-gray-700"
                      "font-bold" "rounded-2xl" "hover:bg-gray-50" "transition-all"]}
     "View API Reference"]]])

(def philosophy-section
  [:section {:class ["bg-gray-50" "py-24"]}
   [:div {:class ["max-w-7xl" "mx-auto" "px-6" "grid" "md:grid-cols-2" "gap-16" "items-center"]}
    [:div
     [:h2 {:class ["text-3xl" "font-black" "mb-6"]}
      "Built as a Guardrail," [:br] "Not a Replacement."]
     [:p {:class ["text-gray-600" "leading-relaxed" "mb-6"]}
      (str "TRID Precheck is an automated filter designed to sit between your data entry "
           "and final disclosure. We help you catch 99% of common errors instantly, "
           "well before the legal audit phase.")]
     [:ul {:class ["space-y-4"]}
      (for [item ["Reduce Re-disclosure Costs"
                  "Instant Tolerance Calculations (LE vs CD)"
                  "Fail Fast: Identify risks at the UI layer"]]
        [:li {:class ["flex" "items-start" "gap-3"]}
         [:div {:class ["mt-1" "bg-green-500" "rounded-full" "p-1" "text-white"]} "✓"]
         [:p {:class ["text-sm" "font-semibold"]} item]])]]
    [:div {:class ["bg-gray-900" "rounded-3xl" "p-8" "shadow-2xl" "overflow-hidden"]}
     [:div {:class ["flex" "gap-2" "mb-6"]}
      [:div {:class ["w-3" "h-3" "rounded-full" "bg-red-500"]}]
      [:div {:class ["w-3" "h-3" "rounded-full" "bg-yellow-500"]}]
      [:div {:class ["w-3" "h-3" "rounded-full" "bg-green-500"]}]]
     [:pre {:class ["text-blue-300" "font-mono" "text-sm" "overflow-x-auto"]}
      [:code
       (str "// Type-safe TRID verification\n"
            "import { checkTrid } from \"trid-precheck\";\n\n"
            "const result = await checkTrid({\n"
            "  loan_estimate: { total_closing_costs: 5000 },\n"
            "  closing_disclosure: { total_closing_costs: 5600 }\n"
            "});\n\n"
            "if (result.status === \"warning\") {\n"
            "  showWarning(result.message);\n"
            "}")]]]]])

(def developer-section
  [:section {:class ["max-w-7xl" "mx-auto" "px-6" "py-32" "text-center"]}
   [:h2 {:class ["text-4xl" "font-black" "mb-16"]} "Engineered for Data Integrity"]
   [:div {:class ["grid" "md:grid-cols-3" "gap-12"]}
    [:div {:class ["p-8" "border" "border-gray-100" "rounded-3xl"]}
     [:div {:class ["text-blue-600" "mb-4" "text-2xl"]} "λ"]
     [:h3 {:class ["font-bold" "text-lg" "mb-2" "text-gray-900"]} "Functional Engine"]
     [:p {:class ["text-sm" "text-gray-500"]}
      "Built on principles of immutability and data-oriented programming for predictable results."]]
    [:div {:class ["p-8" "border" "border-gray-100" "rounded-3xl"]}
     [:div {:class ["text-blue-600" "mb-4" "text-2xl"]} "🛡️"]
     [:h3 {:class ["font-bold" "text-lg" "mb-2" "text-gray-900"]} "Type-Safe Contract"]
     [:p {:class ["text-sm" "text-gray-500"]}
      "Strict OpenAPI 3.0 specs with end-to-end type safety for your frontend and backend."]]
    [:div {:class ["p-8" "border" "border-gray-100" "rounded-3xl"]}
     [:div {:class ["text-blue-600" "mb-4" "text-2xl"]} "🔗"]
     [:h3 {:class ["font-bold" "text-lg" "mb-2" "text-gray-900"]} "Instant Integration"]
     [:p {:class ["text-sm" "text-gray-500"]}
      "Simple REST endpoints that integrate seamlessly with any Loan Origination System (LOS)."]]]])

(def home-footer
  [:footer {:class ["bg-gray-900" "text-white" "py-20"]}
   [:div {:class ["max-w-7xl" "mx-auto" "px-6" "flex" "flex-col" "items-center"]}
    [:p {:class ["text-gray-400" "text-xs" "uppercase" "tracking-widest" "font-bold" "mb-8"]}
     "Provided by Bitem Labs LLC"]
    [:div {:class ["bg-gray-800" "border" "border-gray-700" "p-6" "rounded-2xl" "max-w-2xl" "text-center" "mb-12"]}
     [:p {:class ["text-xs" "text-gray-400" "leading-relaxed" "italic"]}
      (str "Disclaimer: TRID Precheck is an automated early-warning tool designed for preliminary data analysis. "
           "It does not constitute legal advice and is not a final compliance audit. "
           "Users are responsible for ensuring final legal compliance with TILA-RESPA regulations.")]]
    [:p {:class ["text-sm" "text-gray-500"]} "© 2026 Bitem Labs LLC. All rights reserved."]]])

(def content
  [:main {:class ["bg-white" "text-gray-900" "font-sans"]}
   hero-section
   philosophy-section
   developer-section
   home-footer])

(defn handler [req]
  (layout/base
   "TRID Precheck | Early-Warning Compliance API"
   content (get-in req [:session :identity])))
