emem
======================================================================

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a trivial Markdown to HTML converter.


## Installation


### Library

Add the following expression in the `:dependencies` clause of your
`project.clj`:

    [emem "0.1.2-SNAPSHOT"]

### CLI

Fetch the sources, build the JAR, store it somewhere, then create a
command:

    git clone git@github.com:ebzzry/emem.git
    cd emem
    lein uberjar
    mkdir ~/jars
    cp target/uberjar+uberjar/emem-0.1.2-SNAPSHOT.jar ~/jars/emem.jar
    cat >> ~/.bashrc << END
    emem () { java -jar ~/jars/emem.jar $@; }
    END
    . ~/.bashrc


## Usage

### Library

Add the following expression to the `ns` declaration of your source
clj:

    [emem.core :as emem]

To produce `README.html` from `README.md`:

    (emem/produce "README.html" ["README.md"])

To produce `reminders.html` from multiple sources, specifying a custom
title:

    (emem/produce "reminders.html" ["buy.md" "projects.md" "fitness.md"] :title "AAAAH!!!")

To learn more about the available options:

    (doc emem/produce)


### CLI

To produce `README.html` from `README.md`:

    emem -o README.html README.md

To produce a bare and undecorated HTML:

    cat README.md | emem -b

To produce raw HTML:

    echo "# Blah" | emem -r

To create an HTML listing of the current directory:

    ls -R | sed -e '1i```bash' -e '$a```' \
    | emem -T `basename $PWD` -o ls.html

To learn more about the available options:

    emem -h

## Dependencies

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [fs](https://github.com/raynes/fs/)
* [cpath-clj](https://github.com/xsc/cpath-clj)
* [highlight.js](https://github.com/isagalaev/highlight.js)


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
