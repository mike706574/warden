(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [clojure.walk :as walk]
   [cognitect.transit :as transit]
   [com.stuartsierra.component :as component]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]

   [aleph.http :as http]
   [taoensso.timbre :as log]

   [boomerang.message :as message]

   [warden.client :as client]
   [warden.users :as users]
   [warden.system :as system]))

(log/set-level! :trace)

(stest/instrument)

(def port 8001)

(def config {:warden/id "warden-server"
             :warden/port port
             :warden/log-path "/tmp"
             :warden/secret-key "secret"
             :warden/user-manager-type :atomic
             :warden/users {"mike" "rocket"}})

(defonce system nil)

(def url (str "http://localhost:" port))

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'system (constantly (system/system config)))
  :init)

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (try
    (alter-var-root #'system component/start-system)
    :started
    (catch Exception ex
      (log/error (or (.getCause ex) ex) "Failed to start system.")
      :failed)))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop-system s))))
  :stopped)

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after `go))

(defn restart
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (go))

(comment
  (def client (client/client {:host (str "localhost:" port)
                              :content-type "application/json"}))

  client

  (def client (client/authenticate client {:warden/username "mike"
                                         :warden/password "rocket"}))

  client

  (client/token-check client)
  (client/health-check client)


  (client/token (str "localhost:" port) {:warden/username "mike"
                                         :warden/password "rocket"})
  )
