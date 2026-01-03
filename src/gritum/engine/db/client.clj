(ns gritum.engine.db.client
  (:require
   [next.jdbc.sql :as sql]
   [buddy.hashers :as hs]))

(defn register!
  [ds email raw-password]
  (->> {:email email
        :password_hash
        (hs/derive raw-password)}
       (sql/insert! ds :client)))

(defn get-by-email
  [ds email]
  (->> {:email email}
       (sql/find-by-keys ds :client)
       first))

(defn authenticate
  [ds email attempt]
  (let [{:keys [:client/password_hash] :as client}
        (get-by-email ds email)]
    (when (and client (hs/check attempt password_hash))
      (dissoc client :client/password_hash))))

(comment
  (require
   '[integrant.repl.state :as irs])
  (let [ds (:gritum.engine.db/pool irs/system)]
    (register! ds "test2@gritum.io" "raw-pw"))
  (let [ds (:gritum.engine.db/pool irs/system)]
    (authenticate ds "test2@gritum.io" "raw-pw")))
