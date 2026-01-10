(ns gritum.engine.configs)

(def Env
  [:enum :prod :local])

(def Port
  pos-int?)

(def DbConfig
  [:map
   [:dbtype [:enum "postgresql"]]
   [:dbname :string]
   [:user [:enum "gritum_admin"]]
   [:password :string]
   [:host :string]
   [:port [:enum 5432]]])

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

(defn get-db-config
  {:malli/schema
   [:=> [:cat] DbConfig]}
  []
  {:dbtype "postgresql"
   :dbname (or (System/getenv "DB_NAME") "gritum_db_local")
   :user (or (System/getenv "DB_USER") "gritum_admin")
   :password (or (System/getenv "DB_PASS") "gritum_password")
   :host (or (System/getenv "DB_HOST") "localhost")
   :port (or (System/getenv "DB_PORT") "5432")})
