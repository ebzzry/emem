(ns emem.html
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
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
                      [:link {:rel "apple-touch-icon"
                              :sizes "57x57"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAADkAAAA5CAAAAACpc9xZAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyUeikXcAAAA1ElEQVRIx+2Wyw3CQAxEfU0HSNtG+shlK0xX28fegRUBZGNG/rCcMrdR9OLYHkuha1R0ktPJjWkFbpMke9/hOngGyFYLldr85L7Q0LJ7yfYA72hzkpWeqk6yvMjiIzu91f9UM9FnfLbxfSYyNGTKLb+HC3CrpSb8Aiv52bWRVCZtI7Xt2kgtUTZSS7GJVC9ncs1En/HZxveZyNDQl9ymb6WbHSN5L8gJks8POUHynSEnSZ4T5CTJs4mcIPk9IPe7mok+47ON7zORoWhuw/9gfp3kPPIG4pjjOLhmO34AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTUtMDgtMjdUMDA6MzE6MzcrMDI6MDCGlSHhAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE1LTA4LTI3VDAwOjMxOjM3KzAyOjAw98iZXQAAAABJRU5ErkJggg=="}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "60x60"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAAAAAAfl4auAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyfwhCTwAAABtElEQVRIx+2Wv6uCUBTHLYcIhPc3SLNL1NBgDQ7SKkRLa3MgSFOjrdIa/iWOrdHS6tbi6OAgFeYrfN7jj2Ov6+X1lr5D8OXw4V483849XMwg7gP/A7yZUmhTgKdHL6uB98Qep0XYy11o9PjZr8bj1Z5YIu93+Lxscnc1l+ca8IL70YIe3jVSuLGjhucc0Zwa7gLcpYY7AHeo4QnAE2p4C/CWGo6GKTuM6Pt86iVs71QjJPF13W+1+utrXAe+63LJ2Up4MMrqa/TEDl47GbcvXjtro+2k053vasGnpHWNxZkevvbSti/p4TXJW3NPDfchrSta+NICePxWmOnabB+MqVVMIWGLZ0Z/8peMwtxRhWEQRhUnP2BbFnhJD2J0DAW6xAuyXQH7atIL8YANwIOYWNVH4VnaSTEoj95ATO0Mgx0IoF4e+jpYB4ENKEvl50YCayCwAmU+LD50IQ9WQWANykJUfGIjAayGwCaU5fLjLoM1Edhtk7JdXitsUmy7WKustKxiC42aWgvt881Kzp752CrlJzFoW7eKeLqmphgOCXNhiXMMRTNdNJ4s6yPT4lpLH/id8Dc9MeCjYhQRUwAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNS0wOC0yN1QwMDozMTozOSswMjowMNaqWrwAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTUtMDgtMjdUMDA6MzE6MzkrMDI6MDCn9+IAAAAAAElFTkSuQmCC"}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "72x72"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAAAAABwhuybAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyaHgxRmAAACJ0lEQVRYw+2XPYvCQBCGE8VCEEEwdTqF6xR7QZGAvyCFhZ2lhYVWgq1gnx9gH7CwsEgplmKXQgL2NoL4Beb8yCWTvR3xMltc4VuFl+FJZndmMyu5giR9QB+QUFBcZaSkWCelsE6cA1LZd5gt1mmZrKO+D7pO+/V6f3qlgjZV6aHqhgZapSVP6RUFdC5IvgpnAmgoAQ0JoBoE1QigDARlooP2MgTJ++hflIegPCG1BgQ1CKB5PODE55SC7AagLqmyT80fTvNEbNpJ7o7JTe7PJJDrbmez7fPpPZBiMupprKP1WEfhgFItRtoX63xprJP6S2qB/NT2kdaIBc0beTlTG56JoFPXK9LCigbyC0tKbyigCWi+6pUAysE2nkYHbSFH6kcHzUKg+j8ACUtN2GKL235hBSmuRYQ17UMvjxFhB5uwo9b7yuPCdMKp/T78HXNxxFN7eHYlcduQ7ODior+jyyB7MxIV+xXISHo1UlpjP8h1yTOSBg6ygjGmdOH/si8l35AtDLRTQf0P+EPEAISoOwQ0hg2Z5Y81WeiMEVA7dEY4vEHLCYW0EVA5FGXyRj8zFFJGQHooasEbRhehEB0BjWBQ4sgbj48J6IwQ0DIGgir8gb0CjNgSq6NOEJS0+VcIOxk4HbQgD0V/YQ3sUmP4G1A84C1y6DyzUy0XvWZZz7KNdQ6vmtZdjvRye7xzXfzitxu3y/po+br7oaJeRYVdjon6gD4gEfoGr2e7RNm9gY8AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTUtMDgtMjdUMDA6MzE6MzgrMDI6MDBw3VEIAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE1LTA4LTI3VDAwOjMxOjM4KzAyOjAwAYDptAAAAABJRU5ErkJggg=="}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "76x76"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAEwAAABMCAAAAADi/A73AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyhgOzlhAAACSElEQVRYw+2YvYrCQBDHg6BVGruUdkJABFvtbOwCFoLY+Qw+hXCmEcTC3sYUklZsxMZGAnfgAwQtRARBFDS58zM7ezu582Y57sBpXIa/P7KbmdmZKL5EU56wJ+wfw0rpn1lJBEs7c856Bd4zL/R4j5MWwub8Yw+Ny+/CfrEXl6Ux5EXzh2DTjHKyzFQCrB5VLhatk2F9JbA+EbbUGJi2pMFMhTWTBqsAWIUGSwJYkgZLAViKBqsCWJUGawNYmwbbsoeW3BKDdhy5syJjcjp141dWvOvTE90tqh8otej6EmC+783smXdZfhvWG3JmZnnPMGvynp4YVjA4y2q8x9CyvKfw4DYZo5/ZJ9i2XU0lK+ZSBmx8jWStT4d1gziuU2FuPEiw6JQIK7Kpn6HBPBUUkgUJNgMsxSbBbAh7+TtPJvXMpL5NuXEmNQPk5qbcqgEMgUmttFLvgOs2V4PRBm5TcDttRoNV6DZPMK+ZON3duhXABPempZ+iI9H0QmFu/hZB5TV6o6/LN1HeDYEdc0Fwl9FeoxyIckcc1mDTzkK6IIsVNVDYARQEHenPdNajHjCYA/4Z2Qg7x00EuBwM1oF1byTsaUdQ1MFgLagbCLvtARS1MNgE6lbCOWAFRRMMto+xsgQyoSRYT2yPhkaN1TWR2anJimp4nO2Y1573kKnOywcefReSTm93Ws5F5033nif6a2ii72rnc1MbRx+fhI+Nc3THajv/i0q7n7Q6zuG8xGf0g9NpTfbnJQKT+fVA6ncNuj1hT9hvwN4Bby2dKhgDfw8AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTUtMDgtMjdUMDA6MzE6NDArMDI6MDBJ9xZ2AAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE1LTA4LTI3VDAwOjMxOjQwKzAyOjAwOKquygAAAABJRU5ErkJggg=="}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "114x114"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAHIAAAByCAAAAACqttqhAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyUeikXcAAABlklEQVRo3u2awW2FMBBEV0i/h3+gApoxxdAB0q/OjdABN3Igh+RDxqvN2kPIznWEnmTZs7MSsjWXBDKQgQxkIG+MfKUKekFkev785dPqJYx8s2t4ZuSyNEXmeexF+jTnRsh16uRT3bS2QOZBvmjI9ZH5Id/0yLWR6yBvGtbKyEkOmuoic3dEdrkqcpYTzVWR4xkyVUX2Z8i+JnKRUy33QhIOlnF9CI+EEAWEwGPEOmF4MUY0o4hshLq1q3Gp/J13gQUhdhK1h6+WO7L8gJyRmpjwRarC0BWpi3xPpHKweSKV49sRqS0pjkhtFXNEagunI1Jbq/2Q6uXhTyMJB8u4PoRHQogCQuAxYp0wvBgjmlFENkLd2tW4VJa9CywI/3AnQdfA6gEkuuxWDyLRk7Z6GImCy+phJIpnq4eRaAhZvQISjVqrh5GoUFi9AhLVJqtXQKazT8dycUReAYkqsNXDSFT0rd71kISDZVwfwiMhRAEh8BixThhejBHNKCIboW7talwqj7rPTkL4XayFAhnIQAYykLdCfgA04pB+ALk/xAAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNS0wOC0yN1QwMDozMTozNyswMjowMIaVIeEAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTUtMDgtMjdUMDA6MzE6MzcrMDI6MDD3yJldAAAAAElFTkSuQmCC"}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "120x120"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAHgAAAB4CAAAAAAcD2kOAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyhgOzlhAAADlElEQVRo3u3aP2jqQBgA8PikpJViF6ngKjh0LBQ7lNpF3KWjdKtL4yRYcCl0L1S6uLnVN2WT0s2huDgUUrqYUYpIhy4WgmLu9Y+2Mffl/Hyvd9c+cqOfnz808b4vd6cQSUPxYR/2YR/2YR/+9rBZ4zVMNlxTeI3aPNjoeY+wxghqYUbQmA/3GBdm7ZgRPF5jBHtfB/cap7nThuPdQuDhSWxy6WInQ4Hw3abjrtm8EwafqTP3q3omCNapv4ouBH5cp+D1RxHwPjA77AuAr8B56Yo/XAThIn94F4R3ucPjVRBeHfOG7z2Kzz1v+NYDvuUND5dBd3nI/eZKgnCS/119BMJH/OFLEL7kD493AHdnLGCuNkOUG3ptIfmXxQsKviBCYPvQ5R7aYmBCGjEHG2u8vyik2Xs6CE7Y4METEQgT8nxzntvKnd88f7wiqq+mxo+Fw2veQ1EZQVVhBMPzYe3Ye6jbjOC2yghq3/in/iJ4eHs/Fg6PL4+SL/3C6m7xSihsOurX/qMw2L6YqV7ruijYXUGUMzFwg6qZ6p0I+ClGwcrmUAB8ADVkJ/zh5yAEx/jDN2AL+vKZvOFzGG5wh3MwfMod3oLh3P/7jaVdY2l3tbT/sbSZS95cLa06yavH8joQaT0Xkddlvg0pfTUwELC0ZydpT4s//aHtX+CuXk6ninXThmDmGoht1oupdFnv/g3cTEwngehvCmav+tSj09REc1F4UAg45r1sfxZmrnP1s45goDBYCDbisxN95NoBs1f2riOzwbixAGxtuEtMpP8JM9cy+xF3cMPCwyW6mmc/YPbqbZZOLaHhFtQ51QlmvboOBH+1sDC47RC1ESv0dhQKJpGwtQR+tonYkzDB4JKFg9tg9utvPXcXpg6ntnFwFc4uIvad4B1PpYqD83B2CrHTloKDeRyswdlpxN5iGg5qONjj2E0ZsZtahlNrONiAs3XE/rEOpxo4eLQCZncRO+ZdMLgyQk4gGSg7QTBnBBJQMIOduTr0bKwEmgRzKqIZoIOhDrpIVOjsApnC7HMgBTq1QtCwvedOjg8+YPbJl0HcHdyz8TB5cMkbb9UcddbHcNXyvQeyAEzsiuM6B0sWccLs001WyVFVQ5X3VnGBZq+TmfyrlpKtaRR5nquVnNS3lUxn8tJC7e3IqGn5atv6jKJPsFntal6rGaPPd3/jhl7aKUVeYw4s7SSqsOHDPuzDPuzDPuw5/gCmJs7zLAHhNAAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNS0wOC0yN1QwMDozMTo0MCswMjowMEn3FnYAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTUtMDgtMjdUMDA6MzE6NDArMDI6MDA4qq7KAAAAAElFTkSuQmCC"}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "144x144"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAJAAAACQCAAAAADCLb1kAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHyfwhCTwAAAEUklEQVR42u3bP0iyQRgA8FMIhYIIKXBvsD+gEA2J1BIS1OIkCE4hbSLoLlhrSy0OToG1NUUptIhTwzu41GbY5FQkhIR/7uOj7y19vefeO+Pzub7vbnx7evyV99497713hCrWCDZAgzRIgzQIG6BBGqRBGjR6qT3BJgJqkwm2thBodsuuecm6Xcg68dqmmRUEbdl+0QfkwS7kgRzYptn6b0BPF8eZzPHFkxqg16OA2UUDR6/ooN7p/OBdM3/awwW1dq038m4LE9RcHR1aVpt4oPcN1mC38Y4GSrCH3wQW6A6aEO6QQJsQaBMHZMBzpoECysKgLApoDQatoYBmYNAMBqjFK7xaCKA6D1RHAL3wQC8YfcgNe9wonXoJBi2hgNIwKI0CqsCgCgqotwJ5Vno4k+sVBLqiSPXQDtuzQ7FAz4ssz+IzGojeL4x6Fu4pHog2AlZPoEExQfQt7RrkuNJvFBdE6eP+nMmZ23/8vIz5bN+5zSb29hLZ287ARb36MUmQ98CuLZOoXUiULNum8QqCJtjEQOsPdi1Kru1CrknUNs36T+1Dfwn0Um8pA+pV0ku/S/CZtayhAuhqsJTbvMMGPVvLpsQ7Kuh+tGjaaCKCGoySiay20EBvAcJquz0sEPTwdooEenQBoPlXHNA+OIsdoYA6cyAogAK65Uz0TxggzrooucAAJTigYwzQHgeU0SAVvzLlOrVyt71yA6NyU4d6k6ty5Yd6BZpyJax6Rb56j0FUuQdFqt6j9EdTabEBaKIg5RasJth+6KKncn1IbVCzkImFo6mTOgwSebVQP0lFw7FMoflN0GXIafY/f/7r8yRfvnTyfjPEGbr8BsgIDt0SvptRkMjrqRvfUJrg52AtCzqzlnuOXN8CEniB1885LCGus/FAh4xxI94fAgm84uzHGWkOxwEVmSNZbhAk8hI4x0xTlAfV2HtNHDcDIIHX5DcOZoi7Jg3aZn8Y8XWoxEaCjg8I2ZYFlaAPI3kqsdUiD6YpSYIiYCY/ldiM4gdDInKg9jT8aXXx7TqcbWHTbSlQGc5ETsQ3NJ1w0pSlQHlOppT4lq8UJ01eCsRb2ImKb4qLckKyUqAkJ1NYfNtgmBOSlALlOJli4hsrY5yQnBSowMmUEd96muGkKUiBqvxMoptzeX9XVQrU9YCJnE3x7ctNJxji6cqN1HEwU4hKbPAOgSFxKgcyHFCmSxMksgX+EgpxGJIg8AYJ0k+QyCGBIBASo7KgBrsXuYwvkMgxCoO96ulpSINoZYqV6aMcljhocsYKmapQeRA9Z4j+FMMyR3EYpfnUOR0HRCvWb81dpMMgocNKRess7KnQ8UC0ERu617Zr1AoSOs5VGyqHHbGG+YMxnlyNuPlfmo6Uvi7LHngrRcyCzxMfWNMb69m+Wy3kktl8eehX5Y8Etsv5bDJXqHYHL/4bqx8qgJQ7eDvBJgJS7vA2ctMgDdIgDcJuGqRBGqRB2O0X6w29xRjQBb4AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTUtMDgtMjdUMDA6MzE6MzkrMDI6MDDWqlq8AAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE1LTA4LTI3VDAwOjMxOjM5KzAyOjAwp/fiAAAAAABJRU5ErkJggg=="}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "152x152"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAJgAAACYCAAAAAA9qX/9AAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHykXPAn3AAAEqUlEQVR42u3cPUgjQRQA4EEkhKAocsdBJNeoF+z8IWARCw8lV2hnYWEhErDRRrC3OFAsbIwnaB0URNQmIlilEhTbpBJR0EoLRY1GM3fn+ZNkZ97uW4+8p84rN4+XD52dnZmdiZBMQ1ADDMzADMzADMzADMzADIxnvClYfKjUEXcGGxKljiGnsJ8btrEgWu2TNlrFgn3ST+ewDfs2kBIRBy0lIlL2SRsfEpZJjHS3fG3pHklkOMEOo5XPDbkyesgFdj3mLbjHvGPXLGDHIcvtHzpmAEvXKjqm2jQ57KxB2WU2nBHDcp2azrwzRwtb0j5mlkhht/VaWP0tJSwOPJnjlLBeANZLCMtUALCKDB1sFxxl7dLB1kHYOh1sDoTN0cFmQNgMHWwFhK3QwbZB2DYd7NwDuDznhB1sBID9kIQw6Laco4RdfNG6vlxQwuSsFjYrSWHZJo2rKUsLkwefla7PB5IYJpM+hcuXlOQwuRewuAJ7kgFMnvQUuXpOnj6iXiJIhvNY4eTLB9QwKfenu4JVoirYNb2ff5ke9hD3litMYNb437CFlG0kRNg+KRUWCfukBeewUodjWGvENsLik31S5JMI2ye1voM29vFg59srM7/WdwsWkOlhF3ORxzlCRW/8lg0sO1sw2q1fyvGAHVhGlJ1nHGBJxXiyIU0P21ONJkXtMTXsJCCUEbomhvUITYzRwpLah6T3kBQW1sJElBK2D4wrKjOEsGloxJMghHVBsBFCWBCCdRPCqiBYCx3sHnKJr+YvZoWxbWNs70q2/Rjbnp/ts5Lv6ILteIzvCJbtmJ/vLInvvJLvTFyyXbt4CJ6rPbp4DzC2a7Cljre/zs+2jVlhueILrt8lWSq5hWW3hkMBT7m/eWDtUg9z9Pbtcm2g2V/uCYSGt7KvhV1N1Ly0Ud/oqRrm6H3l6WjeEKNm4upVsGV/4e1THVPAnL3hjVUXJvmXXwEbt97ZgzfFMEfvxG8GraXGXcOiqj7n+00hzNEugpvvqlJRl7ApdW84WABztu9iUF1qyhVss0zTT8fyYM52qsQ0SWWbLmB3jZpqovpU4vb2nFbrkhrv8LB57VeKUYnbDTWqLzWPhwFrNL5L1P6xS58+KYiGpYCvFGuoHXerUKk0FjYJVRtA7VEcgEpNYmF9ULVm1K7OZiipDwtrh6r5Uftg/VBSOxZWB1UrzyF2DufKoaQ6LAxcOPWg9lpDDfHvbYmDdUDVAqjd6QEoqQML64eqhVD7+UNQUj8WFoOqDaNOQAxDpWJY2BFUbQt1ZmQLKnWEfiQB/4CaLOqUTbZGn/SnUWBhwINkQuLOJU3oS63iYbJNV8x/9QxzdpLrStvFtkkXsB2vptrDLAJ19m1Zk+TdcQOTi+pq/+YQuNOC4+pSi9IVTD3of5xBIM9XKuc1/4b8bqZvi5b/ZtljMfSJ1CnLDMK7KF3D5E7RHdC4+fQJ+gzvZtEcom3n6RN3axereX+P4Pzd83X8qee7+byBQWj1Jcntas9RrL8jWNfeN5nOv+rqnHhqsq+97ltHf+wo/+p7WINlC2P76w2lDocwtr8QwiIMzMAMjFsYmIEZGLcwMAP7sLDf+qfXlB6Fyu0AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTUtMDgtMjdUMDA6MzE6NDErMDI6MDDvgB3CAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE1LTA4LTI3VDAwOjMxOjQxKzAyOjAwnt2lfgAAAABJRU5ErkJggg=="}]
                      [:link {:rel "apple-touch-icon"
                              :sizes "180x180"
                              :href "data:image/x-icon;base64,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"}]
                      [:link {:rel "icon"
                              :type "image/png"
                              :sizes "16x16"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAABlQAAAZUBDE2OiAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAEgSURBVDiNpZO9TkJBEIW/MRTaUNlpL3Y0JnRQWFuR2GhiZ4UVT2JHh/ENbgkWliQ0dAo1vgAU2h0LzoaF8BPCJJO7c/bMyczemZDEMVYCiIgn4DvDq8AIuHI8zrBkFUldXEFNEsmBBtACZG8BjTVOTRInO6q723JesV0CHeDP3tlGKvlbjYjTDE/93juemZPnVoBBEhhJGqSbiEDSZ8528jnwDBTAcF8L61YG3oFb4BX/oUMENtohAjPgEfgAXljMxsojngEXwJTlg5Wz5PSwb8CP40EairoVBfRYDFIT+LU3jfXMGQN1SSSBNsupE/AA9LO4byzntPNJHAFznycuscj6L4xNHM+dQ0hKyzR1X0Pgms3L9AXc+O5SUjeOXed/zrJ7iglNC+4AAAAASUVORK5CYII="}]
                      [:link {:rel "icon"
                              :type "image/png"
                              :sizes "32x32"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAADKgAAAyoBEJdYGAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAJVSURBVFiFxZcxaxRRFIW/s6SwiaTOD0itEAhINIJYLYosJp1YhlhlMcj+BBHDpk1pGy1E3EqEmISgINHaH2At20mSvRZzZ/Pm7Uw2O+w4F4Z53HvuvWfnvXfeW5kZdVqj1u7ATDqQ9Ab4MQa/DBxNAXPTzJ4CYGb4NGym46IH6Pi7AWwAH/3ZABohZkydYa+ZmNo4kyTgA9AM3E2gKenBpPXKrIG1qHlIYu1/ELhTMjY1Av2SsakReA8McvwDj1VLwMy+AVvAWeA+A7Y8NpFNvAucRFdSD7jrrn0z+1WmVkhgWdK1MfiVZBeOWCvwF2FCWwR2YgJHZrZzWZYkzOzlVTGSloBHwHXgANgzM5O0meJLTcFVTFIbeM3FOnsGPJH0MMRVchhJWgBe5dRvAuuVEyBZnEVfN6OitR/HVRHYJ6sTofUqJ+Ca8IJRxewBu6Gjsl3gYnVM/jYc4pTeCSW9A76PqbsCfJkCZtHMHkOBEEm6AdwmOd0+mdlv94cis0COFEeYeeA+yRc4NLOf7h8K0cg1CdgmmTvzpw+0oitZGzgNMKdAO8K0PDfFDIDt+EqWIQCsBgkWkZgHOsAScJ6DOfdYx7H9glqrIYF4FxRdqWb9U0KyqPJ2T8NjOHa2oFamR1xoriAJknkM32UxmR4xgc8FSQYc+vjgkuJp7NBz8izTIybQBU5ykrrpCgb2iNTMrecxHNvNwZzE/owQmdlfSbdIVvk94A+JeLwNMOZH6joXB0sP2A1FxsyeS/pKMudzJL+86z2GPUMhquWv2ZBAXVb7cfwPaCtOo1/GtoIAAAAASUVORK5CYII="}]
                      [:link {:rel "icon"
                              :type "image/png"
                              :sizes "96x96"
                              :href "data:image/x-icon;base64,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"}]
                      [:link {:rel "icon"
                              :type "image/png"
                              :sizes "194x194"
                              :href "data:image/x-icon;base64,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"}]
                      [:link {:rel "icon"
                              :type "image/png"
                              :sizes "192x192"
                              :href "data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAAAAAB3tzPbAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QA/4ePzL8AAAAHdElNRQffCBsAHzR0OmUuAAAGBElEQVR42u3dP0gjTRQA8JGIIZw2RkRBsRBUEMMJFnqIoFeocKAQxEYRGxsPrUSPw0ZT2YigGBA8CwvTCDYqSFAQU0hsFTFic0gOLDQgCTFx7r5P1mz+7PzJrswbMq90HzPvR7Kb2dmdEWHJA4kuQAFEF6AAogtQANEFKIDoAhRAdAEKILoABRBdgAKILkABRBegAKILUADRBSiA6AI+EPCzF0z8zAvQi8BEb56Az9PsgdAge/IgQhxNf84bMM3zXUQH7MkHiOfcm1aAAgU8bI12N5aVNXaPbj3IB3jd7rKlriK2ru1XuQBHrZlXwtYjiQDRkVwX85GoLIDfbbl/jtp+ywEIVRn9oFaFZABEmo2HBM0R+IDXAdKgZuAVPGCDPCzbgA54riYDqp+BAxZpI+NF2ICXchqg/AU0wE+rHyE/aMAUHTAFGlBPB9RDBiRsdIAtARhwT68foXvAgCALIAgYcMwCOAYMuGIBXAEGPLEAngAD8Cd6/Z8wZEAPHdADGrBCB6yABtzRAXegAfgLrf4vGDbghAY4AQ7A38j1f8PQAZcOUv2OS/AAvEMC7GD4APzDuP4fWAZA8rtR/d+TUgAw3ijJVX7JxnsCdAAOuLLrdwVSx8EDcPJXbXr5tVtJ3WH4AIyju2NOrXrn2G407aAMgH+RuNhbn59f37tIZB6RBGAcClAoADBRsIDBA/ZAyMOe7Pn3hWOPwcI9BxSAD/B0dRy8T2T/XQrA3UrP26SYrX7K/yId4CR9MqN88VkqwGX2RED1hu71HOiAnZzTAAMRSQBJo1voZu3NEOAAwxtoVKW9nQMaQHq1oi0KHxAoIQDQCHhA0oWIcQQd8IsyCm19hQ2I1lIAaBs2YJdWP+qCDRijAmwPkAEJJxWAtiADLuj1o1HIgD0GQDdkwDoDoBEyYJ4BUKYAHwiQ/isk/Uks/WVU+h8y6YcS8g/mpB9OS39DI/8tpfQ39fJPq8g/sSX/1KL0k7tY/ul1LP0Djv9C8kdMbyH1Qz5SFDBA+lcNwETBAqY5vqggzwEFgAe4Odz0zC77TiPZDZl97TJy6lue9Wwe3nwc4GymQTur7P3eMBnA9+Jr2Ntv17IbZs4+BBDoTL8yOOYejQF8rx4/zmWMqDsD+sOWAMLu7Iub02sE4Hv525tjhs6t+4CtAARrcl6fx2M5AVyv38fGczZdE7QS4DNaGNP+JxvAtwDiT7tBssNnHcBfbPgb2a59BnkuQYm1GyYX+60C3JJmkcczAXyLgMYJyc5bawDxFlJJyJsO4FuG5SU23RK3BLBK7AQ5H9MAXAvhHilPCFatAEQqyZ2gOT3ghJKcvhRxjpJcGbEAsEAryRHWAbgWg4YdtOwFCwBNtE7ezoJ8luN6qclN5gHX9JL6UwC+BdH99Oxr04Aleif2yDuAa0l6xE7PXjINcNM7QafvAK5NAU4ZmnabBnQw9OLTAHzbMvgYkjtMA+oYelnWAHwbYywzJNeZBjB8T9GsBuDbmmSWIdluGlDB0ItHA/BtDuNhSK4wDXAx9LKpAfi259lkSHaZBvQx9HKoAfg2SDpkaLrPNGCSoZcbDcC3RdUNQ9OTpgH79E4a8DuAb5OwBnr2vmlArJTayUwKwLdN2ww1uTRmGoCHqL2cpQB8G+WdUZsewuYB50WUTjpxCsC5VWEnJbno3AIAHqb0EtAD+DaLDFCaHsZWAELEFzL+H26lAJzbdZKHiiUhSwB4jdRJTTgdwLdhariGlL2GrQHgCeM+HNoEWp5b1gYJd5UT2CpA/KtRH8Xv02f5bhrsM5w0+xq3DIDjBp+B059qKN9tm/0GUysTWv3WzE6v5TqTW251DeW9cfZtromzkrVUgjXPB0LDmb8HlatxfUP5b10eX82ceioaDumOW/WE5nxIP6poWkh/zGRq8/jIgn7ypnToPO2odc/IYvuTfa4Ke12He+k6qyGT2/dfL7k76uwVrr7J/VjGIfWUUgGEAaT/Ny5gIj+A9P/KSI5QANGhAKJDAUSHAogOBRAdCiA6FEB0KIDoUADRoQCiQwFEhwKIDukBfwF7AJIEb4v6eQAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNS0wOC0yN1QwMDozMTo1MiswMjowMBLCB8EAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTUtMDgtMjdUMDA6MzE6NTIrMDI6MDBjn799AAAAAElFTkSuQmCC"}]

                      [:link {:rel "manifest"
                              :href "data:image/x-icon;base64,ewoJIm5hbWUiOiAiTXkgYXBwIiwKCSJpY29ucyI6IFsKCQl7CgkJCSJzcmMiOiAic3RhdGljXC9pY29cL2FuZHJvaWQtY2hyb21lLTM2eDM2LnBuZyIsCgkJCSJzaXplcyI6ICIzNngzNiIsCgkJCSJ0eXBlIjogImltYWdlXC9wbmciLAoJCQkiZGVuc2l0eSI6ICIwLjc1IgoJCX0sCgkJewoJCQkic3JjIjogInN0YXRpY1wvaWNvXC9hbmRyb2lkLWNocm9tZS00OHg0OC5wbmciLAoJCQkic2l6ZXMiOiAiNDh4NDgiLAoJCQkidHlwZSI6ICJpbWFnZVwvcG5nIiwKCQkJImRlbnNpdHkiOiAiMS4wIgoJCX0sCgkJewoJCQkic3JjIjogInN0YXRpY1wvaWNvXC9hbmRyb2lkLWNocm9tZS03Mng3Mi5wbmciLAoJCQkic2l6ZXMiOiAiNzJ4NzIiLAoJCQkidHlwZSI6ICJpbWFnZVwvcG5nIiwKCQkJImRlbnNpdHkiOiAiMS41IgoJCX0sCgkJewoJCQkic3JjIjogInN0YXRpY1wvaWNvXC9hbmRyb2lkLWNocm9tZS05Nng5Ni5wbmciLAoJCQkic2l6ZXMiOiAiOTZ4OTYiLAoJCQkidHlwZSI6ICJpbWFnZVwvcG5nIiwKCQkJImRlbnNpdHkiOiAiMi4wIgoJCX0sCgkJewoJCQkic3JjIjogInN0YXRpY1wvaWNvXC9hbmRyb2lkLWNocm9tZS0xNDR4MTQ0LnBuZyIsCgkJCSJzaXplcyI6ICIxNDR4MTQ0IiwKCQkJInR5cGUiOiAiaW1hZ2VcL3BuZyIsCgkJCSJkZW5zaXR5IjogIjMuMCIKCQl9LAoJCXsKCQkJInNyYyI6ICJzdGF0aWNcL2ljb1wvYW5kcm9pZC1jaHJvbWUtMTkyeDE5Mi5wbmciLAoJCQkic2l6ZXMiOiAiMTkyeDE5MiIsCgkJCSJ0eXBlIjogImltYWdlXC9wbmciLAoJCQkiZGVuc2l0eSI6ICI0LjAiCgkJfQoJXQp9Cg=="}]
                      [:meta {:name "msapplication-TileColor"
                              :content "#da532c"}]
                      [:meta {:name "msapplication-TileImage"
                              :content "data:image/x-icon;base64,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"}]
                      [:meta {:name "msapplication-config"
                              :content "data:image/x-icon;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjxicm93c2VyY29uZmlnPg0KICA8bXNhcHBsaWNhdGlvbj4NCiAgICA8dGlsZT4NCiAgICAgIDxzcXVhcmU3MHg3MGxvZ28gc3JjPSJzdGF0aWMvaWNvL21zdGlsZS03MHg3MC5wbmciLz4NCiAgICAgIDxzcXVhcmUxNTB4MTUwbG9nbyBzcmM9InN0YXRpYy9pY28vbXN0aWxlLTE1MHgxNTAucG5nIi8+DQogICAgICA8c3F1YXJlMzEweDMxMGxvZ28gc3JjPSJzdGF0aWMvaWNvL21zdGlsZS0zMTB4MzEwLnBuZyIvPg0KICAgICAgPHdpZGUzMTB4MTUwbG9nbyBzcmM9InN0YXRpYy9pY28vbXN0aWxlLTMxMHgxNTAucG5nIi8+DQogICAgICA8VGlsZUNvbG9yPiNkYTUzMmM8L1RpbGVDb2xvcj4NCiAgICA8L3RpbGU+DQogIDwvbXNhcHBsaWNhdGlvbj4NCjwvYnJvd3NlcmNvbmZpZz4NCg=="}]
                      [:meta {:name "theme-color" :content "#ffffff"}]

                      ;; main.css
                      [:style {:media "all" :type "text/css"}
                       (s/replace (slurp-path temp css) "\n" "")])

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
                     :sizes "57x57"
                     :href "static/ico/apple-touch-icon-57x57.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "60x60"
                     :href "static/ico/apple-touch-icon-60x60.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "72x72"
                     :href "static/ico/apple-touch-icon-72x72.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "76x76"
                     :href "static/ico/apple-touch-icon-76x76.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "114x114"
                     :href "static/ico/apple-touch-icon-114x114.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "120x120"
                     :href "static/ico/apple-touch-icon-120x120.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "144x144"
                     :href "static/ico/apple-touch-icon-144x144.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "152x152"
                     :href "static/ico/apple-touch-icon-152x152.png"}]
             [:link {:rel "apple-touch-icon"
                     :sizes "180x180"
                     :href "static/ico/apple-touch-icon-180x180.png"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-16x16.png"
                     :sizes "16x16"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-32x32.png"
                     :sizes "32x32"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-96x96.png"
                     :sizes "96x96"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/favicon-194x194.png"
                     :sizes "194x194"}]
             [:link {:rel "icon"
                     :type "image/png"
                     :href "static/ico/android-chrome-192x192.png"
                     :sizes "192x192"}]

             [:link {:rel "manifest"
                     :href "static/ico/manifest.json"}]
             [:meta {:name "msapplication-TileColor"
                     :content "#da532c"}]
             [:meta {:name "msapplication-TileImage"
                     :content "static/ico/mstile-144x144.png"}]
             [:meta {:name "msapplication-config"
                     :content "static/ico/browserconfig.xml"}]
             [:meta {:name "theme-color" :content "#ffffff"}]

             ;; main.css
             [:link {:rel "stylesheet" :href css :media "all"}]

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

