(ns gritum.engine.auth
  (:require
   [taoensso.timbre :as log]))

(defn create-auth-service [_db]
  (log/info "initializing auth service with db connection")
  (fn [prod? api-key]
    (let [test-keys #{"gritum-api-test-key"}]
      (and (not prod?)
           (contains? test-keys api-key)))))
