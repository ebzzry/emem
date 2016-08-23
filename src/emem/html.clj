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
  "Prints inline CSS code, if used"
  [opts]
  (when (:inline opts)
    (hi/html
     [:style {:media "all" :type "text/css"}
      (:inline opts)])))

(defn html-page
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [title (or (:title opts)
                  (:titlehead opts)
                  (when (in? args) "")
                  (when-let [line (first-line (first args))]
                    line))
        header (or (:header opts) (:titlehead opts))
        css (or (:css opts) "static/css/main.css")
        style (str "static/css/"
                   (or (:style opts) default-style)
                   ".css")
        highlight "static/js/highlight.pack.js"]
    (str
     "<!DOCTYPE html>\n"
     (hi/html
      [:html
       [:head
        (quo title [:title title])
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1.0, user-scalable=yes"}]
        ;; quo
        (when (not (:plain opts))
          (if (:standalone opts)
            (let [temp (temp-dir)]
              (install-resources temp)
              (let [text
                    (str
                     (hi/html
                      ;; favicon
                      [:link {:rel "apple-touch-icon" :sizes "180x180"
                              :href "data:image/x-icon;base64,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"}]
                      [:link {:rel "icon" :type "image/png" :sizes "16x16"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQfgCBcEHg4T3i/BAAABKklEQVQoz23RMUtbYQBG4ef78pnm2oo0lGAkg4tpoBTXortTIV0c/RGu/gj/iVBwcXBS0NJsbQwitWZooXYwFtRrRHM7tJAMnvHwDi+cIKkqgWDkAUlUgEeXSVXLLwELcss4lOmjMOeEupZ/LNkyNLRl6b9pqUdjKlaUla2ojOXkILfjypUd+VgmwYKyoNB04AKnmgrTcnVfk8KFM1Hhub7PmEXbB/s+GiXkbsCtazmC1zZUvfFbb/LDk5TMeOmPKc803Ho0ZRY/vLBtzyAJahoWdc0oa+LUKx10ZSI1bR1Du1ZtGhjYtGrXnY62WklmzZpk3jfvLarIXFuXmdN3GEVd5+g5duTevSPHeviuKwY1LQ1vfXI3EavinS9+OgmSKkaiNJH7wUjE5V9MR1wVsI9MVgAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNi0wOC0yM1QwNDozMDoxNCswMjowMDdq3G4AAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTYtMDgtMjNUMDQ6MzA6MTQrMDI6MDBGN2TSAAAAV3pUWHRSYXcgcHJvZmlsZSB0eXBlIGlwdGMAAHic4/IMCHFWKCjKT8vMSeVSAAMjCy5jCxMjE0uTFAMTIESANMNkAyOzVCDL2NTIxMzEHMQHy4BIoEouAOoXEXTyQjWVAAAAAElFTkSuQmCC"}]
                      [:link {:rel "icon" :type "image/png" :sizes "32x32"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAQAAADZc7J/AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQfgCBcEHg4T3i/BAAAC10lEQVRIx6XVTWhcVRQH8N97M68z00xSsFGwlezbRRc1RFOFVJDW6KK0YDdtEUIEdeVCEEHdd1FcWN0olfgJVg3diDaCUWrSVlswQheuahAKmtii+Zg4H8/FezO8FzO1wbN653/P/d9z7vm/c/mfFiDUL1qHx4L/ROoWtIroN25enFkq2W1OQ6yMmkDRHtes5Y4e8LbfiojMez/H3edJH9juKfsxbcKiyCf+zEUdF1HsUlqs35sOCzDqQc/lMsxYN4KmIw6lVQcOmbK6cWDYhaBgSCHjPZDx7oggXnfiSrcSuhN8aanjLTm/WYLQlJNugVtOmuoWmVziFr0ZmcR6VZS84aqH8J1ZJRW9OTHFtrQJYrsdzaVfMaiuoeVn3OOwokGsagoFmgoCu3yREATmfJjLq0/de+oZJMJZNQcdVHHJpxYcF7RLaGnkCBqa6uuwpsALXtSDEx71TLLeTUj/tqZhz+tJr/iIi369XRc2auywuzL+iMrmCDai3BRBYNYfmc3TapsjKJj1mmXQMmki0UQxvZRiTiJFBREdLBYpiJ3yk8dUXPSZxWRvIqQ9IrFmmmqoYhANLXGKFA2LrKqbEWp6QmiXr9pCuuZjW40YtGraVVWcxT77MGMGkUlL9tqv4gfnrTjaznCnY3qdtiwWu2HcNmN6vOKmWOyml/UYs824G2KxZaf1OmYnBfQZcL9XlUDVoBlV9zmlD5QN+VHRVq+7F0T2mrfmF38lXSgZzWhyhxGBA6odpOqAwIgdHaRoNDkwTLtQzrWsIkh0dhuknOxNCGq+zywtuaLpUtoVpN6VzIzicl5IZ1xIvxomfCMy6Vw6xmLnTIp8a6Lzh17wTruWhOa6E8YMqfncRwgteNasR/C1dy0KrXjJnMeVXXbGdQ9LtXa3p81roaxlTaDc5WmriZWEaggNeMvvyeO6PZlvmZnXSouLO4JudcptR/1tUeuO/6Vu9g9pCNcua1EXmwAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNi0wOC0yM1QwNDozMDoxNCswMjowMDdq3G4AAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTYtMDgtMjNUMDQ6MzA6MTQrMDI6MDBGN2TSAAAAV3pUWHRSYXcgcHJvZmlsZSB0eXBlIGlwdGMAAHic4/IMCHFWKCjKT8vMSeVSAAMjCy5jCxMjE0uTFAMTIESANMNkAyOzVCDL2NTIxMzEHMQHy4BIoEouAOoXEXTyQjWVAAAAAElFTkSuQmCC"}]
                      [:link {:rel "manifest"
                              :href "data:image/x-icon;base64,ewoJIm5hbWUiOiAiZW1lbSIsCgkiaWNvbnMiOiBbCgkJewoJCQkic3JjIjogIlwvYW5kcm9pZC1jaHJvbWUtMTkyeDE5Mi5wbmciLAoJCQkic2l6ZXMiOiAiMTkyeDE5MiIsCgkJCSJ0eXBlIjogImltYWdlXC9wbmciCgkJfSwKCQl7CgkJCSJzcmMiOiAiXC9hbmRyb2lkLWNocm9tZS01MTJ4NTEyLnBuZyIsCgkJCSJzaXplcyI6ICI1MTJ4NTEyIiwKCQkJInR5cGUiOiAiaW1hZ2VcL3BuZyIKCQl9CgldLAoJInRoZW1lX2NvbG9yIjogIiNmZmZmZmYiLAoJImRpc3BsYXkiOiAic3RhbmRhbG9uZSIKfQo="}]
                      [:link {:rel "mask-icon" :color "#5bbad5"
                              :href "data:image/x-icon;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/Pgo8IURPQ1RZUEUgc3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDIwMDEwOTA0Ly9FTiIKICJodHRwOi8vd3d3LnczLm9yZy9UUi8yMDAxL1JFQy1TVkctMjAwMTA5MDQvRFREL3N2ZzEwLmR0ZCI+CjxzdmcgdmVyc2lvbj0iMS4wIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciCiB3aWR0aD0iMTAyNC4wMDAwMDBwdCIgaGVpZ2h0PSIxMDI0LjAwMDAwMHB0IiB2aWV3Qm94PSIwIDAgMTAyNC4wMDAwMDAgMTAyNC4wMDAwMDAiCiBwcmVzZXJ2ZUFzcGVjdFJhdGlvPSJ4TWlkWU1pZCBtZWV0Ij4KPG1ldGFkYXRhPgpDcmVhdGVkIGJ5IHBvdHJhY2UgMS4xMSwgd3JpdHRlbiBieSBQZXRlciBTZWxpbmdlciAyMDAxLTIwMTMKPC9tZXRhZGF0YT4KPGcgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoMC4wMDAwMDAsMTAyNC4wMDAwMDApIHNjYWxlKDAuMTAwMDAwLC0wLjEwMDAwMCkiCmZpbGw9IiMwMDAwMDAiIHN0cm9rZT0ibm9uZSI+CjxwYXRoIGQ9Ik02NzcgOTU4MiBjLTEyIC0yIC0xNyAtMTQgLTE3IC0zOCAtMSAtMTkgLTEgLTIwMjEgLTEgLTQ0NDkgMCAtMjQyOAowIC00NDE5IDEgLTQ0MjUgMCAtMTIgODkyMCAtMTQgODkyMSAtMiAwIDkgMCA4ODU5IDAgODg5MiBsLTEgMjUgLTQ0NDMgMApjLTI0NDQgMCAtNDQ1MSAtMSAtNDQ2MCAtM3ogbTI5MDggLTE1MzQgYzAgLTkxNyAtMyAtMTM4OSAtMTAgLTEzOTEgLTIwIC02Ci0yNzUwIC0xIC0yNzYwIDUgLTcgNCAtMTAgNDc1IC05IDEzOTAgMSA3NjAgMiAxMzgzIDMgMTM4MyAwIDAgNjI1IDAgMTM4OSAwCmwxMzg3IDAgMCAtMTM4N3ogbTI5MjMgLTMgbC0zIC0xMzkwIC0xMzg1IDAgLTEzODUgMSAtMyAxMzg5IC0yIDEzOTAgMTM5MCAwCjEzOTAgMSAtMiAtMTM5MXogbTI5MjcgMyBjMCAtNzY0IC0yIC0xMzg4IC01IC0xMzg5IC0yNSAtNSAtMjc0MCAtMyAtMjc1NSAyCi0yMCA2IC0yMCAxNiAtMjAgMTM5MCBsMCAxMzg1IDEzOTAgLTEgMTM5MCAwIDAgLTEzODd6IG0tNTg1MyAtMTU1MCBjMiAtNyA0Ci02MzIgMyAtMTM4OCBsMCAtMTM3NSAtMTM5MCAwIC0xMzkwIDAgMCAxMzg4IDAgMTM4NyAxMzg2IDAgYzExMDEgMCAxMzg4IC0zCjEzOTEgLTEyeiBtMjkyOCAtMTM3OCBsMCAtMTM5MCAtMTM5MCAwIC0xMzkwIDAgMCAxMzkwIDAgMTM5MCAxMzkwIDAgMTM5MCAwCjAgLTEzOTB6IG0yOTI1IDMgbDAgLTEzODggLTEzODcgLTMgYy03NjQgLTEgLTEzODkgMSAtMTM4OSA1IC00IDIzIC00IDI3NTAKLTEgMjc2MSAzIDkgMjkwIDEyIDEzOTEgMTIgbDEzODYgMCAwIC0xMzg3eiBtLTU4NTQgLTE1NDMgYzMgLTEzIDQgLTI3MzggMQotMjc1NSAtMyAtMjAgLTEwIC0yMCAtMTM4OCAtMjAgLTkxNSAwIC0xMzg0IDMgLTEzODUgMTAgLTMgMjMgLTUgMjc1MSAtMgoyNzYwIDMgMTAgMjc3MiAxNSAyNzc0IDV6IG0yOTI3IC0xMzg1IGwyIC0xMzkwIC0xMzgyIDEgYy03NjEgMSAtMTM4NiAyCi0xMzkwIDMgLTggMSAtMTEgMjc2NCAtNCAyNzcyIDIgMiA2MjcgNCAxMzg4IDQgbDEzODMgMCAzIC0xMzkweiBtMjkyMyAxMzg1CmMxIC0zIDIgLTYyNyAzIC0xMzg3IDEgLTkxOCAtMiAtMTM4MyAtOSAtMTM4NCAtMzIgLTQgLTI3NjUgLTMgLTI3NjYgMSAtNSAyMgotMyAyNzY2IDIgMjc3MCA5IDcgMjc2OSA3IDI3NzAgMHoiLz4KPHBhdGggZD0iTTUwMDAgOTIwOCBjLTE1NyAtMTggLTI0NSAtNDIgLTM3OCAtMTA0IC0xNTUgLTcxIC0zMjcgLTIwNyAtNDIwCi0zMzMgLTE1IC0yMCAtMjkgLTM4IC0zMiAtNDEgLTI4IC0yNyAtMTE5IC0xOTggLTE1MCAtMjgzIC0xOSAtNTQgLTQ0IC0xNDUKLTU1IC0yMDcgLTE0IC04MCAtMTQgLTI5NyAxIC0zODQgOTQgLTU2NSA1ODUgLTk4MCAxMTU5IC05NzkgNjQgMCAxNzggMTAgMjE1CjE5IDI2MCA2MCA0NDkgMTYyIDYxNyAzMzUgMTY3IDE3MSAyNjQgMzYyIDMxOSA2MjQgMTMgNjcgMTMgMzAxIC0xIDM4MCAtODIKNDU1IC0zOTcgODA5IC04MzMgOTM2IC0xMzUgMzkgLTMwNiA1MyAtNDQyIDM3eiIvPgo8cGF0aCBkPSJNNzg4NSA2Mjc5IGMtNTI5IC04MCAtOTI2IC00ODEgLTk5OCAtMTAwOSAtNzEgLTUxOSAyMjQgLTEwMjggNzExCi0xMjMxIDEzNyAtNTcgMjUxIC04MCA0MTIgLTg0IDE1OSAtNSAyNDMgNiAzODcgNTAgNzYgMjMgMjg4IDEyNCAzMDMgMTQzIDMgNAozMCAyNiA2MCA0OCAxNDIgMTA3IDI2MSAyNTMgMzM4IDQxNSA0MyA5MCA4NiAyMTkgOTYgMjg0IDMgMjIgOSA1OCAxMiA3OSAxMAo2MSAxMSAxOTUgMyAyNjcgLTEyIDEwNCAtMTkgMTM5IC01NSAyNDcgLTEzNCA0MDkgLTQ3NyA3MDUgLTkxMCA3ODUgLTg1IDE2Ci0yNzQgMTkgLTM1OSA2eiIvPgo8cGF0aCBkPSJNMjEyNyAzMzY0IGMtMSAtMSAtMzEgLTUgLTY3IC04IC0xNDkgLTE1IC0zMzggLTc5IC00NzQgLTE2MiAtMjg0Ci0xNzMgLTQ5MSAtNDc4IC01NDYgLTgwNSAtMTQgLTgwIC0xNyAtMjg5IC02IC0zNTQgODQgLTUwMCA0MjUgLTg3MSA5MDEgLTk4MAozMzIgLTc3IDY5OCAyIDk3MCAyMDkgNjkgNTMgMTkxIDE4MCAyNDUgMjU3IDI0MSAzNDIgMjgxIDc4NSAxMDYgMTE2NCAtMTY4CjM2MyAtNTMxIDYyOCAtOTIxIDY3MSAtNTYgNiAtMjA0IDEyIC0yMDggOHoiLz4KPHBhdGggZD0iTTUwNDAgMzM2MiBjLTI1NiAtMjAgLTUyMyAtMTMyIC02OTQgLTI5MSAtODUgLTc5IC0xMDcgLTEwNCAtMTY5Ci0xODYgLTEwMiAtMTM2IC0xOTEgLTM1MCAtMjE0IC01MTIgLTE0IC0xMDMgLTkgLTMyMSAxMSAtNDEzIDEwNSAtNTAxIDUzMQotODg1IDEwMjYgLTkyOCAxNTkgLTEzIDI5MSAtMyA0MjEgMzIgMzI4IDg5IDYwMSAzMTQgNzQ5IDYyMCA0OCA5OCA5MCAyMjQKMTA1IDMxNiAxMCA1OCAxMiAzMDIgMyAzNjAgLTE1IDk3IC0yMCAxMTggLTQ5IDIwNCAtMTIxIDM2MyAtNDE4IDY1MCAtNzg0Cjc1NCAtMTI3IDM2IC0yODUgNTQgLTQwNSA0NHoiLz4KPHBhdGggZD0iTTc5NjcgMzM2NCBjLTEgLTEgLTI5IC01IC02MiAtOCAtNTc3IC02NCAtMTAyOSAtNTc0IC0xMDI4IC0xMTYxIDAKLTIyOSA2MyAtNDQzIDE4OCAtNjM3IDI4OCAtNDQ2IDg0OCAtNjQxIDEzNTQgLTQ3MSAyOTAgOTcgNTQwIDMxNyA2NzcgNTk1CjEzMyAyNjggMTU1IDU5MSA2MCA4ODEgLTEzMyA0MDQgLTQ4OSA3MTAgLTkwOSA3ODMgLTcwIDEyIC0yNzIgMjUgLTI4MCAxOHoiLz4KPC9nPgo8L3N2Zz4K"}]
                      [:meta {:name "theme-color" :content "#ffffff"}]

                      ;; main.css
                      [:style {:media "all" :type "text/css"}
                       (s/replace (slurp-path temp css) "\n" "")])

                     ;; inline css
                     (inline-css opts)

                     ;; use full page width
                     (when (:full opts)
                       (inline-css {:inline "html { max-width: 100%; }"}))

                     ;; ewan.css & highlight.pack.js
                     (when-not (= (:style opts) "-")
                       (hi/html
                        [:style {:media "all" :type "text/css"}
                         (s/replace (slurp-path temp style) "\n" "")]
                        ;; [:script {:type "javascript"}
                        ;;  (slurp-path temp highlight)]
                        [:script "hljs.initHighlightingOnLoad();"])))]
                (delete-directory temp)
                text))
            (hi/html
             ;; favicon
             [:link {:rel "apple-touch-icon"
                     :sizes "180x180"
                     :href "static/ico/apple-touch-icon.png"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-16x16.png"
                     :sizes "16x16"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-32x32.png"
                     :sizes "32x32"}]
             [:link {:rel "manifest"
                     :href "static/ico/manifest.json"}]
             [:link {:rel "mask-icon"
                     :color "#5bbad5"
                     :href "static/ico/safari-pinned-tab.svg"}]
             [:meta {:name "theme-color" :content "#ffffff"}]

             ;; main.css
             [:link {:rel "stylesheet" :href css :media "all"}]

             ;; inline css
             (inline-css opts)

             ;; use full page width
             (when (:full opts)
               (inline-css {:inline "html { max-width: 100%; }"}))

             ;; (when (:full opts)
             ;;   (hi/html
             ;;    [:style {:media "all" :type "text/css"}
             ;;     "html { max-width: 100%; }"]))

             ;; ewan.css & highlight.pack.js
             (when-not (= (:style opts) "-")
               (hi/html
                [:link {:rel "stylesheet" :href style :media "all"}]
                [:script {:src "static/js/highlight.pack.js"}]
                [:script "hljs.initHighlightingOnLoad();"])))))]
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
  "Returns the HTML name of PATH."
  [path]
  (suffix-name path ".html"))

