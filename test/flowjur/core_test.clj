(ns flowjur.core-test
  (:require [clojure.test :refer :all]
            [flowjur.core :refer :all])
  (:import (clojure.lang ExceptionInfo)))

(deftest creating-steps
  (testing "Valid step"
    (let [new-step (step {:name    ::step1
                          :handler (fn [ctx] ctx)})]
      (is true (step? new-step))))

  (testing "Name should be a keyword"
    (is (thrown? ExceptionInfo (step {:name    "step1"
                                      :handler (fn [ctx] ctx)}))))

  (testing "Name is mandatory"
    (is (thrown? ExceptionInfo (step {:handler (fn [ctx] ctx)}))))

  (testing "Handler should be a function"
    (is (thrown? ExceptionInfo (step {:name    ::step1
                                      :handler "run"}))))

  (testing "Handler is mandatory"
    (is (thrown? ExceptionInfo (step {:name    ::step1
                                      :handler "run"})))))

(deftest flows
  (testing "Settings"

    (testing "Valid flow"
      (let [step1 (step {:name ::step1 :handler (fn [ctx] ctx)})
            flow1 {:context       {}
                   :steps         [step1]
                   :error-handler (fn [ctx] (throw (-> ctx :error :exception)))}]
        (is true (nil? (throw-if-invalid-flow flow1)))))

    (testing "Valid flow without error handler"
      (let [step1 (step {:name ::step1 :handler (fn [ctx] ctx)})
            flow1 {:context {}
                   :steps   [step1]}]
        (is true (nil? (throw-if-invalid-flow flow1)))))

    (testing "Valid flow without initial context"
      (let [step1 (step {:name ::step1 :handler (fn [ctx] ctx)})
            flow1 {:steps [step1]}]
        (is true (nil? (throw-if-invalid-flow flow1)))))

    (testing "Invalid flow without steps"
      (let [flow1 {:context       {}
                   :error-handler (fn [ctx] (throw (-> ctx :error :exception)))}]
        (is (thrown? ExceptionInfo (throw-if-invalid-flow flow1))))))

  (testing "Execution"

    (testing "Exapected behaviour"
      (testing "Simple flow"
        (let [step1 (step {:name ::step1 :handler (fn [ctx] (assoc ctx :executed true))})
              flow1 {:context {}
                     :steps   [step1]}
              output (flow flow1)]
          (is true (:executed output))))

      (testing "Two steps flow"
        (let [step1 (step {:name ::step1 :handler (fn [ctx] (assoc ctx :executed-1 true))})
              step2 (step {:name ::step2 :handler (fn [ctx] (assoc ctx :executed-2 true))})
              flow1 {:context {}
                     :steps   [step1 step2]}
              output (flow flow1)]
          (is true (:executed-1 output))
          (is true (:executed-2 output))))

      (testing "Flow -> Step transformation"
        (let [step1 (step {:name ::step1 :handler (fn [ctx] (assoc ctx :executed-1 true))})
              step2 (step {:name ::step2 :handler (fn [ctx] (assoc ctx :executed-2 true))})
              flow1 {:context {}
                     :steps   [step1]}
              flow2 {:context {}
                     :steps   [(flow->step ::subflow flow1)
                               step2]}
              output (flow flow2)]
          (is true (:executed-1 output))
          (is true (:executed-2 output)))))

    (testing "Error handling"

      (testing "Default error handler propagates exception"
        (let [step1 (step {:name ::step1 :handler (fn [ctx] (throw (ex-info "Expected error" {:ctx ctx})))})
              flow1 {:context {}
                     :steps   [step1]}]
          (is (thrown-with-msg? ExceptionInfo #"Expected error" (flow flow1)))))

      (testing "Context contains error"
        (let [ex (ex-info "Expected error" {:ctx nil})
              step1 (step {:name          ::step1
                           :handler       (fn [_] (throw ex))
                           :error-handler (fn [ctx] ctx)})
              flow1 {:context      {}
                     :steps        [step1]
                     :handle-error (fn [ctx] ctx)}
              output (flow flow1)]
          (is true (contains? output :error))))

      (testing "Error contains step name"
        (let [ex (ex-info "Expected error" {:ctx nil})
              step1 (step {:name          ::step1
                           :handler       (fn [_] (throw ex))
                           :error-handler (fn [ctx] ctx)})
              flow1 {:context      {}
                     :steps        [step1]
                     :handle-error (fn [ctx] ctx)}
              output (flow flow1)]
          (is true (= (-> output :error :name) ::step1))))

      (testing "Error contains exception"
        (let [ex (ex-info "Expected error" {:ctx nil})
              step1 (step {:name          ::step1
                           :handler       (fn [_] (throw ex))
                           :error-handler (fn [ctx] ctx)})
              flow1 {:context      {}
                     :steps        [step1]
                     :handle-error (fn [ctx] ctx)}
              output (flow flow1)]
          (is true (= (-> output :error :exception) ex)))))))
