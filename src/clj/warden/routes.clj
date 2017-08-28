(ns warden.routes
  (:require [warden.users :as users]
            [warden.authentication :as auth]
            [boomerang.http :refer [with-body
                                    body-response
                                    not-acceptable]]
            [clojure.string :as str]
            [compojure.core :as compojure :refer [GET POST]]
            [compojure.route :as route]
            [taoensso.timbre :as log]))

(defn ^:private create-token
  [{:keys [user-manager authenticator]} request]
  (try
    (or (not-acceptable request #{"text/plain"})
        (with-body [credentials :warden/credentials request]
          (if-let [user (users/authenticate user-manager credentials)]
            {:status 201
             :headers {"Content-Type" "text/plain"}
             :body (auth/token authenticator (:warden/username credentials))}
            {:status 401
             :headers {"Content-Type" "text/plain"}
             :body "Authentication failed."})))
    (catch Exception e
      (log/error e "An exception was thrown while processing a request.")
      {:status 500
       :headers {"Content-Type" "text/plain"}
       :body "An error occurred."})))

(defn routes
  [{:keys [user-manager authenticator] :as deps}]
  (letfn [(unauthenticated [request]
            (when-not (auth/authenticated? authenticator request)
              {:status 401}))]
    (compojure/routes
     (GET "/api/health-check" request
          (body-response 200 request {:warden/message "Health is OK!"}))
     (GET "/api/token-check" request
          (or (unauthenticated request)
              (body-response 200 request {:warden/message "Token is OK!"})))
     (POST "/api/tokens" request (create-token deps request))
     (fn [request] (body-response 404 request {:warden/message "Not found."})))))
