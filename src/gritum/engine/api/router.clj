(ns gritum.engine.api.router
  (:require
   [reitit.ring :as ring]
   [gritum.engine.core :as core]
   [gritum.engine.api.middlewares :as mw]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.params :refer [wrap-params]]))

(defn- handle-health [_]
  {:status 200
   :body {:status "up"
          :version core/version
          :timestamp (.toString (java.time.Instant/now))}})

(defn- handle-evaluate [req]
  (let [params (:multipart-params req)
        le-file (get params "le-file")
        cd-file (get params "cd-file")]
    (if (and le-file cd-file)
      (let [le-str (slurp (:tempfile le-file))
            cd-str (slurp (:tempfile cd-file))
            report (core/evaluate-xml le-str cd-str)]
        {:status 200 :body report})
      {:status 400
       :body {:error "Missing files"
              :details "le-file and cd-file are required"}})))

(defn app [{:keys [prod? auth-fn]}]
  (let [auth-mw (mw/wrap-api-key-auth prod? auth-fn)]
    (ring/ring-handler
     (ring/router
      ["/api"
       ["/health" {:get handle-health}]
       ["/v1"
        {:middleware [auth-mw]}
        ["/evaluate" {:post handle-evaluate}]]]
      {:data {:middleware [mw/wrap-exception
                           mw/wrap-api-cors
                           mw/inject-headers-in-resp
                           mw/turn-resp-body-to-bytes
                           wrap-params
                           wrap-multipart-params]}}))))


