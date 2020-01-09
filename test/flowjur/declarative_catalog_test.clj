(ns flowjur.declarative-catalog-test
    (:require
      [flowjur.core :as flowjur]
      [clojure.test :refer :all]))

;catalog {:flows
;         {:add-to-cart
;          {:context       {}
;           :error-handler :print
;           :steps         [:create-cart
;                           :dec-available-items
;                           :add-item-to-cart
;                           :calculate-total-price]}}
;
;         {:remove-from-cart
;          {:context       {}
;           :error-handler :print
;           :steps         [:inc-available-items
;                           :remove-item-from-cart
;                           :calculate-total-price]}}
;
;         {:finish-order
;          {:context       {}
;           :error-handler :print
;           :steps         [:confirm-order
;                           :delete-cart]}}
;
;         {:cancel-order
;          {:context       {}
;           :error-handler :print
;           :steps         [:delete-cart
;                           :inc-available-items
;                           :delete-order]}}}

(defn compile [flows steps]
      (let [names (keys flows)
            maps (for [n names]
                      (-> (assoc {} :error-handler ((get-in flows [n :error-handler]) steps))
                          (assoc :steps (map #(steps %) (get-in flows [n :steps])))))
            fns (map (fn [m] (fn [ctx] (assoc m :context ctx))) maps)]
           (apply hash-map (interleave names fns))))

(deftest flows-catalog
         (testing "successful flow compilation"
                  (let [flows {:add-to-cart
                               {:error-handler :print-error
                                :steps         [:create-cart]}}

                        steps {:create-cart (fn [ctx] (assoc ctx :data1 "cart created"))
                               :print-error (fn [ctx] (assoc ctx :error "something went wrong"))}

                        compiled-flows (compile flows steps)]

                       (println compiled-flows)
                       (is true (:add-to-cart compiled-flows)))))
;
;         (testing "flow declaration"
;                  (let [flows {:add-to-cart
;                               {:error-handler :print-error
;                                :steps         [:create-cart
;                                                :dec-available-items
;                                                :add-item-to-cart
;                                                :calculate-total-price]}}
;
;                        steps {:create-cart           (fn [ctx] (assoc ctx :data1 "cart created"))
;                               :dec-available-items   (fn [ctx] (assoc ctx :data2 "item count decremented"))
;                               :add-item-to-cart      (fn [ctx] (assoc ctx :data3 (str (:product ctx) "added to cart")))
;                               :calculate-total-price (fn [ctx] (assoc ctx :data4 "new price calculated"))
;                               :print-error           (fn [ctx] (assoc ctx :data5 "something went wrong"))}
;
;                        compiled-flows (flowjur/compile flows steps)
;
;                        output (flowjur/execute compiled-flows :add-to-cart {:product "Xbox Series X"})]
;
;                       (is "Xbox Series X added to cart" (:data2 output)))))
