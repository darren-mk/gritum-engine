(require
 '[babashka.tasks :as b]
 '[clojure.edn :as edn]
 '[clojure.java.io :as io])

(def inputs
  *command-line-args*)

(def env
  (keyword (or (first inputs) "prod")))

(def action
  (keyword (or (second inputs) "all")))

(defn get-config [env]
  (let [all-config (-> "config.edn" io/file slurp edn/read-string)]
    (assoc (get all-config env) :env env)))

(defn provision-db [{:keys [cloud db] :as _cfg}]
  (let [{:keys [project-id region]} cloud
        {:keys [version tier instance dbname user password]} db]
    (println "🚀 Step 1: Creating Cloud SQL instance (PostgreSQL)...")
    (b/shell "gcloud" "sql" "instances" "create" instance
             "--database-version" version
             "--tier" tier
             "--region" region
             "--root-password" password
             "--project" project-id)
    (println "📡 Step 2: Creating the 'gritum' database...")
    (b/shell "gcloud" "sql" "databases" "create" dbname
             "--instance" instance
             "--project" project-id)
    (println "👤 Step 3: Creating the application user...")
    (b/shell "gcloud" "sql" "users" "create" user
             "--instance" instance
             "--password" password
             "--project" project-id)
    (println "✅ Cloud SQL setup is complete!")))

(defn build []
  (b/shell "clojure" "-T:build" "uberjar"))

(defn image [{:keys [image]}]
  (let [{:keys [name tag]} image]
    (println (str "🐳 Building Docker image: " name ":" tag))
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
    (println "🚀 Deploying to Cloud Run...")
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
    :local (do (println "🏠 Running LOCAL migrations...")
               (b/shell "clojure" "-M:migrate"))
    :prod (let [{:keys [dbname user password]} db]
            (println "🚀 Running PROD migrations via Proxy...")
            (b/shell {:extra-env {"DB_NAME" dbname
                                  "DB_USER" user
                                  "DB_PASS" password
                                  "DB_HOST" "127.0.0.1"
                                  "DB_PORT" "5433"}}
                     "clojure" "-M:migrate"))))

(defn ->msg [action]
  (str "*" (name action) "*"
       " job is done. 🚀"))

(let [cfg (get-config env)]
  (case action
    :check (println cfg)
    :provision (provision-db cfg)
    :migrate (migrate cfg)
    :build (build)
    :image (image cfg)
    :register (register cfg)
    :deploy (deploy cfg)
    :all (do (migrate cfg)
             (build)
             (image cfg)
             (register cfg)
             (deploy cfg)))
  (println (->msg action)))
