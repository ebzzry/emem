emem
====

[![Build Status](https://travis-ci.org/ebzzry/emem.svg)](https://travis-ci.org/ebzzry/emem)

_emem_ is a trivial Markdown to HTML converter. This library leverages
on [markdown-clj](https://github.com/yogthos/markdown-clj), and
[hiccup](https://github.com/weavejester/hiccup) to produce HTML.


Installation
------------

### Leiningen

```clojure
[emem "0.2.18-SNAPSHOT"]
```

### Maven

```xml
<dependency>
  <groupId>emem</groupId>
  <artifactId>emem</artifactId>
  <version>0.2.18-SNAPSHOT</version>
</dependency>
```

### Binaries

#### Nix

Install [Nix](https://nixos.org/nix), if you don’t have it, yet:

```bash
$ curl http://nixos.org/nix/install | bash
```

Then, install emem with:

```bash
$ nix-env -iA nixpkgs.emem
```

#### Uberjar

If you can’t use Nix, or you just want the JAR file, you can build a
standalone JAR file that contains all the dependencies. To create
one, install [Leiningen](http://leiningen.org/#install), first. To
build the JAR, run the following command inside the checkout
directory:

```bash
$ lein uberjar
```

This command generates two JAR files. The file that we need is the
standalone
one—`./target/uberjar/emem-0.2.18-SNAPSHOT-standalone.jar`. Copy this
file to `~/bin`, as `emem.jar`.

```bash
$ cp target/uberjar/emem-0.2.18-*-standalone.jar ~/bin/emem.jar
```

Next, create a shell script to ease typing.

```bash
$ emacs ~/bin/emem
```

Then, put the following:

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


#### Releases

If you are unable to build a JAR or EXE, you may download a
[binary release](https://github.com/ebzzry/emem/releases).


Usage
-----

### CLI

To convert `README.md` to `README.html`:

    $ emem README.md

To convert all `.md` files in the current directory:

    $ emem .

To convert `README.md` and embed the CSS data to a standalone `README.html`:

    $ emem -s README.md

To convert all `.md` files in the directory `~/Desktop/notes/`:

    $ emem ~/Desktop/notes/

In continuous mode, _emem_ will wait for changes to your files. When a
change has been detected, it automatically rebuilds the HTML files. It
will remain to monitor for changes, until <kbd>Ctrl-C</kbd> is
pressed:

    $ emem -c ~/Desktop/notes

The continuous mode works great when used with browser extensions that
reload a page when a page gets updated. The ones I can suggest are:

* [LivePage](https://chrome.google.com/webstore/detail/livepage/pilnojpmdoofaelbinaeodfpjheijkbh/related?hl=en) (Chrome)
* [Auto Reload](https://addons.mozilla.org/en-US/firefox/addon/auto-reload/?src=api) (Firefox)

When ran without arguments, _emem_ will accept input from stdin. The
following command accepts any Markdown-valid input, including regular
text, then outputs to screen the raw HTML equivalent, using the `-w`
option. It will remain to accept input, until <kbd>Ctrl-D</kbd> is
pressed:

    $ emem -w
    # foo
    **bold**
    _emph_
    ^D

The following is an equivalent command of the above:

    $ echo '# foo\n**bold**\n_emph_' | emem -w

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

To convert multiple files, to different output names:

```clojure
(mm/convert ["foo.md" "bar.md" "baz.md"]
            ["mu.html" "ka.html" "mo.html"])
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


Dependencies
------------

* [markdown-clj](https://github.com/yogthos/markdown-clj)
* [hiccup](https://github.com/weavejester/hiccup)
* [tools.cli](https://github.com/clojure/tools.cli)
* [cpath-clj](https://github.com/xsc/cpath-clj)
* [highlight.js](https://github.com/isagalaev/highlight.js)


License
-------

Copyright © 2015 Rommel Martinez

Distributed under the Eclipse Public License
