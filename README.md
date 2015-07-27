emem
======================================================================

A trivial Markdown to HTML converter that uses

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [highlight.js](https://github.com/isagalaev/highlight.js)


## Installation

Download from [https://ebzzry.github.io/emem](https://ebzzry.github.io/emem).

## Usage

emem can be run via lein, or from the standalone jar:

```bash
$ lein run -- -o foo.html file.md

OR

$ java -jar emem-0.1.0-SNAPSHOT-standalone.jar -o foo.html file.md
```

Specifying multiple input files will merge the output together:

    $ java -jar emem-0.1.0-SNAPSHOT-standalone.jar -o foo.html file1.md file2.md ...

Examples of the output can be found in the `examples/` directory.


## Options

    -o, --output HTML_FILE      output file
    -t, --title TITLE           document title
    -H, --header HEADER         document header
    -T, --titlehead TEXT        like -t TEXT -H TEXT
    -v                          increase verbosity
    -h, --help

## Bugs


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
