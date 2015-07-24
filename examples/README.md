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

    $ lein run -- -o file.html file.md

    OR

    $ java -jar emem-0.1.0-SNAPSHOT-standalone.jar -o file.html file.md

Examples of the output can be found in the `./examples/` directory.

## Options

Specify the output file. Defaults to stdout.

    -o, --output FILE

Specify the verbosity.

    -v


## Bugs


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
