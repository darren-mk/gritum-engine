(ns gritum.engine.db.api-key
  (:require
   [clojure.string :as cstr]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]
   [buddy.hashers :as hs]
   [crypto.random :as random]
   [gritum.engine.configs :as configs]))

(defn ->env-indicator
  {:malli/schema
   [:=> [:cat configs/Env] :string]}
  [env]
  (case env
    :prod "live"
    :stage "stage"
    (:test :local) "test"))

(defn gen-token
  {:malli/schema
   [:=> [:cat configs/Env] :map]}
  [env]
  (let [key-id (random/hex 6)
        secret (random/hex 32)
        env-indicator (->env-indicator env)]
    {:key-id key-id :secret secret
     :raw-key (str "bitem" "_" env-indicator
                   "_" key-id "_" secret)}))

(defn create!
  ([ds client-id]
   (create! ds client-id 10))
  ([ds client-id usage-limit]
   (let [env (configs/get-env)
         {:keys [key-id secret raw-key]} (gen-token env)]
     (sql/insert! ds :api_key
                  {:client_id client-id
                   :key_id key-id
                   :hashed_key (hs/derive secret)
                   :usage_limit  usage-limit})
     raw-key)))

(defn verify!
  {:malli/schema [:=> [:cat :any :string] :map]}
  [ds raw-key]
  (let [[_ _ key-id secret] (cstr/split raw-key #"_")]
    (when (and key-id secret)
      (let [{:keys [api_key/hashed_key] :as k}
            (->> ["SELECT * FROM api_key WHERE key_id = ? AND is_active = true" key-id]
                 (sql/query ds) first)]
        (when (and hashed_key (hs/check secret hashed_key))
          k)))))

(defn list-by-client
  {:malli/schema [:=> [:cat :any :uuid] [:sequential :any]]}
  [ds client-id]
  (sql/query ds [(str "SELECT key_id, created_at, usage_count, usage_limit "
                      "FROM api_key WHERE client_id = ? "
                      "ORDER BY created_at DESC")
                 client-id]
             {:builder-fn rs/as-unqualified-maps}))

(comment
  (require
   '[integrant.repl.state :as irs])
  (let [ds (:gritum.engine.db/pool irs/system)]
    (list-by-client ds #uuid "d0deca17-5594-470a-b85f-b7c52e39d33a"))
  (let [ds (:gritum.engine.db/pool irs/system)]
    (create! ds #uuid "d0deca17-5594-470a-b85f-b7c52e39d33a" 10))
  (let [ds (:gritum.engine.db/pool irs/system)]
    (verify! ds "bitem_test_f38ab5a80fdc_47e5690e2705fca515b2b556584fbd962fdccb9f72b7fed33926c76fbdcd3153")))
