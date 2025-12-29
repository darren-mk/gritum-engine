(ns gritum.web.router
  (:require
   [reitit.ring :as ring]
   [gritum.core :as core]
   [gritum.web.middlewares :as mw]
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

(def routes
  ["/api"
   ["/v1"
    ["/health" {:get handle-health}]
    ["/evaluate" {:post handle-evaluate}]]])

(def app
  (ring/ring-handler
   (ring/router
    routes
    {:data {:middleware [mw/inject-headers-in-resp
                         mw/turn-resp-body-to-bytes
                         mw/wrap-exception
                         wrap-params
                         wrap-multipart-params]}})))
