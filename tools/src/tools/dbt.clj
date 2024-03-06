(ns tools.dbt
  (:require [codax.core :as c]))

(def db-t (c/open-database! "data/tools-database"))

(c/with-write-transaction [db-t tx]
  (c/assoc-at tx [:counters] {:id 0 :tools 0}))

(defn add-tool ;; works fine
  [tool-name tool-desc]
  (c/with-write-transaction [db-t tx]
    (let [tool-id (c/get-at tx [:counters :id])
          tool {:id tool-id
                :tool-name tool-name
                :tool-desc tool-desc
                :timestamp nil
                :user-guardian nil}]
      (-> tx
          (c/assoc-at [:tools tool-id] tool)
          (c/update-at [:counters :id] inc)
          (c/update-at [:counters :tools] inc)))))

(defn get-tool-by-id ;;works fine
  [tool-id]
  (c/get-at! db-t [:tools tool-id]))

(defn remove-tool ;; works fine
  [tool-id]
  (when (c/get-at! db-t [:tools tool-id])
    (c/with-write-transaction [db-t tx]
      (-> tx
          (c/dissoc-at [:tools tool-id])
          (c/update-at [:counters :tools] dec)))))

(defn get-all-tools [] ;; works fine
  (c/get-at! db-t [:tools]))

(defn get-tools-counters [] ;; works fine
  (c/get-at! db-t [:counters]))

(defn set-tool-user [user-id tool-id] ;; works fine
  (when (c/get-at! db-t [:tools tool-id])
    (c/with-write-transaction [db-t tx]
      (-> tx
          (c/assoc-at [:tools tool-id :user-guardian] user-id)
          (c/assoc-at [:tools tool-id :timestamp] (System/currentTimeMillis))))))

(defn remove-tool-user [tool-id] ;; works fine
  (when (c/get-at! db-t [:tools tool-id])
    (c/with-write-transaction [db-t tx]
      (-> tx
          (c/assoc-at [:tools tool-id :user-guardian] nil)
          (c/assoc-at [:tools tool-id :timestamp] nil)))))

(defn get-user-by-tool [tool-id]
  (when (c/get-at! db-t [:tools tool-id])
    (c/get-at! db-t [:tools tool-id :user-guardian])))

;; (add-tool "Ключ на 10" "Момент ключа 12")

;; (remove-tool 0)

;; (doseq  [i (range 4)]
;;   (add-tool (str "Ключ на " i) "Момент ключа 242"))

