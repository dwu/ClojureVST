Introduction
------------

This code allows developing VST plugins in [Clojure](http://clojure.org) based on [jVSTWRapper](http://jvstwrapper.sourceforge.net/). 

Status
------

The project is a proof of concept and not actively developed. 

Requirements
------------

* a 32-bit JVM
* a VST host
* jVSTWrapper (included)
* clojure (included)

Installation & running
----------------------

* Follow [jVSTWrapper's installation instructions](http://jvstwrapper.sourceforge.net/#installation).

* Copy `lib/clojure.jar`, `lib/clojure-contrib.jar` and `dist/clojurevst.jar` to the folder where you installed jVSTWrapper. 

* Copy the sample config (e.g. `dist/clojurevst-win.ini`) to the folder where you installed jVSTWrapper and rename it according to your plugin's dll name. See <http://jvstwrapper.sourceforge.net/#usage> for more information.

* Copy the sample plugins from `src/de/flupp/clojurevst/plugins` to the folder where you installed jVSTWrapper. The plugin to be used is defined in the ini file:

		ClojureVSTPluginFile=cljdelay.clj
		ClojureVSTPluginNamespace=de.flupp.clojurevst.cljdelay

* If `ClojureVSTPluginReload` is set to true, the *.clj files in the plugin folder are monitored for updates and reloaded upon saving.

* Start your VST host and load the jVSTWrapper plugin dll.

Development
-----------

The project includes an Eclipse project file as well as all required dependencies.

Copyright Notice
----------------

* "VST" is a trademark of Steinberg Media Technologies GmbH.

* The code includes a copy of [opaz-plugdk's](https://github.com/thbar/opaz-plugdk) `ProxyTools` class which provides a number of helper functions.  
