emem
====

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a Markdown to HTML converter.


## Installation

### Leiningen

```clojure
[emem "0.2.0-SNAPSHOT"]
```

### Maven

```xml
<dependency>
  <groupId>emem</groupId>
  <artifactId>emem</artifactId>
  <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### Binaries

#### Uberjar

> [emem.jar](https://github.com/ebzzry/emem/releases/download/v0.2-beta/emem.jar)
>
> tthsum: E6MTI73KBZPFJKE3N44JQPTSVXYN57VL5N3AITY

For purposes of demonstration, let's save this file to `~/jar`,
creating that directory as necessary. Next create a shell script to
reduce typing. This presumes that you have `~/bin/` in your `PATH`:

```console
cat > ~/bin/emem << END
#!/bin/sh
java -jar ~/jar/emem.jar $@
END
chmod +x ~/bin/emem
```

#### Windows 32-bit

> [emem.exe](https://github.com/ebzzry/emem/releases/download/v0.2-beta/emem.exe)
>
> tthsum: Z4GYUFOEOZPYL3RQEOVBBPVBW6NRBDIBPYLGUEI

Save this file somewhere in your `PATH`. Run `sysdm.cpl` to
view/modify your settings. This binary was created using
[Launch4j](https://fbergmann.github.io/launch4j/).


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

    emem -co TODO.html TODO.md

The continuous mode works great when used with browser enhancements
that reload a page when the HTML file becomes modified/updated. The
most popular ones are:

* [Auto Reload](https://addons.mozilla.org/en-US/firefox/addon/auto-reload/?src=api) (Firefox)
* [LivePage](https://chrome.google.com/webstore/detail/livepage/pilnojpmdoofaelbinaeodfpjheijkbh/related?hl=en) (Chrome)

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
