(ns gritum.engine.frontend.signal)

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

(defn ->val
  {:malli/schema
   [:=> [:cat :string] :string]}
  [s]
  (str "'" s "'"))
