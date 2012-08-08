(ns startingclojure.app
  (:use (compojure handler
                   [core :only (GET POST defroutes)]))
  (:require [net.cgrand.enlive-html :as en]
            [ring.util.response :as response]
            [ring.adapter.jetty :as jetty])
  )
  
(def counter (atom 10000))

(defonce urls (atom {}))

(defn shorten
  [url]
  (let [id (swap! counter inc)
        id (Long/toString id 36)]
    (swap! urls assoc id url)
    id)
  )

(en/deftemplate homepage
  (en/xml-resource "homepage.html")
  [request]
  [:#listing :li] (en/clone-for [[id url] @urls]
                               [:a] (comp
                                      (en/content (format "%s : %s" id url))
                                      (en/set-attr :href (str \/ id))))
  )
        
(defn redirect
  [id]
  (response/redirect (@urls id))
  )


(defroutes app*
  (GET "/" request (homepage request))
  (POST "/shorten" request 
        (let [id (shorten (-> request :params :url))]
          (response/redirect "/")))
  (GET "/:id" [id] (redirect id))
  )

(def app (compojure.handler/site app*))

;;(defn -main []
;;  (jetty/run-jetty app {:port 8080 :join? false}))
