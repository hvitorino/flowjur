(ns flowjur.core)

(defrecord Step [name handler])

(defn step? [s]
  (= (type s) Step))

(defn throw-if-not-step [s]
  (if-not (step? s)
    (throw (ex-info (str "steps vector items must be Step" (pr-str s)) {:step s}))))

(defn throw-if-invalid-name [n]
  (when-not (keyword? n)
    (throw (ex-info (str "name must be keyword; Got: " (pr-str n)) {:name n}))))

(defn throw-if-invalid-handler [h]
  (when-not (fn? h)
    (throw (ex-info (str "handler must be fn with arity 1; Got: " (pr-str h)) {:handler h}))))

(defn throw-if-invalid-step [s]
  (throw-if-not-step s)
  (throw-if-invalid-name (:name s))
  (throw-if-invalid-handler (:handler s)))

(defn throw-if-invalid-flow [f]
  (let [steps (:steps f)]
    (when-not (> (count steps) 0)
      (throw (ex-info "Flow must have at least one step" {:flow f})))
    (-> (map (fn [s] (throw-if-invalid-step s)) steps)
        (doall))))

(defn step [{:keys [name handler]}]
  (throw-if-invalid-name name)
  (throw-if-invalid-handler handler)
  (->Step name handler))

(defn flow [{:keys [context steps handle-error]
             :or   {handle-error (fn [ctx] (throw (-> ctx :error :exception)))}
             :as   settings}]
  (throw-if-invalid-flow settings)
  (loop [curr-context context
         curr-step (first steps)
         next-steps (rest steps)]
    (if (or (nil? curr-step) (:error curr-context))
      curr-context
      (let [{run  :handler
             name :name} curr-step
            next-context (try
                           (run curr-context)
                           (catch Exception e
                             (-> (assoc curr-context :error {:step name :exception e})
                                 (handle-error))))]
        (recur next-context (first next-steps) (rest next-steps))))))

(defn flow->step [name {:keys [steps handle-error]}]
  (step {:name    name
         :handler (fn [ctx]
                    (flow {:context      ctx
                           :steps        steps
                           :handle-error handle-error}))}))