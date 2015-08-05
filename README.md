emem
====

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a Markdown to HTML converter.


## Installation

### Leiningen

    [emem "0.2.0-SNAPSHOT"]

### Maven

    <dependency>
      <groupId>emem</groupId>
      <artifactId>emem</artifactId>
      <version>0.2.0-SNAPSHOT</version>
    </dependency>

### CLI

Fetch the sources, build the JAR, store it somewhere, then create a
command:

    git clone git@github.com:ebzzry/emem.git
    cd emem
    lein uberjar
    mkdir ~/jars
    cp target/uberjar+uberjar/emem-0.2.0-SNAPSHOT.jar ~/jars/emem.jar
    cat >> ~/.bashrc << END
    emem () { java -jar ~/jars/emem.jar $@; }
    END
    . ~/.bashrc


## Usage

### Source

Add the following expression to `(ns ...)`:

    (:require [emem.core :as emem])

Convert `README.md` to `README.html`:

    (emem/convert ["README.md"] :out "README.html")

Convert multiple sources to `reminders.html`, using a custom title:

    (emem/convert ["buy.md" "projects.md" "fitness.md"] :out "reminders.html" :title "AAAAH!!!")

Convert a Markdown string to HTML:

    (emem/convert "# Blah")

By default, `convert` prints to `*out*`:

    (emem/convert ["notes.md"])

Output to `../todo.html`, by binding `*out*`:

    (let [file "../todo.html"]
      (binding [*out* (io/writer file)]
        (emem/convert ["notes.md"])
        (emem/re-install file)))

To learn more about the available options:

    (doc emem/convert)


### CLI

Convert `README.md` to `README.html`:

    emem -o README.html README.md

Get file contents from stdin, then output to stdout without CSS and JS:

    cat README.md | emem -p

Get Markdown input from stdin, then output to stdout the 1:1 equivalent
of the input:

    echo "# Blah" | emem -w

Create an HTML listing of the current directory:

    ls -R | sed -e '1i```bash' -e '$a```' \
    | emem -T `basename $PWD` -o ls.html

If no inputs are provided, it will accept inputs from stdin. After
<kbd>Ctrl-D</kbd> is pressed, the converted text will be display to
the screen:

    emem -w
    # foo
    **bar**

Run in continuous build mode -- build the HTML file, and if any of the
input files are updated, rebuild the HTML file automatically. It will
remain to monitor for changes, until <kbd>Ctrl-C</kbd> is pressed:

    emem -c -o TODO.html TODO.md

List the available style sheets that can be used with [highlight.js](https://github.com/isagalaev/highlight.js):

    emem -L

To use a style sheet:

    emem -C zenburn -o reminders.html shop.md repairs.md

Change the main style sheet for the page; the path specified will be relative to the HTML file:

    emem -M css/custom.css -o list.html list.md

To learn more about the available options:

    emem -h

## Dependencies

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [cpath-clj](https://github.com/xsc/cpath-clj)
* [highlight.js](https://github.com/isagalaev/highlight.js)


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
