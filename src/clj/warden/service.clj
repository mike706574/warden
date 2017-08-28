(ns warden.service
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [aleph.http :as aleph-http]
            [warden.handler :as handler])
  (:gen-class :main true))

(defn- already-started
  [{:keys [id port] :as service}]
  (log/info (str "Service " id " already started on port " port "."))
  service)

(defn- start-service
  [{:keys [id port] :as service} handler-factory]
  (log/info (str "Starting " id " on port " port "..."))
  (try
    (let [handler (handler/handler handler-factory)
          server (aleph-http/start-server handler {:port port})]
      (log/info (str "Finished starting."))
      (assoc service :server server))
    (catch java.net.BindException e
      (throw (ex-info (str "Port " port " is already in use.") {:id id
                                                                :port port})))))

(defn- stop-service
  [{:keys [id port server] :as service}]
  (log/info (str "Stopping " id " on port " port "..."))
  (.close server)
  (dissoc service :server))

(defn- already-stopped
  [{:keys [id] :as service}]
  (log/info (str id " already stopped."))
  service)

(defrecord AlephService [id port handler-factory server]
  component/Lifecycle
  (start [this]
    (if server
      (already-started this)
      (start-service this handler-factory)))
  (stop [this]
    (if server
      (stop-service this)
      (already-stopped this))))

(defn aleph-service
  [{:keys [:warden/id :warden/port] :as config}]
  (component/using
   (map->AlephService {:id id :port port})
   [:handler-factory]))
