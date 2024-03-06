(ns hello-world.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rd]
   [clojure.edn :as edn]
   [ajax.core :refer [GET]]))

(enable-console-print!)

(def url "http://localhost:3002/")

(defonce tools (atom nil))
(defonce users (atom nil))

(defn make-map [res]
  (into (sorted-map-by (comp compare))
        (edn/read-string res)))

(defn handle-tools [res]
  (reset! tools (make-map res)))
  ;;(println (str  @tools)))

(defn handle-users [res]
  (reset! users (make-map res)))
  ;;;(println (str "users: " @users)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn handler [response]
  (.log js/console (str "title ans - " response)))

(defn get-all-users []
  (GET (str url "users")
    {:handler handle-users
     :error-handler error-handler}))

(defn get-all-tools []
  (GET (str url "tools")
    {:handler handle-tools
     :error-handler error-handler}))

(defn remove-tool [id]
  (GET (str url "tool/remove/" id)
    {:handler (fn [response]
                (get-all-tools)
                (get-all-users)
                (handler response)
                (.requestPermission js/Notification #(js/Notification. "Инструмент удален" (js-obj  "body" (str "Инструмент с id = " id " успешно удален")))))
     :error-handler error-handler}))

(defn remove-user [id]
  (GET (str url "user/remove/" id)
    {:handler (fn [response]
                (get-all-tools)
                (get-all-users)
                (handler response)
                (.requestPermission js/Notification #(js/Notification. "Пользователь успешно удален" (js-obj  "body" (str "Пользователь " id " успешно удален")))))
     :error-handler error-handler}))

(defn change-owner [user-id tool-id]
  (println (str tool-id)
           (GET (str url "tool/owner/" user-id "/" tool-id)
             {:handler (fn [response]
                         (get-all-tools)
                         (get-all-users)
                         (handler response)
                         (.requestPermission js/Notification #(js/Notification. "Владелец замене" (js-obj  "body" (str "Владелец у инструмента " tool-id " успешно поменян на id = " user-id)))))
              :error-handler error-handler})))

;; (GET (str url "user/get/2")
;;   {:handler handler
;;    :error-handler error-handler})

(defonce selected-tool (atom nil))
(defonce coordinates-tool (atom {:x nil :y nil}))
(defonce show-popup-tool? (atom false))
(defonce is-on-popup-tool? (atom false))
;; (defonce selected-in-tool-user (atom nil))

;; (defn setSelectedValue [] 
;;   (when (:user-guardian @selected-tool)
;;                        (-> js/document
;;                            (.getElementById "selectUserTool")
;;                            (.-value)
;;                            (set! (:user-guardian @selected-tool)))))

(defn tools-lister [tools]
  [:div#tools-list
   (for [tool tools]
     ^{:key tool} [:span  {:onMouseOver (fn [event]
                                          (swap! coordinates-tool assoc :x (-> event .-pageX) :y (-> event .-pageY))
                                          (reset! selected-tool tool)
                                          (reset! show-popup-tool? true)
                                          (reset! is-on-popup-tool? false))
                           :onMouseOut (fn [_event] (reset! show-popup-tool? false))}
                   (str (:tool-name tool))
                   [:br]])])

(defonce selected-tool-item-id (reagent/atom {:selected-value -1}))

(defn handle-select-change [event]
  (let [new-value (-> event .-target .-value)]
    (swap! selected-tool-item-id assoc :selected-value new-value)))

(defn popup-tool []
  (when (or @is-on-popup-tool? @show-popup-tool?)
    [:div#popup-user
     {:onMouseOver (fn [_event]
                     (reset! is-on-popup-tool? true)
                     (reset! show-popup-tool? false))
      :onMouseOut (fn [_event]
                    (reset! is-on-popup-tool? false))
      :style {:position "absolute"
              :left (+ (:x @coordinates-tool) 5)
              :top (- (:y @coordinates-tool) 50)}}
     [:p "Информация об инструменте:"]
     [:div (str "ID: " (:id @selected-tool))]
     [:div (str "Наименование инструмента: " (:tool-name @selected-tool))]
     [:div (str "Полное описание инструмента: " (:tool-desc @selected-tool))]
     [:div [:button {:on-click (fn [_event] (remove-tool (:id @selected-tool)) (reset! is-on-popup-tool? false))}
            "Выкинуть инструмент на помойку"]]
     [:div "Какому сотрдунику пренадлежит:"]
     [:div#list-tool-user]
     [:select#selectUserTool {:onChange handle-select-change
                              :defaultValue (if (:user-guardian @selected-tool) (:user-guardian @selected-tool) -1)}
      [:option {:value -1}
       "Раб без владельца"]
      (for [user (vals @users)]
        ^{:key user} [:option {:value (str (:id user))} (str "id: " (:id user) ", Имя: " (:user-first-name user))])]
     [:button {:on-click (fn [_event] (change-owner (:selected-value @selected-tool-item-id) (:id @selected-tool)))}
      "Поменять владельца"]]))

(defonce selected-user (atom nil))
(defonce coordinates-user (atom {:x nil :y nil}))
(defonce show-popup-user? (atom false))
(defonce is-on-popup-user? (atom false))

(defn peopl-lister [users]
  [:div#user-list
   (for [user users]
     ^{:key user} [:span  {:onMouseOver (fn [event]
                                          (swap! coordinates-user assoc :x (-> event .-pageX) :y (-> event .-pageY))
                                          (reset! selected-user user)
                                          (reset! show-popup-user? true)
                                          (reset! is-on-popup-user? false))
                           :onMouseOut (fn [_event] (reset! show-popup-user? false))}
                   (str (:user-second-name user) " " (:user-first-name user))

                   [:br]])])

(defn popup-user []
  (when (or @is-on-popup-user? @show-popup-user?)
    [:div#popup-user
     {:onMouseOver (fn [_event]
                     (reset! is-on-popup-user? true)
                     (reset! show-popup-user? false))
      :onMouseOut  (fn [_event]
                     (reset! is-on-popup-user? false))
      :style       {:position "absolute"
                    :left     (+ (:x @coordinates-user) 5)
                    :top      (- (:y @coordinates-user) 50)}}
     [:p "Полученный пользователь:"]
     [:div (str "ID: " (:id @selected-user))]
     [:div (str "Имя: " (:user-first-name @selected-user))]
     [:div (str "Фамилия: " (:user-second-name @selected-user))]
     [:div (str "Описание сотрудника: " (:user-descr @selected-user))]
     [:div [:button {:on-click (fn [_event] (remove-user (:id @selected-user)) (reset! is-on-popup-user? false))}
            "Уволить сотрудника"]]
     [:div "Список предметов у сотрудника:"]
     [:div#list-tools-user
      (doall (for [tool (vals @tools)]
               (when (= (:id @selected-user) (:user-guardian tool)) ^{:key tool}  [:div (str "ID: " (:id tool) ", Название: " (:tool-name tool) ", Описание: " (:tool-desc tool))])))]]))

(defn label-input [text]
  [:label text])

(defn atom-input [state placeholder-text]
  [:input {:type        "text"
           :placeholder placeholder-text
           :value       @state
           :required true
           :on-change   (fn [event]
                          (reset! state (-> event .-target .-value)))}])

(defonce usernamefirst-input (atom ""))
(defonce usernamesecond-input (atom ""))
(defonce userdiscr-input (atom ""))

(defonce tool-name-input (atom ""))
(defonce tool-desc-input (atom ""))

(defn add-tool-form []
  [:form {:on-submit (fn [e]
                       (.preventDefault e)
                       (GET (str url "tool/add/" @tool-name-input "/" @tool-desc-input)
                         {:handler (fn [response]
                                     (get-all-tools)
                                     (handler response)
                                     (.requestPermission js/Notification #(js/Notification. "Добавление инструмента" (js-obj  "body" (str "Новый инструмент: "  @tool-name-input " успешно добавлен")))))
                          :error-handler error-handler}))}

   (label-input "Введите название инструмента")
   (atom-input tool-name-input "Название")
   (label-input "Введите описание инструмента")
   (atom-input tool-desc-input "Описание")
   (label-input "")
   [:input.submit-btn {:type "submit"}]])

(defn add-user-form []
  [:form {:on-submit (fn [e]
                       (.preventDefault e)
                       (GET (str url "user/add/" @usernamefirst-input "/" @usernamesecond-input "/" @userdiscr-input)
                         {:handler (fn [response]
                                     (get-all-users)
                                     (.requestPermission js/Notification #(js/Notification. "Добавление сотруднкиа" (js-obj  "body" (str "Добавление нового сотрудника " @usernamesecond-input " успешно проведено"))))
                                     (handler response))
                          :error-handler error-handler}))}

   (label-input "Введите имя нового пользоватлея")
   (atom-input usernamefirst-input "Имя")
   (label-input "Введите фамилию нового пользоватлея")
   (atom-input usernamesecond-input "Фамилия")
   (label-input "Введите описание нового пользоватлея")
   (atom-input userdiscr-input "Описание")
   (label-input "")
   [:input.submit-btn {:type "submit"}]])

(defn people-list []
  [:div
   [:div (str @coordinates-user)]
   [:h3 "People list:"]
   [:p "Добавление нового пользователя"]
   (add-user-form)
   [peopl-lister (vals @users)]
   (popup-user)])

(defn tools-list []
  [:div
   [:div (str @coordinates-tool)]
   [:h3 "Tools list"]
   [:p "Добавление нового инструмента"]
   (add-tool-form)
   [tools-lister (vals @tools)]
   (popup-tool)])

;; (defn hello-world []
;;   [:div
;;    [:h1 (:text @app-state)]
;;    [:h3 "Edit this and watch it change!"]])

(get-all-users)
(get-all-tools)

(rd/render
 [:div#main-container

  [:header [:h1 "UI Ведения склада"]]
  [:div#grid-container
   [people-list]
   [tools-list]]]

 (. js/document (getElementById "app")))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

