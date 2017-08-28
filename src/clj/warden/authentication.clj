(ns warden.authentication
  (:require [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [clojure.string :as str]))

(defn ^:private token-from-header
  [request]
  (some-> request
          :headers
          (get "Authorization")
          (str/split #" ")
          second))

(defprotocol IAuthenticator
  (token [this username])
  (authenticated? [this request]))

(defrecord Authenticator [secret-key]
  IAuthenticator
  (token [this username]
    (let [claims {:username username
                  :exp (time/plus (time/now) (time/days 1))}]
      (jwt/sign claims secret-key {:alg :hs512})))
  (authenticated? [this request]
    (try
      (when-let [token (or (token-from-header request)
                           (:token (:params request)))]
        (jwt/unsign token secret-key {:alg :hs512}))
      (catch clojure.lang.ExceptionInfo e nil))))

(defn authenticator
  [config]
  (map->Authenticator {:secret-key (:warden/secret-key config)}))
