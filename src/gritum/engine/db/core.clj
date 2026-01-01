(ns gritum.engine.db.core
  (:require
   [integrant.core :as ig]
   [next.jdbc.connection :as connection]
   [malli.core :as m]
   [gritum.engine.infra :as inf])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(defmethod ig/init-key :gritum.engine.db/pool [_ db-cfg]
  (malli.core/assert inf/DbConfig db-cfg)
  (println "📡 Initializing HikariCP connection pool...")
  (connection/->pool HikariDataSource db-cfg))

(defmethod ig/halt-key! :gritum.engine.db/pool [_ datasource]
  (println "🛑 Closing HikariCP connection pool...")
  (when (instance? java.io.Closeable datasource)
    (.close datasource)))
