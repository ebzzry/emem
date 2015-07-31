emem
======================================================================

_emem_ is a trivial Markdown to HTML converter.


## Installation


### Library

Add the following expression in the `:dependencies` clause of your
`project.clj`:

    [emem "0.1.2-SNAPSHOT"]

Add the following expression the the `ns` declaration of your source
clj:

    [emem.core :as emem]

### Command-line

Fetch the sources, build the JAR, then store it somewhere:

    git clone git@github.com:ebzzry/emem.git
    cd emem && lein uberjar
    mkdir ~/jars
    cp target/uberjar+uberjar/emem-0.1.2-SNAPSHOT.jar ~/jars/emem.jar


## Usage

### Library

To create `README.html` from `README.md` and `TODO.md`, merging them
together:

    (emem/produce "README.html" ["README.md" "TODO.md"])

To learn more about the available options and parameters:

    (doc emem/produce)

### CLI

To convert README.md to README.html

    java -jar ~/jars/emem.jar -o README.html README.md

To save typing, you may use shell functions:

    cat >> ~/.bashrc << END
    emem () { java -jar ~/jars/emem.jar $@; }
    em () { emem -o ${1%%.*}.html $1; }
    END

or a shell script:

    cat > ~/bin/emem << END
    #!/bin/sh
    java -jar ~/jars/emem.jar $@
    END
    chmod +x ~/bin/emem

Enabling us to just type:

    emem -o README.html README.md

or

    em README.md

*emem* accepts input from stdin:

    # Dump to screen
    cat README.md | emem
    
    # Like above, but output a bare and undecorated HTML
    cat README.md | emem -b
    
    # Produce a raw HTML output
    echo "# Blah" | emem -r
    
    # Create an HTML listing of the current directory
    ls -R | sed -e '1i```bash' -e '$a```' \
    | emem -T `basename $PWD` -o files.html

Other examples can be found in the `examples/` directory.


## Options

    Usage: emem [OPTION]... [MARKDOWN_FILE]...
    
    Options:
      -o, --output HTML_FILE  /dev/stdout  output file
      -r, --raw                            emit raw HTML
      -b, --bare                           emit bare HTML
      -H, --htmlonly                       emit full HTML, sans resources
      -R, --resonly                        install the resource files only
          --title TEXT                     document title
          --header TEXT                    document header
      -T, --titlehead TEXT                 like --title TEXT --header TEXT
      -v                                   increase verbosity
      -V, --version                        display program version
      -h, --help                           display this help


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
