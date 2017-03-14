(ns emem.html
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [markdown.core :as md]
            [hiccup.core :as hi])
  (:use emem.util
        emem.base64))

(def ^:private default-style
  "Default style for the syntax highlighter."
  "ewan")

(defn inline-css
  "Print inline CSS code, if used"
  [opts]
  (when (:inline opts)
    (hi/html
     [:style {:media "all" :type "text/css"}
      (:inline opts)])))

(defn html-page
  "Wrap CONTENT with HTML necessary for correct page display."
  [opts args content]
  (let [title (or (:title opts)
                  (:titlehead opts)
                  (when (:first-line-title opts)
                    (when-let [line (first-line (first args))]
                      line))
                  (when (in? args) "")
                  (base-name (first args)))
        header (or (:header opts) (:titlehead opts))
        css (or (:css opts) "static/css/main.css")
        style (str "static/css/"
                   (or (:style opts) default-style)
                   ".css")
        highlight "static/js/highlight.pack.js"
        temp (temp-dir)]
    (install-resources temp)
    (str
     "<!DOCTYPE html>\n"
     (hi/html
      [:html
       [:head
        (quo title [:title title])
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport" :content "width=device-width,initial-scale=1.0,user-scalable=yes"}]
        [:meta {:name "robots" :content "noodp,noydir"}]
        (when (:head opts) (str (:head opts)))
        (when (:description opts) (hi/html [:meta {:name "description" :content (:description opts)}]))
        (when (:keywords opts) (hi/html [:meta {:name "keywords" :content (:keywords opts)}]))
        ;; quo
        (when (not (:plain opts))
          (if (:standalone opts)
            (let [pre-body (str
                            (hi/html
                             (when (:icon opts)
                               (hi/html
                                ;; favicon
                                [:link {:rel "apple-touch-icon" :sizes "180x180" :href apple-touch-icon-180x180}]
                                [:link {:rel "icon" :type "image/png" :sizes "16x16" :href icon-16x16}]
                                [:link {:rel "icon" :type "image/png" :sizes "32x32" :href icon-32x32}]
                                [:link {:rel "manifest" :href manifest}]
                                [:link {:rel "mask-icon" :color "#5bbad5" :href mask-icon}]
                                [:meta {:name "theme-color" :content "#ffffff"}]))

                             ;; main.css
                             [:style {:media "all" :type "text/css"}
                              (slurp-remove-newlines temp css)])

                            ;; inline css
                            (inline-css opts)

                            ;; use full page width
                            (when (:full opts)
                              (inline-css {:inline "html { max-width: 100%; }"}))

                            (when-not (= (:style opts) "-")
                              (hi/html
                               ;; ewan.css
                               [:style {:media "all" :type "text/css"} (slurp-remove-newlines temp style)]
                               ;; highlight css
                               [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.9.0/styles/default.min.css"}])))]
              pre-body)
            (hi/html
             (when (:icon opts)
               (hi/html
                ;; favicon
                [:link {:rel "apple-touch-icon" :sizes "180x180" :href "static/ico/apple-touch-icon.png"}]
                [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-16x16.png" :sizes "16x16"}]
                [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-32x32.png" :sizes "32x32"}]
                [:link {:rel "manifest" :href "static/ico/manifest.json"}]
                [:link {:rel "mask-icon" :color "#5bbad5" :href "static/ico/safari-pinned-tab.svg"}]
                [:meta {:name "theme-color" :content "#ffffff"}]))

             ;; main.css
             (if (:standalone-css opts)
               [:style {:media "all" :type "text/css"} (slurp-remove-newlines temp css)]
               [:link {:rel "stylesheet" :href css :media "all"}])

             ;; inline css
             (inline-css opts)

             ;; use full page width
             (when (:full opts)
               (inline-css {:inline "html { max-width: 100%; }"}))

             ;; ewan.css
             (when-not (= (:style opts) "-")
               (hi/html
                (if (:standalone-css opts)
                  [:style {:media "all" :type "text/css"} (slurp-remove-newlines temp style)]
                  [:link {:rel "stylesheet" :href style :media "all"}]))))))]
       (do (delete-directory temp) "")
       [:body
        (quo header [:h1 header])
        [:div {:id "content"} content]
        (if (:standalone opts)
          [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.9.0/highlight.min.js"}]
          [:script {:src "static/js/highlight.pack.js"}])
        [:script "hljs.initHighlightingOnLoad();"]]]))))

(defn markdown
  "Return a Markdown string converted to HTML."
  [str]
  (md/md-to-html-string str))

(defn html
  "Convert Markdown inputs to HTML strings."
  [opts args]
  (let [text (if (:merge opts)
               (s/join (map #(markdown (slurp %)) args))
               (markdown (slurp (first args))))]
    (if (:raw opts)
      text
      (html-page opts args text))))

(defn html-name
  "Return the HTML name of PATH."
  [path]
  (suffix-name path ".html"))

