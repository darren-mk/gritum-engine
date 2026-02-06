(ns gritum.engine.external.utils-test
  (:require
   [clojure.test :as t]
   [gritum.engine.external.utils :as sut]))

(t/deftest inject-into-txt-test
  (t/is (= "i love you!"
           (sut/inject-into-txt "i {{secret}} you!"
                                :secret "love"))))
