(defproject flowjur "0.1.0"
  :description "flowjur is a Clojure library designed to run stateful functions as part of a workflow."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://github.com/hvitorino/flowjur"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :repl-options {:init-ns flowjur.core}
  :profiles {:kaocha {:dependencies [[lambdaisland/kaocha "0.0-565"]]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]})
