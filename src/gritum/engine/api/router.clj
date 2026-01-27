(ns gritum.engine.api.router
  (:require
   [gritum.engine.api.middleware :as mw]
   [gritum.engine.core :as core]
   [gritum.engine.db.client :as db.client]
   [gritum.engine.db.api-key :as db.api-key]
   [gritum.engine.frontend.routes :as route.web]
   [reitit.coercion.malli :as rcmal]
   [reitit.ring :as ring]
   [reitit.openapi :as openapi]
   [ring.middleware.multipart-params :as multp]
   [ring.middleware.params :as midp]
   [ring.util.http-response :as resp]
   [taoensso.timbre :as log]
   [gritum.engine.domain.model :as dom]))

(defn- handle-health [_]
  {:status 200
   :body {:status "up"
          :version core/version
          :timestamp (.toString (java.time.Instant/now))}})

(defn- evaluate-handler [req]
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

(defn login-handler [ds]
  (fn [{:keys [body] :as _req}]
    (let [{:keys [email password]} body
          client (db.client/authenticate ds email password)]
      (if client
        (-> (resp/ok client)
            (assoc :session {:identity (:id client)}))
        (resp/unauthorized {:error "Invalid email or password"})))))

(defn logout-handler [_req]
  (-> (resp/ok {:message "Logged out"})
      (assoc :session nil)))

(defn signup-handler [ds]
  (fn [{:keys [body] :as _req}]
    (let [{:keys [email password full_name]} body]
      (try
        (let [{:keys [email]} (db.client/register! ds email password full_name)]
          (resp/ok {:message "Account created successfully"
                    :email email}))
        (catch Exception e
          (log/error "Signup failed:" (.getMessage e))
          (resp/bad-request {:error "Signup failed"
                             :details "Email might already be in use"}))))))

(defn pong-handler [_]
  (resp/ok {:message "pong"}))

(defn me-handler [req]
  (resp/ok {:id (get-in req [:session :identity])}))

(defn create-api-key-handler [ds]
  (fn [req]
    (let [client-id (get-in req [:session :identity])
          raw-key (db.api-key/create! ds client-id)
          msg "API key created successfully."]
      (resp/ok {:api_key raw-key
                :message msg}))))

(defn list-api-keys-handler [ds]
  (fn [req]
    (let [client-id (get-in req [:session :identity])
          api-keys (db.api-key/list-by-client ds client-id)]
      (resp/ok api-keys))))

(defn app [{:keys [ds]}]
  (let [auth-mw (mw/wrap-api-key-auth ds)]
    (ring/ring-handler
     (ring/router
      [(route.web/pages [mw/wrap-session
                         mw/content-type-html
                         mw/wrap-hiccup])
       (route.web/hypermedia [mw/wrap-session
                              mw/read-body] ds)
       ["/openapi.json"
        {:get {:no-doc true
               :handler (openapi/create-openapi-handler)}}]

       ["/api" {:coercion rcmal/coercion
                :middleware [mw/write-body-as-bytes]}
        ["/health" {:get handle-health}]
        ["/services" {:middleware [mw/content-type-json
                                   auth-mw
                                   mw/wrap-public-cors
                                   mw/read-body]}
         ["/v1"
          ["/ping" {:get {:responses {200 {:body [:map [:message :string]]}}
                          :handler pong-handler}}]
          ["/evaluate" {:post evaluate-handler}]]]
        ["/dashboard" {:middleware [mw/wrap-session
                                    mw/wrap-dashboard-cors]}

         ["/signup" {:post {:summary "create client and return email with message"
                            :responses {200 {:body [:map [:message :string] [:email dom/Email]]}}
                            :handler (signup-handler ds)}}]
         ["/login" {:post {:summary "log in as client"
                           :responses {200 {:body dom/Client}}
                           :handler (login-handler ds)}}]
         ["/auth" {:middleware [mw/wrap-require-auth]}
          ["/api-keys" {:get {:summary "returns api keys for the client"
                              :responses {200 {:body [:sequential dom/ApiKey]}}
                              :handler (list-api-keys-handler ds)}
                        :post {:summary "create a api key for the client"
                               :response {200 {:body [:map [:message :string [:api_key :string]]]}}
                               :handler (create-api-key-handler ds)}}]
          ["/me" {:get me-handler}]
          ["/logout" {:post logout-handler}]]]]]
      {:data {:middleware [mw/wrap-exception
                           midp/wrap-params
                           multp/wrap-multipart-params]}}))))
