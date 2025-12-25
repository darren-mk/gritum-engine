(ns gritum.extract-test
  (:require
   [clojure.test :as t]
   [gritum.extract :as sut]))

(t/deftest jump-map-test
  (let [sample {:tag :A :content ["val"]}]
    (t/testing "returns content when tag matches"
      (t/is (= ["val"] (sut/jump-map sample :A))))
    (t/testing "returns nil when tag does not match"
      (t/is (nil? (sut/jump-map sample :B))))))

(t/deftest jump-vec-test
  (let [sample [{:tag :A :content [1]}
                {:tag :B :content [2]}]]
    (t/testing "returns content when exactly one matching tag exists in the list"
      (t/is (= [1] (sut/jump-vec sample :A))))
    (t/testing "throws exception if tag is missing or duplicated"
      (t/is (thrown? clojure.lang.ExceptionInfo (sut/jump-vec sample :C)))
      (let [dups [{:tag :A} {:tag :A}]]
        (t/is (thrown? clojure.lang.ExceptionInfo (sut/jump-vec dups :A)))))))

(t/deftest jump-test
  (t/testing "dispatches to appropriate logic based on input type (Map/Vector)"
    (t/is (= ["val"] (sut/jump {:tag :root :content ["val"]} :root)))
    (t/is (= ["val"] (sut/jump [{:tag :child :content ["val"]}] :child)))))

(t/deftest traverse-test
  (let [sample {:tag :root
                :content [{:tag :mid
                           :content [{:tag :leaf :content ["END"]}]}]}]
    (t/testing "traverses deep down the path and returns the final content"
      (t/is (= ["END"]
               (sut/traverse sample [:root :mid :leaf]))))))
