(ns gritum.engine.infra
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [malli.core :as m]))

(def Env
  [:enum :prod :local])

(def DbConfig
  [:map
   [:dbtype [:enum "postgresql"]]
   [:dbname :string]
   [:user [:enum "gritum_admin"]]
   [:password :string]
   [:host :string]
   [:port [:enum 5432]]])

(def MigrationConfig
  [:map
   [:store [:enum :database]]
   [:migration-dir [:enum "migrations"]]
   [:db DbConfig]])

(def Config
  [:map
   [:local :map]
   [:prod :map]])

(def Context
  [:map
   [:env Env]
   [:port :int]])

(defn ->config []
  (->> "config.edn" io/file
       slurp edn/read-string
       (m/coerce Config)))

(defn ->context []
  (->> "context.edn" io/file
       slurp edn/read-string
       (m/coerce Context)))

(defn prod? []
  (= :prod (:env (->context))))
