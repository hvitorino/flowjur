# flowjur

Flowjur is a Clojure library designed to run stateful functions as part of a workflow.

Each step has a name and a handler, which is a function of arity one designed to receive 
a context hashmap used to read/store shared data through all steps of the workflow.

All functions must take a context and return it to keep the state flowing through the execution,
including the error handler, so the flow output will have the necessary information in case of an
error happening in one of the steps.  

## Usage

### Simple flow

```
(def right-foot 
  (step {:name    ::right-foot
         :handler (fn [ctx]
                    (assoc ctx :right-foot-data "right foot step"))))

(def left-foot 
  (step {:name    ::lef-foot
         :handler (fn [ctx]
                    (assoc ctx :left-foot-data "left foot step"))))

(defn handle-error [ctx]
  (println "Ouch!")
  (println (-> ctx :error :exception))
  ctx)

(def run-forrest {:context      {}
                  :steps        [right-foot left-foot]
                  :handle-error handle-error)

(flow run-forrest)
```

### Sub flow

```
(def jump
  (step {:name    ::jump
         :handler (fn [ctx]
                    (assoc ctx :jump-data "jump"))))

(def jump-forrest {:context {}
                   :steps   [jump]})

(def right-foot 
  (step {:name    ::right-foot
         :handler (fn [ctx]
                    (assoc ctx :right-foot-data "right foot step")))))

(def left-foot 
  (step {:name    ::lef-foot
         :handler (fn [ctx]
                    (-> (subflow ctx jump-forrest) 
                        (assoc :left-foot-data "left foot step")))))

(defn handle-error [ctx]
  (println "Ouch!")
  (println (-> ctx :error :exception))
  ctx)

(def run-forrest {:context       {}
                  :steps         [right-foot 
                                  left-foot
                  :error-handler handle-error)

(flow run-forrest)
```

## License

Copyright © 2018-2019 hvitorino

Distributed under the Eclipse Public License 2.0.