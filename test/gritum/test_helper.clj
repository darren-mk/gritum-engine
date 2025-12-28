(ns gritum.test-helper
  (:require
   [clojure.edn :as edn]
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
   [clojure.test :as t]))

(defn load-edn [filename]
  (if-let [resource-url (io/resource filename)]
    (-> resource-url slurp edn/read-string)
    (throw (Exception. (str "Resource not found: " filename)))))

(defn load-xml [filename]
  (if-let [resource-url (io/resource filename)]
    (-> resource-url slurp xml/parse-str)
    (throw (Exception. (str "Resource not found: " filename)))))

(defn populate-xml [v path]
  (-> (fn [acc tag] {:tag tag
                     :content (if acc [acc] v)})
      (reduce nil (reverse path))))

(t/deftest populate-xml-test
  (t/is (= {:tag :MESSAGE
            :content
            [{:tag :DOCUMENT_SETS
              :content
              [{:tag :DOCUMENT_SET
                :content
                [{:tag :DOCUMENTS
                  :content
                  [{:tag :DOCUMENT
                    :content
                    [{:tag :DEAL_SETS
                      :content
                      [{:tag :DEAL_SET
                        :content
                        [{:tag :DEALS
                          :content
                          [{:tag :DEAL
                            :content
                            [{:tag :LOANS
                              :content
                              [{:tag :LOAN
                                :content
                                [{:tag :FEE_INFORMATION
                                  :content
                                  [{:tag :FEES
                                    :content []}]}]}]}]}]}]}]}]}]}]}]}]}
           (populate-xml []
                         [:MESSAGE :DOCUMENT_SETS :DOCUMENT_SET
                          :DOCUMENTS :DOCUMENT :DEAL_SETS :DEAL_SET :DEALS
                          :DEAL :LOANS :LOAN :FEE_INFORMATION :FEES]))))


