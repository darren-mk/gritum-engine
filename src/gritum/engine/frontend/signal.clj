(ns gritum.engine.frontend.signal
  (:require
   [jsonista.core :as json]))

(defn ->json-obj [m]
  (json/write-value-as-string m))

(defn bind
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (name k))

(defn cite
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str "$" (name k)))

(defn cite-not
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str "!$" (name k)))

(defn assign
  {:malli/schema
   [:=> [:cat :keyword :any] :string]}
  [k v]
  (str (cite k) "=" v))

(defn equal?
  {:malli/schema
   [:=> [:cat :string :string] :string]}
  [a b]
  (str a "==" b))

(defn toggle
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str (cite k) "=" (cite-not k)))

(defn erase
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str (cite k) "=''"))

(defn ref-empty?
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str (cite k) "==''"))

(defn ref-not-empty?
  {:malli/schema
   [:=> [:cat :keyword] :string]}
  [k]
  (str (cite-not k) "==''"))

(defn ->text
  {:malli/schema
   [:=> [:cat :string] :string]}
  [s]
  (str "'" s "'"))

(defn post
  {:malli/schema
   [:=> [:cat :string] :string]}
  [url]
  (str "@post('" url "')"))

(defn combine
  [& xs]
  (->> xs
       (interpose "; ")
       (apply str)))
