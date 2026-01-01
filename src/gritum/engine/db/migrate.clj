(ns gritum.engine.db.migrate
  (:require
   [gritum.engine.infra :as inf]
   [migratus.core :as migratus]))

(defn ->mig-config
  {:malli/schema [:=> [:cat inf/Env] inf/MigrationConfig]}
  [env]
  (let [{:keys [db migration]} (get (inf/->config) env)]
    (if db {:store :database
            :migration-dir (:dir migration)
            :db db}
        (throw (ex-info "no config found" {:env env})))))

(defn create
  "populates up migration file only as
  we follow forward-only principle"
  {:malli/schema [:=> [:cat inf/Env :string] :any]}
  [env name]
  (migratus/create (->mig-config env) name))

(defn run
  {:malli/schema [:=> [:cat inf/Env] :any]}
  [env]
  (let [config (->mig-config env)]
    (println (str "🚀 running migrations for [" env "]..."))
    (migratus/migrate config)))

(defn -main [& args]
  (let [env (keyword (or (first args) "local"))]
    (run env)))
