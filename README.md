emem
====

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a trivial Markdown to HTML converter.


## Installation

### Leiningen

```clojure
[emem "0.2.6-SNAPSHOT"]
```

### Maven

```xml
<dependency>
  <groupId>emem</groupId>
  <artifactId>emem</artifactId>
  <version>0.2.6-SNAPSHOT</version>
</dependency>
```

### Binaries

#### Standalone JAR

If you already have Leiningen installed, proceed to the next
step. Otherwise, follow the installation instructions at
<http://leiningen.org/#install>.

To build the JAR, run the following inside the checkout directory:

```bash
$ lein uberjar
```

The JAR of interest here is the standalone one, located at
`./target/uberjar/emem-0.2.6-SNAPSHOT-standalone.jar`. This JAR
contains _emem_ itself, plus all the dependencies. Copy this file to
`~/bin`, as `emem.jar`.

```bash
$ cp target/uberjar/emem-0.2.6-*-standalone.jar  ~/bin/emem.jar
```

Next, create a shell script to reduce typing.

```bash
$ emacs ~/bin/emem
```

Put the following:

```bash
#!/bin/sh
java -jar $HOME/bin/emem.jar $@
```

Save your changes, then make it executable:

```bash
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


#### Builds

If you are unable to build the JAR or EXE, you may be able to get the
latest version from <https://goo.gl/oDqZAo>.


## Usage

### CLI

To convert `README.md` to `README.html`:

    $ emem README.md

To convert all `.md` files in the current directory:

    $ emem .

To convert all `.md` files in the directory `~/Desktop/notes/`:

    $ emem ~/Desktop/notes

In continuous mode, _emem_ will wait for changes to your files. When a
change has been detected, it automatically rebuilds the HTML files. It
will remain to monitor for changes, until <kbd>Ctrl-C</kbd> is
pressed:

    $ emem -c ~/Desktop/notes

The continuous mode works great when used with browser extensions that
reload a page when a page gets updated. The ones I can suggest are:

* [LivePage](https://chrome.google.com/webstore/detail/livepage/pilnojpmdoofaelbinaeodfpjheijkbh/related?hl=en) (Chrome)
* [Auto Reload](https://addons.mozilla.org/en-US/firefox/addon/auto-reload/?src=api) (Firefox)

_emem_ accepts input from stdin, too. The following command outputs a
1:1 Markdown:HTML equivalence

    $ echo "# Blah" | emem -w

To create an HTML listing of the current directory:

    $ ls -R | sed -e '1i```bash' -e '$a```' \
    | emem -t `basename $PWD` -o ls.html

To change the top-level CSS:

    $ emem -C custom.css list.md

To change the syntax highlighter CSS:

    $ emem -S zenburn repairs.md

To list the available syntax highlighter styles:

    $ emem -L

To learn more about the available options:

    $ emem -h


### API

Add the following expression to `(ns ...)`:

```clojure
(:require [emem.core :as mm])
```

To convert `README.md` to `README.html`:

```clojure
(mm/convert "README.md")
```

To convert `README.md` to `foo.html`:

```clojure
(mm/convert "README.md" "foo.html")
```

To convert multiple files:

```clojure
(mm/convert ["foo.md" "bar.md" "baz.md"])
```

To merge multiple files, to `reminders.html`:

```clojure
(mm/convert ["buy.md" "projects.md" "fitness.md"]
            :merge true
            :out "reminders.html")
```

To convert a Markdown string to an HTML string:

```clojure
(mm/markdown "# Blah")
```

To learn more about the available options:

```clojure
(doc mm/convert)
```


## Dependencies

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [cpath-clj](https://github.com/xsc/cpath-clj)
* [highlight.js](https://github.com/isagalaev/highlight.js)


## License

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
