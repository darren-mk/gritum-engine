(ns gritum.engine.api.middlewares-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [gritum.engine.api.middleware :as sut]
   [jsonista.core :as json]))

(deftest inject-headers-in-resp-test
  (testing "should add application/json Content-Type header to valid responses"
    (let [mock-handler (fn [_] {:status 200 :body "ok"})
          middleware (sut/content-type-json mock-handler)
          response (middleware {})]
      (is (= "application/json; charset=utf-8"
             (get-in response [:headers "Content-Type"])))))
  (testing "should return nil if the handler returns nil"
    (let [mock-handler (fn [_] nil)
          middleware (sut/content-type-json mock-handler)
          response (middleware {})]
      (is (nil? response)))))

(deftest turn-resp-body-to-bytes-test
  (testing "should convert Map body into JSON byte array"
    (let [data {:foo "bar"}
          mock-handler (fn [_] {:status 200 :body data})
          middleware (sut/write-body-as-bytes mock-handler)
          response (middleware {})]
      (is (bytes? (:body response)))
      (is (= data (json/read-value (:body response) json/keyword-keys-object-mapper)))))
  (testing "should return response as is if body is missing"
    (let [mock-handler (fn [_] {:status 204})
          middleware (sut/write-body-as-bytes mock-handler)
          response (middleware {})]
      (is (not (contains? response :body)))
      (is (= 204 (:status response))))))

(deftest wrap-exception-test
  (testing "should catch generic exception and return 500"
    (let [mock-handler (fn [_] (throw (Exception. "Critical engine failure")))
          middleware (sut/wrap-exception mock-handler)
          response (middleware {})]
      (is (= 500 (:status response)))
      (is (= "Critical engine failure" (get-in response [:body :message])))
      (is (= "Internal Server Error" (get-in response [:body :error])))))
  (testing "should return specific status code when using ex-info"
    (let [mock-handler (fn [_] (throw (ex-info "Invalid XML" {:status 400})))
          middleware (sut/wrap-exception mock-handler)
          response (middleware {})]
      (is (= 400 (:status response)))
      (is (= "Invalid XML" (get-in response [:body :message])))
      (is (= "Client Error" (get-in response [:body :error]))))))
