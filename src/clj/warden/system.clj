(ns warden.system
  (:require [warden.authentication :as auth]
            [warden.handler :as handler]
            [warden.service :as service]
            [warden.users :as users]
            [warden.util :as util]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(defn configure-logging!
  [{:keys [:warden/id :warden/log-path] :as config}]
  (let [log-file (str log-path "/" id "-" (util/uuid))]
    (log/merge-config!
     {:appenders {:spit (appenders/spit-appender
                         {:fname log-file})}})))

(s/def :warden/id string?)
(s/def :warden/port integer?)
(s/def :warden/log-path string?)
(s/def :warden/user-manager-type #{:atomic})
(s/def :warden/users (s/map-of :warden/username :warden/password))
(s/def :warden/config (s/keys :req [:warden/id
                                     :warden/port
                                     :warden/log-path
                                     :warden/user-manager-type]
                               :opt [:warden/users]))

(defn ^:private build
  [config]
  (log/info (str "Building " (:warden/id config) "."))
  (configure-logging! config)
  {:user-manager (users/user-manager config)
   :authenticator (auth/authenticator config)
   :handler-factory (handler/factory config)
   :app (service/aleph-service config)})

(defn system
  [config]
  (if-let [validation-failure (s/explain-data :warden/config config)]
    (do (log/error (str "Invalid configuration:\n"
                        (util/pretty config)
                        "Validation failure:\n"
                        (util/pretty validation-failure)))
        (throw (ex-info "Invalid configuration." {:config config
                                                  :validation-failure validation-failure})))
    (build config)))

(s/fdef system
  :args (s/cat :config :warden/config)
  :ret map?)
