(ns warden.main
  (:require [warden.system :as system]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [taoensso.timbre :as log])
  (:gen-class :main true))

(def config {:warden/id "warden-server"
             :warden/log-path "/tmp"
             :warden/secret-key "secret"
             :warden/user-manager-type :atomic
             :warden/users {"mike" "rocket"}})

(defn -main
  [& [port]]
  (log/set-level! :debug)
  (let [port (Integer. (or port (env :port) 5000))
        api-token (str/trim (slurp (io/resource "token.txt")))]
    (log/info (str "Using port " port "."))
    (let [system (system/system (merge config {:warden/port port}))]
      (log/info "Starting system.")
      (component/start-system system)
      (log/info "Waiting forever.")
      @(promise))))
