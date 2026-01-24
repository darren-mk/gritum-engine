(ns gritum.engine.frontend.pages.pricing
  (:require [gritum.engine.frontend.layout :as layout]))

(def plans
  [{:name "Starter"
    :price "$0"
    :description "Perfect for developers testing the waters."
    :features ["100 validations / mo" "Basic discrepancy reports"
               "Community support" "1 API Key"]
    :cta "Start for Free"
    :highlight false}
   {:name "Professional"
    :price "$99"
    :description "For growing teams needing full automation."
    :features ["5,000 validations / mo" "Advanced compliance insights"
               "Priority email support" "5 API Keys" "Webhooks integration"]
    :cta "Get Started"
    :highlight true}
   {:name "Enterprise"
    :price "Custom"
    :description "High-volume solutions for institutions."
    :features ["Unlimited validations" "Dedicated account manager"
               "Custom SLA & Security" "Unlimited API Keys" "On-premise options"]
    :cta "Contact Sales"
    :highlight false}])

(def header-section
  [:header {:class ["max-w-3xl" "mx-auto" "text-center" "mb-16"]}
   [:h1 {:class ["text-4xl" "md:text-5xl" "font-black" "tracking-tight" "mb-6"]}
    "Simple, Transparent Pricing"]
   [:p {:class ["text-lg" "text-slate-600" "leading-relaxed"]}
    (str "Choose the plan that fits your business needs. "
         "From individual developers to large financial institutions, "
         "we've got you covered.")]])

(defn pricing-card [{:keys [name price description features cta highlight]}]
  [:div
   {:class (into ["relative" "flex" "flex-col" "p-8" "rounded-3xl" "border"]
                 (if highlight
                   ["border-blue-600" "ring-1" "ring-blue-600" "shadow-xl" "shadow-blue-50"]
                   ["border-slate-100" "shadow-sm"]))}

   (when highlight
     [:span {:class ["absolute" "-top-4" "left-1/2" "-translate-x-1/2" "bg-blue-600"
                     "text-white" "text-[11px]" "font-bold" "px-3" "py-1"
                     "rounded-full" "uppercase" "tracking-widest"]}
      "Most Popular"])

   [:div {:class ["mb-8"]}
    [:h3 {:class ["text-lg" "font-bold" "mb-2"]} name]
    [:div {:class ["flex" "items-baseline" "gap-1" "mb-4"]}
     [:span {:class ["text-4xl" "font-black"]} price]
     (when-not (= price "Custom")
       [:span {:class ["text-sm" "text-slate-500"]} "/month"])]
    [:p {:class ["text-sm" "text-slate-500" "leading-relaxed"]}
     description]]

   [:ul {:class ["flex-1" "space-y-4" "mb-8"]}
    (for [feature features]
      [:li {:class ["flex" "items-start" "gap-3" "text-sm" "text-slate-600"]}
       [:svg {:class ["w-5" "h-5" "text-blue-500" "flex-shrink-0"]
              :fill "none" :stroke "currentColor" :viewBox "0 0 24 24"}
        [:path {:stroke-linecap "round" :stroke-linejoin "round"
                :stroke-width "2" :d "M5 13l4 4L19 7"}]]
       feature])]

   [:button
    {:class (into ["w-full" "py-3" "px-4" "rounded-xl" "font-bold" "text-sm" "transition-all"]
                  (if highlight
                    ["bg-blue-600" "text-white" "hover:bg-blue-700" "shadow-lg" "shadow-blue-100"]
                    ["bg-slate-900" "text-white" "hover:bg-black"]))}
    cta]])

(def trust-footer
  [:footer {:class ["mt-20" "text-center"]}
   [:p {:class ["text-sm" "text-slate-400" "font-medium" "italic"]}
    (str "All plans include SSL encryption and 99.9% uptime guarantee. "
         "Need something different? ")
    [:a {:href "mailto:support@tridcheck.com"
         :class ["text-blue-600" "underline"]}
     "Chat with us"] "."]])

(def content
  [:div {:class ["bg-white" "min-h-screen" "pb-24" "text-slate-900"]}
   [:main {:class ["max-w-7xl" "mx-auto" "px-6" "pt-20"]}
    header-section
    [:div {:class ["grid" "grid-cols-1" "md:grid-cols-3" "gap-8" "max-w-6xl" "mx-auto"]}
     (map pricing-card plans)]
    trust-footer]])

(defn handler [_]
  (layout/base "Pricing | TRID Check" content))
