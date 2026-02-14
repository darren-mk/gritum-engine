(ns gritum.engine.configs
  (:require [clojure.string :as str]))

(def Env
  [:enum :prod :local])

(defn- bring! [k]
  (let [v (System/getenv k)]
    (if (str/blank? v)
      (let [msg (str "🚨 CRITICAL CONFIG ERROR: Environment variable '"
                     k "' is not set.")]
        (throw (ex-info msg {:variable k})))
      v)))

(defn get-env []
  (keyword (bring! "GRITUM_ENV")))

(defn get-port []
  (Integer/parseInt (bring! "PORT")))

(defn get-db-config []
  (let [user (bring! "DB_USER")]
    {:dbtype (bring! "DB_TYPE")
     :dbname (bring! "DB_NAME")
     :host (bring! "DB_HOST")
     :port (Integer/parseInt (bring! "DB_PORT"))
     :user user
     :username user
     :password (bring! "DB_PASSWORD")}))

(defn get-llm-config []
  {:ai-api-key (bring! "LLM_API_KEY")
   :ai-model (bring! "LLM_MODEL")})