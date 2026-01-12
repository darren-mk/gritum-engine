(ns gritum.engine.configs)

(def Env
  [:enum :prod :local])

(def Port
  pos-int?)

(def DbConfig
  [:map
   [:dbtype [:enum "postgresql"]]
   [:dbname :string]
   [:host :string]
   [:port [:enum 5432]]

   [:user [:enum "gritum_admin"]]
   [:username [:enum "gritum_admin"]]
   [:password :string]])

(def MigrationConfig
  [:map
   [:store [:enum :database]]
   [:migration-dir [:enum "migrations"]]
   [:db DbConfig]])

(defn get-env
  {:malli/schema
   [:=> [:cat] Env]}
  []
  (keyword
   (or (System/getenv "GRITUM_ENV")
       "local")))

(defn get-port
  {:malli/schema
   [:=> [:cat] Port]}
  []
  (Integer/parseInt
   (or (System/getenv "PORT")
       "3000")))

(defn parse-int [s default-val]
  (try
    (Integer/parseInt s)
    (catch Exception _ default-val)))

(defn get-db-config
  {:malli/schema [:=> [:cat] DbConfig]}
  []
  (let [user (or (System/getenv "DB_USER") "gritum_admin")]
    {:dbtype "postgresql"
     :dbname (or (System/getenv "DB_NAME") "gritum_db_local")
     :host (or (System/getenv "DB_HOST") "localhost")
     :port (parse-int (System/getenv "DB_PORT") 5432)
     :user user :username user
     :password (or (System/getenv "DB_PASS") "gritum_password")}))
