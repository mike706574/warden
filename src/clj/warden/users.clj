(ns warden.users
  (:require [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]))

(s/def :warden/username string?)
(s/def :warden/password string?)
(s/def :warden/credentials (s/keys :req [:warden/username :warden/password]))

(defprotocol UserManager
  "Abstraction around user storage and authentication."
  (add! [this user] "Adds a user.")
  (authenticate [this credentials] "Authenticates a user."))

(s/def :warden/user-manager (partial satisfies? UserManager))

(s/fdef add!
  :args (s/cat :user-manager :warden/user-manager
               :credentials :warden/credentials)
  :ret :warden/credentials)

(defn ^:private find-by-username
  [users username]
  (when-let [user (first (filter (fn [[user-id user]] (= (:warden/username user) username)) @users))]
    (val user)))

(defrecord AtomicUserManager [counter users]
  UserManager
  (add! [this user]
    (swap! users assoc (str (swap! counter inc))
           (update user :warden/password hashers/encrypt))
    (dissoc user :warden/password))

  (authenticate [this {:keys [:warden/username :warden/password]}]
    (when-let [user (find-by-username users username)]
      (when (hashers/check password (:warden/password user))
        (dissoc user :warden/password)))))

(defmulti user-manager :warden/user-manager-type)

(defmethod user-manager :default
  [{user-manager-type :warden/user-manager-type}]
  (throw (ex-info (str "Invalid user manager type: " (name user-manager-type))
                  {:user-manager-type user-manager-type})))

(defmethod user-manager :atomic
  [config]
  (let [user-manager (AtomicUserManager. (atom 0) (atom {}))]
    (when-let [users (:warden/users config)]
      (doseq [[username password] users]
        (add! user-manager {:warden/username username
                            :warden/password password})))
    user-manager))
