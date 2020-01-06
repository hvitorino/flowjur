# flowjur

A Clojure library designed to run clojure functions as part of a workflow. 

Each step should have a name and a handler wich is a function of arity 1 designed
to receive a context hashmap used to read/store shared data throught all steps of 
the workflow.

All functions should take a context and forward the context to keep the state flowing
throught the execution, including the error handler, so flow's output shows what's
happened during execution.  

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

(def run-forrest {:context       {}
                  :steps         [right-foot 
                                  left-foot 
                                  right-foot 
                                  left-foot]
                  :error-handler handle-error)

(flow run-forrest)
```

### Sub flow

```
(def jump
  (step {:name    ::jump
         :handler (fn [ctx]
                    (assoc ctx :jump-data "jump"))))

(def right-foot 
  (step {:name    ::right-foot
         :handler (fn [ctx]
                    (-> (if (obstacle? ctx)
                          jump
                          ctx) 
                        (assoc ctx :right-foot-data "right foot step")))))

(def left-foot 
  (step {:name    ::lef-foot
         :handler (fn [ctx]
                    (-> (if (obstacle? ctx)
                          jump
                          ctx) 
                        (assoc ctx :left-foot-data "left foot step")))))

(defn handle-error [ctx]
  (println "Ouch!")
  (println (-> ctx :error :exception))
  ctx)

(def run-forrest {:context       {}
                  :steps         [right-foot 
                                  left-foot 
                                  right-foot 
                                  left-foot]
                  :error-handler handle-error)

(flow run-forrest)
```

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
