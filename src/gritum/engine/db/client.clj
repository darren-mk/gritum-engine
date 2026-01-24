(ns gritum.engine.db.client
  (:require
   [buddy.hashers :as hs]
   [gritum.engine.domain.model :as dom]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

(defn register!
  {:malli/schema [:=> [:cat :any dom/Email :string]
                  [:maybe dom/Client]]}
  [ds email raw-pw-str full_name]
  (->> {:email email
        :password_hash (hs/derive raw-pw-str)
        :full_name full_name}
       (sql/insert! ds :client)))

(defn get-by-email
  {:malli/schema [:=> [:cat :any :string]
                  [:maybe dom/Client]]}
  [ds email]
  (first
   (sql/find-by-keys
    ds :client {:email email}
    {:builder-fn rs/as-unqualified-maps})))

(defn authenticate
  {:malli/schema [:=> [:cat :any :string :string]
                  [:maybe dom/Client]]}
  [ds email pw-attempt]
  (let [{:keys [password_hash] :as client}
        (get-by-email ds email)]
    (when (and client (hs/check pw-attempt password_hash))
      (dissoc client :password_hash))))

(comment
  (require
   '[integrant.repl.state :as irs])
  (let [ds (:gritum.engine.db/pool irs/system)]
    (register! ds "test1@email.com" "raw-pw" "John Doe"))
  (let [ds (:gritum.engine.db/pool irs/system)]
    (get-by-email ds "test2@gritum.io"))
  (let [ds (:gritum.engine.db/pool irs/system)]
    (authenticate ds "test2@gritum.io" "raw-pw")))
