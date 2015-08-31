(ns emem.html
  (:require [clojure.string :as s]
            [markdown.core :as md]
            [hiccup.core :as hi])
  (:use emem.util))

(def ^:private default-style
  "Default style for the syntax highlighter."
  "ewan")

(defn html-page
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [title (or (:title opts)
                  (:titlehead opts)
                  (when (in? args) "")
                  (when-let [line (first-line (first args))]
                    line))
        header (or (:header opts) (:titlehead opts))
        icon (or (:icon opts) "static/ico/glider.ico")
        css (or (:css opts) "static/css/main.css")
        style (str "static/css/"
                   (or (:style opts) default-style)
                   ".css")]
    (str
     "<!DOCTYPE html>\n"
     (hi/html
      [:html
       [:head
        (quo title [:title title])
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, user-scalable=yes"}]
        ;; quo
        (when (not (:plain opts))
          (hi/html
           [:link {:rel "apple-touch-icon" :sizes "57x57" :href "static/ico/apple-touch-icon-57x57.png"}]
           [:link {:rel "apple-touch-icon" :sizes "60x60" :href "static/ico/apple-touch-icon-60x60.png"}]
           [:link {:rel "apple-touch-icon" :sizes "72x72" :href "static/ico/apple-touch-icon-72x72.png"}]
           [:link {:rel "apple-touch-icon" :sizes "76x76" :href "static/ico/apple-touch-icon-76x76.png"}]
           [:link {:rel "apple-touch-icon" :sizes "114x114" :href "static/ico/apple-touch-icon-114x114.png"}]
           [:link {:rel "apple-touch-icon" :sizes "120x120" :href "static/ico/apple-touch-icon-120x120.png"}]
           [:link {:rel "apple-touch-icon" :sizes "144x144" :href "static/ico/apple-touch-icon-144x144.png"}]
           [:link {:rel "apple-touch-icon" :sizes "152x152" :href "static/ico/apple-touch-icon-152x152.png"}]
           [:link {:rel "apple-touch-icon" :sizes "180x180" :href "static/ico/apple-touch-icon-180x180.png"}]
           [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-32x32.png" :sizes "32x32"}]
           [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-194x194.png" :sizes "194x194"}]
           [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-96x96.png" :sizes "96x96"}]
           [:link {:rel "icon" :type "image/png" :href "static/ico/android-chrome-192x192.png" :sizes "192x192"}]
           [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-16x16.png" :sizes "16x16"}]
           [:link {:rel "manifest" :href "static/ico/manifest.json"}]
           [:link {:rel "shortcut icon" :href "static/ico/favicon.ico"}]
           [:meta {:name "msapplication-TileColor" :content "#da532c"}]
           [:meta {:name "msapplication-TileImage" :content "static/ico/mstile-144x144.png"}]
           [:meta {:name "msapplication-config" :content "static/ico/browserconfig.xml"}]
           [:meta {:name "theme-color" :content "#ffffff"}]
           [:link {:rel "icon" :href icon :type "image/x-icon"}]
           [:link {:rel "stylesheet" :href css :media "all"}]
           (when-not (= (:style opts) "-")
             (hi/html
              [:link {:rel "stylesheet" :href style :media "all"}]
              [:script {:src "static/js/highlight.pack.js"}]
              [:script "hljs.initHighlightingOnLoad();"]))))]
       [:body
        (quo header [:h1 header])
        [:div {:id "content"}
         text]]]))))

(defn markdown
  "Returns a Markdown string converted to HTML."
  [str]
  (md/md-to-html-string str))

(defn html
  "Converts Markdown inputs to HTML strings."
  [opts args]
  (let [text (if (:merge opts)
               (s/join (map #(markdown (slurp %)) args))
               (markdown (slurp (first args))))]
    (if (:raw opts)
      text
      (html-page opts args text))))

(defn html-name
  "Returns the HTML name of PATH"
  [path]
  (if (empty? path)
    nil
    (str (abs-file-name path) ".html")))
