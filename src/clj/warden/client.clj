(ns warden.client
  (:require [aleph.http :as http]
            [boomerang.message :as message]
            [warden.users :as users]
            [clojure.java.io :as io]))

(defn parse
  [response]
  (let [content-type (get-in response [:headers "content-type"])]
    (if (contains? response :body)
      (update response :body (comp (partial message/decode content-type)))
      response)))

(defn add-user!
  [system username password]
  (users/add! (:user-manager system) {:warden/username username
                                      :warden/password password}))

(defn http-url [host] (str "http://" host))

(defn get-token
  [host credentials]
  (let [response @(http/post (str (http-url host) "/api/tokens")
                             {:headers {"Content-Type" "application/json"
                                        "Accept" "text/plain"}
                              :body (String. (message/encode "application/json" credentials) "UTF-8")
                              :throw-exceptions false})]

    (when (= (:status response) 201)
      (-> response :body slurp))))

(defprotocol Client
  (authenticate [this credentials])
  (health-check [this])
  (token-check [this]))

(defrecord ServiceClient [host content-type token]
  Client
  (authenticate [this credentials]
    (when-let [retrieved-token (get-token host credentials)]
      (assoc this :token retrieved-token)))

  (health-check [this]
    (parse @(http/get (str (http-url host) "/api/health-check")
                      {:headers {"Accept" content-type}
                       :throw-exceptions false})))

  (token-check [this]
    (when token
      (parse @(http/get (str (http-url host) "/api/token-check")
                        {:headers {"Content-Type" content-type
                                   "Accept" content-type
                                   "Authorization" (str "Token " token)}
                         :throw-exceptions false})))))

(defn client
  [{:keys [host content-type]}]
  (map->ServiceClient {:host host
                       :content-type content-type}))
