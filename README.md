emem
====

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a trivial Markdown to HTML converter.


## Installation

### Leiningen

```clojure
[emem "0.2.1-SNAPSHOT"]
```

### Maven

```xml
<dependency>
  <groupId>emem</groupId>
  <artifactId>emem</artifactId>
  <version>0.2.1-SNAPSHOT</version>
</dependency>
```

### Binaries

#### Standalone JAR

To build the JAR, run the following inside the checkout directory:

```bash
$ lein uberjar
```

The JAR of interest here is the standalone one, located at
`./target/uberjar/emem-0.2.1-SNAPSHOT-standalone.jar`. This JAR
contains _emem_ itself, plus all the dependencies. Copy this file to
`~/bin`, as `emem.jar`.

```bash
$ cp target/uberjar/emem-0.2.1-*-standalone.jar  ~/bin/emem.jar
```

Next, create a shell script to reduce typing. This presumes that you
have `~/bin/` in `$PATH`:

```bash
$ cat > ~/bin/emem << END
#!/bin/sh
java -jar $HOME/bin/emem.jar $@
END
$ chmod +x ~/bin/emem
```

#### Windows 32-bit PE

Once you have the standalone JAR, creating a Windows exe is relatively
easy. Download [Launch4j](https://fbergmann.github.io/launch4j/) and
use it to create the executable. The bare-minimum fields are:

* Basic > Output file
* Basic > Jar
* Header > Header type
* JRE > Min JRE version

The _Output file_ and _Jar_ fields are self-explanatory. For
_Header type_, select `Console`, while for _Min JRE version_,
specify `1.1.0`.

## Usage

### API

Add the following expression to `(ns ...)`:

```clojure
(:require [emem.core :as emem])
```

Convert `README.md` to `README.html`:

```clojure
(emem/convert ["README.md"] :out "README.html")
```

Convert multiple sources to `reminders.html`, using a custom title:

```clojure
(emem/convert ["buy.md" "projects.md" "fitness.md"] :out "reminders.html" :title "AAAAH!!!")
```

Convert a Markdown string to HTML:

```clojure
(emem/convert "# Blah")
```

By default, `convert` prints to `*out*`:

```clojure
(emem/convert ["notes.md"])
```

Output to `../todo.html`, by binding `*out*`:

```clojure
(let [file "../todo.html"]
  (binding [*out* (io/writer file)]
    (emem/convert ["notes.md"])
    (emem/re-install file)))
```

To learn more about the available options:

```clojure
(doc emem/convert)
```


### CLI

Convert `README.md` to `README.html`:

    % emem -o README.html README.md

Get file contents from stdin, then output to stdout without CSS and JS:

    % cat README.md | emem -p

Get Markdown input from stdin, then output to stdout the 1:1 equivalent
of the input:

    % echo "# Blah" | emem -w

Create an HTML listing of the current directory:

    % ls -R | sed -e '1i```bash' -e '$a```' \
    | emem -T `basename $PWD` -o ls.html

If no inputs are provided, it will accept inputs from stdin. After
<kbd>Ctrl-D</kbd> is pressed, the converted text will be display to
the screen:

    % emem -w
    # foo
    **bar**

Run in continuous build mode -- build the HTML file, and if any of the
input files are updated, rebuild the HTML file automatically. It will
remain to monitor for changes, until <kbd>Ctrl-C</kbd> is pressed:

    % emem -co TODO.html TODO.md

The continuous mode works great when used with browser enhancements
that reload a page when the HTML file becomes modified/updated. The
most popular ones are:

* [Auto Reload](https://addons.mozilla.org/en-US/firefox/addon/auto-reload/?src=api) (Firefox)
* [LivePage](https://chrome.google.com/webstore/detail/livepage/pilnojpmdoofaelbinaeodfpjheijkbh/related?hl=en) (Chrome)

List the available style sheets that can be used with [highlight.js](https://github.com/isagalaev/highlight.js):

    % emem -L

To use a style sheet:

    % emem -C zenburn -o reminders.html shop.md repairs.md

Change the main style sheet for the page; the path specified will be relative to the HTML file:

    % emem -M css/custom.css -o list.html list.md

To learn more about the available options:

    % emem -h

## Dependencies

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [cpath-clj](https://github.com/xsc/cpath-clj)
* [highlight.js](https://github.com/isagalaev/highlight.js)


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
