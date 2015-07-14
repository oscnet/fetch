(ns noir.fetch.remotes
  (:use [compojure.core]))

(def remotes (atom {}))

(defn get-remote [remote]
  (get @remotes remote))

(defn add-remote [remote func]
  (swap! remotes assoc remote func))

(defn safe-read [s]
  (binding [*read-eval* false]
    (read-string s)))

(defmacro defremote [remote params & body]
  `(do
    (defn ~remote ~params ~@body)
    (add-remote ~(keyword (name remote)) ~remote)))

(defn call-remote [remote params]
  (if-let [func (get-remote remote)]
    (let [result (apply func params)]
      {:status 202
       :headers {"Content-Type" "application/clojure; charset=utf-8"}
       :body (pr-str result)})
    {:status 404}))

(defroutes fetch-routes
  (POST "/_fetch" [params remote]
    (let [params (safe-read params)
          remote (keyword remote)]
      (call-remote remote params))))
