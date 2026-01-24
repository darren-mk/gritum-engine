(ns gritum.engine.frontend.pages.docs
  (:require
   [gritum.engine.frontend.layout :as layout]))

(def intro
  [:section#introduction {:class ["mb-20"]}
   [:h1 {:class ["text-4xl" "font-black" "mb-6" "tracking-tight"]}
    "Introduction"]
   [:p {:class ["text-lg" "text-slate-600" "leading-relaxed" "mb-6"]}
    (str "TRID Check is a specialized validation engine built for lenders, "
         "fintechs, and mortgage professionals. Our API automates the "
         "complex logic required to verify TILA-RESPA Integrated Disclosures (TRID), "
         "ensuring your loan documents meet regulatory standards in real-time.")]
   [:ul {:class ["space-y-3" "text-slate-700"]}
    [:li {:class ["flex" "items-start" "gap-2"]}
     [:span {:class ["text-blue-600" "font-bold"]} "•"]
     [:span [:strong "Automated Compliance: "]
      "Replace manual checklists with a sub-second validation engine."]]
    [:li {:class ["flex" "items-start" "gap-2"]}
     [:span {:class ["text-blue-600" "font-bold"]} "•"]
     [:span [:strong "Discrepancy Reporting: "]
      "Get granular feedback on specific data points that fail compliance."]]]])

(def quickstart
  [:section#quickstart {:class ["mb-20"]}
   [:h2 {:class ["text-2xl" "font-bold" "mb-6" "tracking-tight"]}
    "Quickstart"]
   [:div {:class ["space-y-6" "text-slate-600"]}
    [:p [:strong "Step 1: Get your API Key. "]
     "Sign up at the Console and generate your unique secret key."]
    [:p [:strong "Step 2: Make your first request."]]
    [:div {:class ["bg-[#0f172a]" "rounded-xl" "p-6" "overflow-x-auto" "shadow-sm"]}
     [:pre {:class ["text-blue-300" "text-sm" "font-mono" "leading-relaxed"]}
      (str "curl -X POST https://api.tridcheck.com/v1/validate \\\n"
           "  -H \"Authorization: Bearer YOUR_API_KEY\" \\\n"
           "  -H \"Content-Type: application/json\" \\\n"
           "  -d '{\n"
           "    \"loan_amount\": 350000,\n"
           "    \"apr\": 4.5,\n"
           "    \"fees\": []\n"
           "  }'")]]]])

(def authentication
  [:section#authentication {:class ["mb-20" "pt-12" "border-t" "border-slate-100"]}
   [:h2 {:class ["text-2xl" "font-bold" "mb-6" "tracking-tight"]}
    "Authentication"]
   [:p {:class ["text-slate-600" "leading-relaxed"]}
    (str "All requests to the TRID Check API must be authenticated "
         "using a Bearer Token in the ")
    [:code "Authorization"] " header."]])

(def reference
  [:section#api-reference {:class ["mb-20" "pt-12" "border-t" "border-slate-100"]}
   [:h2 {:class ["text-2xl" "font-bold" "mb-6" "tracking-tight"]}
    "API Reference"]
   [:h3 {:class ["text-sm" "font-bold" "text-blue-600" "uppercase" "tracking-widest" "mb-4"]}
    "POST /v1/validate"]
   [:p {:class ["text-slate-600" "leading-relaxed" "mb-8"]}
    (str "Performs a comprehensive check on a "
         "set of disclosure data.")]
   [:div {:class ["space-y-4" "border-t" "border-slate-50"]}
    [:div {:class ["flex" "justify-between" "py-4" "border-b" "border-slate-50"]}
     [:code {:class ["text-sm" "font-bold" "text-pink-600"]} "loan_info"]
     [:span {:class ["text-sm" "text-slate-400" "italic"]} "Object (Required)"]]
    [:div {:class ["flex" "justify-between" "py-4" "border-b" "border-slate-50"]}
     [:code {:class ["text-sm" "font-bold" "text-pink-600"]} "disclosure_date"]
     [:span {:class ["text-sm" "text-slate-400" "italic"]} "String (ISO 8601)"]]]])

(def footer
  [:footer {:class ["mt-32" "pt-12" "border-t" "border-slate-100"
                    "flex" "justify-between" "items-center"
                    "text-[12px]" "text-slate-400" "font-medium"]}
   [:p "© 2026 Bitem Labs Inc."]
   [:div {:class ["flex" "gap-6"]}
    [:a {:href "#" :class ["hover:text-slate-900"]} "Support"]
    [:a {:href "#" :class ["hover:text-slate-900"]} "Privacy"]]])

(def content
  [:div {:class "bg-white min-h-screen pb-24 text-slate-900"}
   [:main {:class "max-w-4xl mx-auto px-6 pt-16"}
    intro quickstart authentication reference footer]])

(defn handler [_]
  (layout/base
   "Documentation | TRID Check"
   content))
