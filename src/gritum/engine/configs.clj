(ns gritum.engine.configs
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn- read-dotenv []
  (let [env-file (io/file ".env")]
    (if (.exists env-file)
      (with-open [r (io/reader env-file)]
        (->> (line-seq r)
             (keep (fn [line]
                     (let [line (str/trim line)]
                       (when-not (or (str/blank? line) (str/starts-with? line "#"))
                         (let [[k v] (str/split line #"=" 2)]
                           (when (and k v)
                             [(keyword (str/replace (str/lower-case k) "_" "-")) v]))))))
             (into {})))
      {})))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]]
              [(keyword (str/replace (str/lower-case k) "_" "-")) v]))
       (into {})))

(defn- load-env
  "ensures reading from env at runtime"
  []
  (merge (read-dotenv)
         (read-system-env)))

(def ^:private env
  "ensures compute only when actually used"
  (delay (load-env)))

(defn- env-get [k]
  (get @env k))

(def Env
  [:enum :prod :local])

(def Port
  pos-int?)

(def LlmConfig
  [:map
   [:ai-api-key :string]
   [:ai-model :string]])

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
   (or (env-get :gritum-env)
       "local")))

(defn parse-int [s default-val]
  (try
    (Integer/parseInt s)
    (catch Exception _ default-val)))

(defn get-port
  {:malli/schema
   [:=> [:cat] Port]}
  []
  (parse-int (env-get :port) 3000))

(defn get-db-config
  {:malli/schema [:=> [:cat] DbConfig]}
  []
  (let [user (env-get :db-user)]
    {:dbtype "postgresql"
     :dbname (env-get :db-name)
     :host (env-get :db-host)
     :port (parse-int (env-get :db-port) 5432)
     :user user :username user
     :password (env-get :db-pass)}))

(defn get-llm-config
  {:malli/schema [:=> [:cat] LlmConfig]}
  []
  {:ai-api-key (env-get :llm-api-key)
   :ai-model (env-get :llm-model)})