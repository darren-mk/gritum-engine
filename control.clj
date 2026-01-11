(ns control
  (:require [babashka.process :as b]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn get-config [env]
  (-> "config.edn"
      io/file slurp
      edn/read-string
      (get env)
      (assoc :env env)))

(defn provision [{:keys [cloud db] :as _cfg}]
  (let [{:keys [project-id region]} cloud
        {:keys [version tier instance dbname user password]} db]
    (println "🚀 step 1: creating cloud sql instance...")
    (b/shell "gcloud" "sql" "instances" "create" instance
             "--database-version" version
             "--tier" tier
             "--region" region
             "--root-password" password
             "--project" project-id)
    (println "📡 step 2: creating the 'gritum' database...")
    (b/shell "gcloud" "sql" "databases" "create" dbname
             "--instance" instance
             "--project" project-id)
    (println "👤 step 3: creating the application user...")
    (b/shell "gcloud" "sql" "users" "create" user
             "--instance" instance
             "--password" password
             "--project" project-id)
    (println "✅ cloud sql setup is complete!")))

(defn build []
  (b/shell "clojure" "-T:build" "uberjar"))

(defn press [{:keys [image]}]
  (let [{:keys [name tag]} image]
    (println (str "🐳 building docker image: " name ":" tag))
    (b/shell "docker" "buildx" "build"
             "--platform" "linux/amd64"
             "-t" (str name ":" tag) ".")))

(defn register [{:keys [cloud image]}]
  (let [{:keys [project-id region]} cloud
        {:keys [name tag]} image
        remote-tag (format "%s-docker.pkg.dev/%s/images/%s:%s"
                           region project-id name tag)]
    (println "📤 Pushing image to Artifact Registry...")
    (b/shell "docker" "tag" (str name ":" tag) remote-tag)
    (b/shell "docker" "push" remote-tag)))

(defn deploy [{:keys [env cloud image db]}]
  (let [{:keys [project-id region]} cloud
        {image-name :name :keys [tag]} image
        {:keys [instance dbname user password]} db
        remote-tag (format "%s-docker.pkg.dev/%s/images/%s:%s"
                           region project-id image-name tag)
        db-conn-name (format "%s:%s:%s" project-id region instance)]
    (println "☁️ deploying to cloud run...")
    (b/shell "gcloud" "run" "deploy" image-name
             "--image" remote-tag
             "--region" region
             "--project" project-id
             "--add-cloudsql-instances" db-conn-name
             "--set-env-vars" (str "GRITUM_ENV=" (name env)
                                   ",DB_NAME=" dbname
                                   ",DB_USER=" user
                                   ",DB_PASS=" password
                                   ",DB_HOST=/cloudsql/" db-conn-name)
             "--allow-unauthenticated")))

(defn migrate [{:keys [env db]}]
  (case env
    :local (do (println "🏠 running local migrations...")
               (b/shell "clojure" "-M:migrate"))
    :prod (let [{:keys [dbname user password]} db]
            (println "🏠 running prod migrations via Proxy...")
            (b/shell {:extra-env {"DB_NAME" dbname
                                  "DB_USER" user
                                  "DB_PASS" password
                                  "DB_HOST" "127.0.0.1"
                                  "DB_PORT" "5433"}}
                     "clojure" "-M:migrate"))))

(defn completion-msg [task]
  (str "🚀 *" (name task) "*"
       " job is done. 🚀"))

(defn -main [& args]
  (let [[env-str task-str] args]
    (assert env-str "❌ Environment (prod/local) is required.")
    (assert task-str "❌ Task is required.")
    (let [env (keyword env-str)
          task (keyword task-str)
          cfg (get-config env)]
      (case task
        :check (println cfg)
        :provision (provision cfg)
        :migrate (migrate cfg)
        :build (build)
        :press (press cfg)
        :register (register cfg)
        :deploy (deploy cfg)
        :all (do (migrate cfg)
                 (build)
                 (press cfg)
                 (register cfg)
                 (deploy cfg))
        (do (println (str "📖 Usage: bb control.clj "
                          "[env] [build|press|register|deploy|all]"))
            (System/exit 1)))
      (println (completion-msg task)))))

(apply -main *command-line-args*)
