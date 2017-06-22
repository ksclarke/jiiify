# Jiiify Image Server [![Build Status](https://travis-ci.org/ksclarke/jiiify.png?branch=master)](https://travis-ci.org/ksclarke/jiiify)

### Introduction

Jiiify is an experimental, Java-based, IIIF (Version 2, Level 0) image server built with [Vert.x](http://vertx.io/) (an event-driven, non-blocking, reactive tool-kit). Jiiify is still in active development and should not yet be considered ready for production use.

As a Level 0 IIIF image server, Jiiify does not generate images on-the-fly, but pre-generates the tiles and thumbnails necessary to use [Mirador](http://projectmirador.org/) and [OpenSeadragon](https://openseadragon.github.io/) (and, perhaps, other IIIF compatible image viewers that work with tiled images). It is, by design, de-coupled from other systems, so archival images should be ingested into Jiiify so that tiles can be generated. It does not store the archival images, just the tiles and other derivative images (thumbnails, etc.) that are created. The archival images should continue to live in their repositories or on their separate archival file systems.

Note that in order to use Jiiify with Mirador, or other similar viewers, one must create IIIF Presentation API manifests and upload them into Jiiify using its Web-based administrative interface. There is not, currently, any mechanism for creating or editing IIIF presentation manifests within Jiiify itself. Currently, at my place of work, we use a script that builds a IIIF manifest from files on the file system and a CSV document with metadata.

### Installation

Jiiify is meant to be distributed as an executable jar file but, as there are no stable releases of Jiiify yet, if you want to experiment with it you'll need to build it yourself.

To do this, you will need [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [Git](https://git-scm.com/), and [Maven](http://maven.apache.org/) installed on your local machine. This is left as an exercise for the reader.

Once these pre-requisites are installed, you can install a development copy of Jiiify:

    git clone https://github.com/ksclarke/jiiify.git
    cd jiiify
    mvn clean install

While images can be ingested and served without connecting Jiiify to [Solr](http://lucene.apache.org/solr/), parts of the administrative interface do require a Solr connection (for browsing images in the administrative interface, etc.) If you don't already have Solr running on your local machine (or even if you do), you can use Docker to easily bring up Solr at the place that Jiiify expects to find it. To do this, type:

    docker run --name jiiify_solr -d -p 8983:8983 -t solr
    docker exec -it --user=solr jiiify_solr bin/solr create_core -c jiiify

This will bring up Solr and create a core for Jiiify's use. Of course, after the first time this has been done, you only need to type the following to start the Solr container:

    docker start jiiify_solr

You can also choose to skip the above steps if you have Docker installed on your machine. There is a `startup.sh` script for developers to use that will download and startup a Solr Docker container for you. To run that script to start Solr and Jiiify, from within the project's main directory, type:

    target/startup.sh

The server should then be available at: [https://localhost:8443](https://localhost:8443).

The out-of-the-box install uses a self-signed SSL certificate, so you will be warned about this on connecting in the browser and you'll have to click through that warning, acknowledging that a self-signed certificate is being used. At the moment, the self-signed certificate is regenerated every time you do a build, so each build will present you with a new opportunity to click through that warning when you connect to the administrative interface in a browser.

For more on using Jiiify (including how to ingest a sample image), visit the [project page](http://projects.freelibrary.info/jiiify).

### Running Jiiify in Production

Silly goose, you shouldn't be doing that yet.

But, there are [supervisord.conf](https://github.com/ksclarke/jiiify/blob/master/src/main/resources/supervisord.conf) and [jiiifyNagios.sh](https://github.com/ksclarke/jiiify/blob/master/src/main/scripts/jiiifyNagios.sh) files that you can use while running on a remote server, if you want.

### Connecting a JDWP agent or JMX monitor

You can build the project with support for connecting a JDWP agent by running with:

    mvn clean install -Ddev.tools="JDWP_AGENTLIB"

Or with support for a JMX monitor:

    mvn clean install -Ddev.tools="JMX_REMOTE"

Or with both:

    mvn clean install -Ddev.tools="JDWP_AGENTLIB JMX_REMOTE"

You can also supply the `dev.tools` variable in a default Maven profile. See [src/main/resources/settings.xml](https://github.com/ksclarke/jiiify/blob/master/src/main/resources/settings.xml) for an example.

Once you run the `target/startup.sh` script, you'll be told which ports to use to connect your profiling or debugging client. If you want to enable these options on a remote machine instead, you'll need to edit the `target/startup.sh` file (after you've run the build) to allow that.

### License

[BSD 3-Clause License](https://github.com/ksclarke/jiiify/blob/master/LICENSE.txt)

### Contact

If you have questions about Jiiify feel free to contact Kevin S. Clarke &lt;<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>&gt;.

If you encounter a problem or have a feature to suggest, submit it to the [issue queue](https://github.com/ksclarke/jiiify/issues "GitHub Issue Queue").