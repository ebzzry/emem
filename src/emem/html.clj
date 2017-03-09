(ns emem.html
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [markdown.core :as md]
            [hiccup.core :as hi])
  (:use emem.util))

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
                  (when (:file-title opts) (base-name (first args)))
                  (when (in? args) "")
                  (when-let [line (first-line (first args))]
                    line))
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
        [:meta {:http-equiv "Cache-control" :contents "max-age=86400"}]
        [:meta {:name "viewport" :content "width=device-width,initial-scale=1.0,user-scalable=yes"}]
        ;; quo
        (when (not (:plain opts))
          (if (:standalone opts)
            (let [pre-body (str
                            (hi/html
                             ;; favicon
                             [:link {:rel "apple-touch-icon" :sizes "180x180"
                                     :href "data:image/x-icon;base64,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"}]
                             [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAAAAAA6mKC9AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfgCQEGMTZiVlqvAAAA6ElEQVQY02WPvWrCUACFzyQouNkWfQftUopT+gIO1QcJuuYNMmYKZDAJuljIRfdw/4hJ6sVHcWy8i7mNaenS5XD44HDOweasVBamjpOGmVLnDZQxpuJ2r2fzqrGqBToeAsNY/4ErmQATcm1BpqvqwollEX7RWmdYl3nOgpLSMuAkFiFOpjZfx5sxdZGMn+z0pyWvG6VvQN/5D06NbSO330j4mUoRFJQWgfzwZQTmvbxLkVhWIuT81WNwH4DFrhn2vFsAjy5WXWDqj4CRPwW6K+xnnYEn7ueEN+jM9tgeli6L7vcj5i4P22+pxJ2k7Q2AAAAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNi0wOS0wMVQwNjo0OTo1NCswMjowMIIsbGoAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTYtMDktMDFUMDY6NDk6NTQrMDI6MDDzcdTWAAAAV3pUWHRSYXcgcHJvZmlsZSB0eXBlIGlwdGMAAHic4/IMCHFWKCjKT8vMSeVSAAMjCy5jCxMjE0uTFAMTIESANMNkAyOzVCDL2NTIxMzEHMQHy4BIoEouAOoXEXTyQjWVAAAAAElFTkSuQmCC"}]
                             [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAAAAABWESUoAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfgCQEGMTZiVlqvAAAB10lEQVQ4y52TPU8CQRCGl1D7EcUYv34LllhQno3G0wKx0mik0WCFye3d2XiV3OyNf4FQUdCQYEELFscW2mGC/0AO3LvbEzFrTJxq8ubJzsy7MyRvyKBayTTvKL0zzZJGEzVPjEkSrj8ZesWiN5z47pdofAf4ay5NSDr3ytUA9AokikIPlIBX34qBrbqnBBDnY2AeUf1CbSMGNmrqF6Crx4DeVffgcp5NEZLa5r9MIXwYOHt7zmDWBw3cOJheYQyrVWSsojMXPPTABY2U+n4cvNLgMmtUOO/WsP7M/dJsCRmiRP9gfW6z8PKzhwRgg2w0T25oqgF0UhGQ9mw1UN2PHSEn9J/AnyXY23YE7Lybv46pJ2Ne+iqj+sIoLzTqUlgNyB4YAtNvE6tvdQaIDBGE1cZk1DrXTpsfs5/10TzVzluj0Mngfkm0s0ADkN+d5RwCuiDEJScwiNVeiRpebOJ0YbC5GGUrbYtY19KTM5yuHJ5J8doixpHMd+FraR9hV4pHosSNzC9wuvZ4IcUbUaKzFqXLLUwO57iHreUoW+uIEmNYFWnGGcP09GDsZIS4CuNwYYJO+fDqaTR7vKOnq8NyJxA+iPO3LMOyw/Onpk2pbdLw/G0r1I38J5jBb4snJ+z2AAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE2LTA5LTAxVDA2OjQ5OjU0KzAyOjAwgixsagAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNi0wOS0wMVQwNjo0OTo1NCswMjowMPNx1NYAAABXelRYdFJhdyBwcm9maWxlIHR5cGUgaXB0YwAAeJzj8gwIcVYoKMpPy8xJ5VIAAyMLLmMLEyMTS5MUAxMgRIA0w2QDI7NUIMvY1MjEzMQcxAfLgEigSi4A6hcRdPJCNZUAAAAASUVORK5CYII="}]
                             [:link {:rel "manifest" :href "data:image/x-icon;base64,ewoJIm5hbWUiOiAiZW1lbSIsCgkiaWNvbnMiOiBbCgkJewoJCQkic3JjIjogIlwvYW5kcm9pZC1jaHJvbWUtMTkyeDE5Mi5wbmciLAoJCQkic2l6ZXMiOiAiMTkyeDE5MiIsCgkJCSJ0eXBlIjogImltYWdlXC9wbmciCgkJfSwKCQl7CgkJCSJzcmMiOiAiXC9hbmRyb2lkLWNocm9tZS01MTJ4NTEyLnBuZyIsCgkJCSJzaXplcyI6ICI1MTJ4NTEyIiwKCQkJInR5cGUiOiAiaW1hZ2VcL3BuZyIKCQl9CgldLAoJInRoZW1lX2NvbG9yIjogIiNmZmZmZmYiLAoJImRpc3BsYXkiOiAic3RhbmRhbG9uZSIKfQo="}]
                             [:link {:rel "mask-icon" :color "#5bbad5" :href "data:image/x-icon;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/Pgo8IURPQ1RZUEUgc3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDIwMDEwOTA0Ly9FTiIKICJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy1TVkctMjAwMTA5MDQvRFREL3N2ZzEwLmR0ZCI+CjxzdmcgdmVyc2lvbj0iMS4wIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciCiB3aWR0aD0iODk0LjAwMDAwMHB0IiBoZWlnaHQ9Ijg5NC4wMDAwMDBwdCIgdmlld0JveD0iMCAwIDg5NC4wMDAwMDAgODk0LjAwMDAwMCIKIHByZXNlcnZlQXNwZWN0UmF0aW89InhNaWRZTWlkIG1lZXQiPgo8bWV0YWRhdGE+CkNyZWF0ZWQgYnkgcG90cmFjZSAxLjExLCB3cml0dGVuIGJ5IFBldGVyIFNlbGluZ2VyIDIwMDEtMjAxMwo8L21ldGFkYXRhPgo8ZyB0cmFuc2Zvcm09InRyYW5zbGF0ZSgwLjAwMDAwMCw4OTQuMDAwMDAwKSBzY2FsZSgwLjEwMDAwMCwtMC4xMDAwMDApIgpmaWxsPSIjMDAwMDAwIiBzdHJva2U9Im5vbmUiPgo8cGF0aCBkPSJNMTAgNDQ3MCBsMCAtNDQ2MCA0NDYwIDAgNDQ2MCAwIDAgNDQ2MCAwIDQ0NjAgLTQ0NjAgMCAtNDQ2MCAwIDAKLTQ0NjB6IG0yOTIwIDI5MzAgbDAgLTEzOTAgLTEzOTAgMCAtMTM5MCAwIDAgMTM4MyBjMCA3NjEgMyAxMzg3IDcgMTM5MCAzIDQKNjI5IDcgMTM5MCA3IGwxMzgzIDAgMCAtMTM5MHogbTI5MzAgMCBsMCAtMTM5MCAtMTM5MCAwIC0xMzkwIDAgMCAxMzkwIDAKMTM5MCAxMzkwIDAgMTM5MCAwIDAgLTEzOTB6IG0yOTI4IC0yIGwyIC0xMzg4IC0xMzkwIDAgLTEzOTAgMCAwIDEzOTAgMCAxMzkwCjEzODggLTIgMTM4NyAtMyAzIC0xMzg3eiBtLTU4NTggLTI5MjggbDAgLTEzOTAgLTEzOTAgMCAtMTM5MCAwIDAgMTM5MCAwCjEzOTAgMTM5MCAwIDEzOTAgMCAwIC0xMzkweiBtMjkzMCAwIGwwIC0xMzkwIC0xMzkwIDAgLTEzOTAgMCAwIDEzOTAgMCAxMzkwCjEzOTAgMCAxMzkwIDAgMCAtMTM5MHogbTI5MzAgMCBsMCAtMTM5MCAtMTM5MCAwIC0xMzkwIDAgMCAxMzkwIDAgMTM5MCAxMzkwCjAgMTM5MCAwIDAgLTEzOTB6IG0tNTg2MCAtMjkzMCBsMCAtMTM5MCAtMTM4OCAyIC0xMzg3IDMgLTMgMTM4OCAtMiAxMzg3CjEzOTAgMCAxMzkwIDAgMCAtMTM5MHogbTI5MzAgMCBsMCAtMTM5MCAtMTM5MCAwIC0xMzkwIDAgMCAxMzkwIDAgMTM5MCAxMzkwCjAgMTM5MCAwIDAgLTEzOTB6IG0yOTI4IDMgbC0zIC0xMzg4IC0xMzg3IC0zIC0xMzg4IC0yIDAgMTM5MCAwIDEzOTAgMTM5MCAwCjEzOTAgMCAtMiAtMTM4N3oiLz4KPHBhdGggZD0iTTQzNTMgODU2MCBjLTQ3NSAtNDggLTg3NCAtMzgxIC0xMDA3IC04NDEgLTE4OSAtNjU0IDIyNCAtMTMzMiA4OTIKLTE0NjkgMTM3IC0yNyAzMjcgLTI3IDQ2NCAwIDIyMSA0NSA0MDcgMTQyIDU3MSAyOTkgNTI5IDUwMyA0NzcgMTM0NyAtMTA5CjE3ODkgLTc4IDU5IC0yMzcgMTQxIC0zMzMgMTcxIC0xNDAgNDUgLTMzMiA2NSAtNDc4IDUxeiIvPgo8cGF0aCBkPSJNNzIwMSA1NjI0IGMtMjMwIC0zOSAtNDQwIC0xNDUgLTYxMyAtMzEyIC0yMzkgLTIzMCAtMzU4IC01MDkgLTM1OAotODQyIDAgLTMxMyAxMDYgLTU3OSAzMTkgLTgwMyA0NjAgLTQ4NCAxMjE4IC00ODkgMTY4MyAtMTEgODMgODYgMTIyIDEzNyAxODIKMjM5IDE2MiAyODEgMTk1IDY0OCA4NSA5NjMgLTE4NiA1MzMgLTczOSA4NjAgLTEyOTggNzY2eiIvPgo8cGF0aCBkPSJNMTMzMyAyNjk2IGMtNTE3IC05OCAtOTAwIC01MTkgLTk1MyAtMTA0NiAtMzQgLTM0OCAxMDYgLTcxNiAzNjYKLTk1OSAzNTQgLTMzMSA4NzggLTQwOSAxMzA5IC0xOTcgMjkxIDE0NCA0OTcgMzgwIDU5OSA2ODYgNDcgMTQzIDYwIDI0MSA1Mwo0MTkgLTcgMjA4IC01NCAzNjggLTE1OCA1NDMgLTE1OCAyNjYgLTQxNiA0NTggLTcxOSA1MzggLTEwOCAyOCAtMzg0IDM3IC00OTcKMTZ6Ii8+CjxwYXRoIGQ9Ik00MjM4IDI2OTEgYy0yOTMgLTY0IC01MzEgLTIxNiAtNzA3IC00NTEgLTM4MCAtNTA4IC0yNzcgLTEyMzYgMjI5Ci0xNjI0IDU3MSAtNDM5IDE0MDcgLTI1NSAxNzQzIDM4MyAzNTIgNjY3IC0xMCAxNDg1IC03NDEgMTY3OCAtMTM0IDM2IC0zOTMKNDIgLTUyNCAxNHoiLz4KPHBhdGggZD0iTTcxODMgMjY5NSBjLTIyOSAtNDEgLTQ0MyAtMTU1IC02MTMgLTMyNSAtMTcwIC0xNzAgLTI4NSAtMzkwIC0zMjUKLTYxOCAtMTkgLTEwOSAtMTkgLTMwNSAwIC00MTQgMTMyIC03NTggOTY3IC0xMTgyIDE2NjEgLTg0MyA3MTAgMzQ2IDg3OSAxMjcwCjMzOSAxODUyIC0xNjMgMTc3IC00MDIgMzA2IC02NDQgMzQ4IC0xMDIgMTggLTMxOSAxOCAtNDE4IDB6Ii8+CjwvZz4KPC9zdmc+Cg=="}]
                             [:meta {:name "theme-color" :content "#ffffff"}]

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
             ;; favicon
             [:link {:rel "apple-touch-icon" :sizes "180x180" :href "static/ico/apple-touch-icon.png"}]
             [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-16x16.png" :sizes "16x16"}]
             [:link {:rel "icon" :type "image/png" :href "static/ico/favicon-32x32.png" :sizes "32x32"}]
             [:link {:rel "manifest" :href "static/ico/manifest.json"}]
             [:link {:rel "mask-icon" :color "#5bbad5" :href "static/ico/safari-pinned-tab.svg"}]
             [:meta {:name "theme-color" :content "#ffffff"}]

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

