(ns tools.dbu
  (:require [codax.core :as c]))

(def db-u (c/open-database! "data/users-database"))

(c/with-write-transaction [db-u tx]
  (c/assoc-at tx [:counters] {:id 0 :users 0}))

(defn add-user ;; Works fine
  [user-first-name user-second-name user-descr]
  (c/with-write-transaction [db-u tx]
    (let [user-id (c/get-at tx [:counters :id])
          user {:id user-id
                :user-first-name user-first-name
                :user-second-name user-second-name
                :user-descr user-descr
                :tools-in-use []}]
                ;; :timestamp (System/currentTimeMillis) ADD LATEER TO TOOLS
      (-> tx
          (c/assoc-at [:users user-id] user)
          (c/update-at [:counters :id] inc)
          (c/update-at [:counters :users] inc)))))

(defn get-user-by-id ;; works fine
  [user-id]
  (c/get-at! db-u [:users user-id]))

(defn remove-user ;; works fine
  [user-id]
  (when (c/get-at! db-u [:users user-id])
    (c/with-write-transaction [db-u tx]
      (-> tx
          (c/dissoc-at [:users user-id])
          (c/update-at [:counters :users] dec)))))

(defn get-all-users [] ;; works fine
  (c/get-at! db-u [:users]))

(defn get-users-counters [] ;; works fine
  (c/get-at! db-u [:counters]))

(defn add-tool-to-user [user-id tool-id]  ;;works fine
  (when (and (c/get-at! db-u [:users user-id]) (not (some  #(= tool-id %) (c/get-at! db-u [:users user-id :tools-in-use]))))
    (c/with-write-transaction [db-u tx]
      (-> tx
          (c/update-at [:users user-id :tools-in-use] conj tool-id)))))

(defn get-user-tools [user-id] ;; works fine
  (when (c/get-at! db-u [:users user-id])
    (c/get-at! db-u [:users user-id :tools-in-use])))

(defn remove-tool-from-user [user-id tool-id] ;; works fine 
  (when (c/get-at! db-u [:users user-id])
    (c/with-write-transaction [db-u tx]
      (-> tx
          (c/update-at [:users user-id :tools-in-use] (fn [ll] (into [] (remove #(= tool-id %) ll))))))))

;; (add-user "Mikhail" "Oskilko" "simple worker")
;; (add-user "Admin" "New" "Manager")

;; (doseq [i (range 4)]
;;   (add-user (str "Admin " i) "New" "new new Manager"))

;; (println (c/get-at! db-u [:users 0]))
;; (add-tool-to-user 0 1)
;; (add-tool-to-user 0 2)
;; (remove-tool-from-user 0 1)
;; (add-tool-to-user 0 3)
;; (println (c/get-at! db-u [:users 0]))

