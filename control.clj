(require
 '[babashka.tasks :as b]
 '[clojure.string :as cstr])

(def inputs
  *command-line-args*)

(def env
  (keyword (or (first inputs) "prod")))

(def action
  (keyword (or (second inputs) "all")))

(def region
  "us-east1")

(defn config []
  (let [project-id
        (->> "gcloud config get-value project"
             (b/shell {:out :string})
             :out cstr/trim)
        image-name "gritum-engine"
        tag "latest"]
    (assert (= (str "bitem-gritum-" (name env)) project-id)
            (format (str "mismatch: requested [%s] but "
                         "gcloud project is set to [%s]!")
                    (name env) project-id))
    {:env env :region region
     :project-id project-id
     :image-name image-name
     :tag tag}))

(defn build []
  (b/shell "clojure" "-T:build" "uberjar"))

(defn image [{:keys [image-name tag]}]
  (b/shell "docker" "buildx" "build"
           "--platform" "linux/amd64"
           "-t" (str image-name ":" tag) "."))

(defn register [{:keys [project-id region image-name tag]}]
  (let [remote-tag (format "%s-docker.pkg.dev/%s/images/%s:%s"
                           region project-id image-name tag)]
    (b/shell "docker" "tag" (str image-name ":" tag) remote-tag)
    (b/shell "docker" "push" remote-tag)))

(defn deploy [{:keys [env project-id region image-name tag]}]
  (let [remote-tag (format "%s-docker.pkg.dev/%s/images/%s:%s"
                           region project-id image-name tag)]
    (b/shell "gcloud" "run" "deploy" image-name
             "--image" remote-tag "--region" region
             "--set-env-vars" (str "GRITUM_ENV=" (name env))
             "--allow-unauthenticated")))

(let [cfg (config)
      msg (str "*" (name action) "*"
               " job is done. 🚀")]
  (case action
    :check (println cfg)
    :build (build)
    :image (image cfg)
    :register (register cfg)
    :deploy (deploy cfg)
    :all (do (build)
             (image cfg)
             (register cfg)
             (deploy cfg)))
  (println msg))
