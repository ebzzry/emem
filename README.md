emem
======================================================================

A trivial Markdown to HTML converter that uses

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [highlight.js](https://github.com/isagalaev/highlight.js)
* [fs](https://github.com/raynes/fs/)
* [cpath-clj](https://github.com/xsc/cpath-clj)

## Installation

    git clone git@github.com:ebzzry/emem.git
    cd emem
    lein uberjar
    mkdir ~/jars
    cp target/uberjar+uberjar/emem-0.1.1-SNAPSHOT.jar ~/jars/emem.jar

## Usage

*emem* is typically ran from the jar:

    java -jar ~/jars/emem.jar -o README.html README.md

but you can always run it via lein:

    lein run -- -o README.html README.md

To save typing, use shell functions:

    # zsh/bash
    function emem () {
        java -jar ~/jars/emem.jar $@
    }
    
    # zsh
    function em () {
        emem -o ${1:r}.html $1
    }
    
    # bash
    function em () {
        emem -o ${1%%.*}.html $1
    }

Enabling us to just type:

    emem -o README.html README.md

OR

    em README.md

Examples can be found in the `examples/` directory.


## Options

    -o, --output HTML_FILE    output file
    -t, --title TITLE         document title
    -H, --header HEADER       document header
    -T, --titlehead TEXT      like -t TEXT -H TEXT
    -r                        install the resource files only; do not build the HTML file
    -R                        do not install the resource files; do build the HTML file
    -v                        increase verbosity
    -h, --help                display this help

## Bugs


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
