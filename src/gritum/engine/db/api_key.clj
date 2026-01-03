(ns gritum.engine.db.api-key
  (:require
   [clojure.string :as cstr]
   [next.jdbc.sql :as sql]
   [buddy.hashers :as hs]
   [crypto.random :as random]
   [gritum.engine.infra :as inf]))

(defn ->env-indicator
  {:malli/schema
   [:=> [:cat inf/Env] :string]}
  [env]
  (case env
    :prod "live"
    :stage "stage"
    (:test :local) "test"))

(defn gen-token
  {:malli/schema
   [:=> [:cat inf/Env] :map]}
  [env]
  (let [key-id (random/hex 6)
        secret (random/hex 32)
        env-indicator  (->env-indicator env)]
    {:key-id key-id :secret secret
     :raw-key (str "bitem" "_" env-indicator
                   "_" key-id "_" secret)}))

(defn create!
  ([ds client-id]
   (create! ds client-id 10))
  ([ds client-id usage-limit]
   (let [env (:env (inf/->context))
         {:keys [key-id secret raw-key]} (gen-token env)]
     (sql/insert! ds :api_keys
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
      (let [{:keys [api_keys/hashed_key] :as k}
            (->> ["SELECT * FROM api_keys WHERE key_id = ? AND is_active = true" key-id]
                 (sql/query ds) first)]
        (when (and hashed_key (hs/check secret hashed_key))
          k)))))

(defn list-by-client
  {:malli/schema [:=> [:cat :any :uuid] [:sequential :any]]}
  [ds client-id]
  (sql/query ds ["SELECT key_id, created_at, usage_count, usage_limit 
                  FROM api_keys 
                  WHERE client_id = ? 
                  ORDER BY created_at DESC" client-id]))

(comment
  (require
   '[integrant.repl.state :as irs])

  (let [ds (:gritum.engine.db/pool irs/system)]
    (list-by-client ds #uuid "d0deca17-5594-470a-b85f-b7c52e39d33a"))
  ;; => [#:api_keys{:key_id "f38ab5a80fdc",
  ;;                :created_at #inst "2026-01-02T14:54:31.334336000-00:00",
  ;;                :usage_count 0,
  ;;                :usage_limit 10}
  ;;     #:api_keys{:key_id "7a7f00b006ff",
  ;;                :created_at #inst "2026-01-02T14:54:10.883906000-00:00",
  ;;                :usage_count 0,
  ;;                :usage_limit 10}
  ;;     #:api_keys{:key_id "8b5c65de2e0d",
  ;;                :created_at #inst "2026-01-02T14:44:58.514786000-00:00",
  ;;                :usage_count 0,
  ;;               :usage_limit 10}]

  (let [ds (:gritum.engine.db/pool irs/system)]
    (create! ds #uuid "d0deca17-5594-470a-b85f-b7c52e39d33a" 10))
  ;; => "bitem_test_f38ab5a80fdc_47e5690e2705fca515b2b556584fbd962fdccb9f72b7fed33926c76fbdcd3153"

  (let [ds (:gritum.engine.db/pool irs/system)]
    (verify! ds "bitem_test_f38ab5a80fdc_47e5690e2705fca515b2b556584fbd962fdccb9f72b7fed33926c76fbdcd3153"))
  ;; => #:api_keys{:id #uuid "e3050955-f2de-4f0a-b34b-7e609eb825fa",
  ;;               :key_id "f38ab5a80fdc",
  ;;               :client_id #uuid "d0deca17-5594-470a-b85f-b7c52e39d33a",
  ;;               :hashed_key
  ;;               "bcrypt+sha512$0c7ec2bd93e4fc11e9b41cdb5bbc8900$12$4ad8a28414a90ce5f680d48c4afdac1b995579a92c173d71",
  ;;               :usage_count 0,
  ;;               :usage_limit 10,
  ;;               :is_active true,
  ;;              :created_at #inst "2026-01-02T14:54:31.334336000-00:00"}
  )

