(ns gritum.engine.db.migrate
  (:require
   [gritum.engine.configs :as configs]
   [migratus.core :as migratus]))

(defn get-mig-config []
  {:store :database
   :migration-dir "migrations"
   :db (configs/get-db-config)})

(defn create
  "populates up migration file only as
  we follow forward-only principle"
  {:malli/schema [:=> [:cat :string] :any]}
  [name]
  (migratus/create (get-mig-config) name))

(defn run []
  (let [config (get-mig-config)]
    (println "🚀 running migrations")
    (migratus/migrate config)
    (println "✅ migrations completed successfully.")))

(defn -main [& _args]
  (run))
