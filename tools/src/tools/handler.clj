(ns tools.handler
  (:use ring.util.response)
  (:use ring.middleware.edn)
  (:require [compojure.core :refer :all]
            [compojure.coercions :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.codec :refer [form-decode]]
            [ring.middleware.json :as middleware]
            [tools.dbu :refer :all]
            [tools.dbt :refer :all]
            [tools.dbut :refer :all])
  (:gen-class))

(defn getKeyValueMap [query]
  (keywordize-keys (form-decode query)))

(defn hande-posts [title]
  (pr-str {:msg title}))

(defroutes app-routes
  (GET "/posts" [title] (hande-posts title))

  (GET "/user/add/:first-name/:second-name/:disc" [first-name second-name disc] (str (add-user first-name second-name disc)))
  (GET "/tool/add/:tool-name/:disc" [tool-name disc] (str (add-tool tool-name disc)))

  (GET "/user/remove/:id" [id :<< as-int] (pr-str (remove-user-tools id)))
  (GET "/tool/remove/:id" [id :<< as-int] (pr-str (remove-tool-at-all id)))

  (GET "/users" [] (pr-str (get-all-users)))
  (GET "/tools" [] (pr-str (get-all-tools)))

  (GET "/user/get/:id" [id :<< as-int] (pr-str (get-user-by-id id)))
  (GET "/tool/:id" [id :<< as-int] (pr-str (get-tool-by-id id)))
  (GET "/tool/owner/:user-id/:tool-id" [tool-id :<< as-int user-id :<< as-int] (pr-str (change-owner user-id tool-id)))

  (route/not-found "Not Found"))

  ;; (GET "/hello/:name" [name] (str "Hello " name))
  ;; (GET "/posts" [:as {query :query-string params :params}]
  ;;   (let [mmap (getKeyValueMap query)
  ;;         title (:title mmap)
  ;;         author (:author mmap)]
  ;;     (pr-str {:msg "test23"}))))

(def app (-> app-routes
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:delete :get
                                                       :patch :post :put])
             (wrap-edn-params)
             (wrap-defaults site-defaults)))