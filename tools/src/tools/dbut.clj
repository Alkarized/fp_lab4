(ns tools.dbut
  (:require [tools.dbt :refer :all]
            [tools.dbu :refer :all]))

(defn remove-user-tools [user-id]
  (let [tool-ids (get-user-tools user-id)]
    (for [id tool-ids]
      (remove-tool-user id)))
  (remove-user user-id))

(defn remove-tool-at-all [tool-id]
  (remove-tool-from-user (get-user-by-tool tool-id) tool-id)
  (remove-tool tool-id))

(defn change-owner [user-id tool-id]
  (if (= -1 user-id) (do (remove-tool-user tool-id)
                         (remove-tool-from-user (get-user-by-tool tool-id) tool-id))
      (do (set-tool-user user-id tool-id)
          (add-tool-to-user user-id tool-id)))
  (println "new tool -" (str (get-tool-by-id tool-id)))
  (println "new user -" (str (get-user-by-id (get-user-by-tool tool-id)))))
