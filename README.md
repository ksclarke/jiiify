# Jiiify Image Server [![Build Status](https://travis-ci.org/ksclarke/jiiify.png?branch=master)](https://travis-ci.org/ksclarke/jiiify)

The Jiiify Image Server is a Java-based IIIF image server that, among its other features, provides backwards API compatibility with the now defunct adore-djatoka image server.  It is designed to be simple to install, run, and scale.  For more detailed information about it, consult its [project page](http://projects.freelibrary.info/jiiify).

### Installation

There are no stable Jiiify releases yet.

To install a development release there are several steps.  Because Jiiify uses some libraries that are not yet stable, some project dependencies need to be pre-installed before installing Jiiify.  This will not be the case once Jiiify has reached a stable status.  At that point, all dependencies will be handled by Jiiify's Maven build.

To start, download and install the [iiif-presentation-api](https://github.com/datazuul/iiif-presentation-api) software library.  To do this, you will need [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [git](https://git-scm.com/), and [Maven](http://maven.apache.org/) installed on your local machine (this is left as an exercise for the reader).

Once these pre-requisites are installed, you can install a development copy of the Jiiify Image Server:

    git clone https://github.com/ksclarke/jiiify.git
    mvn clean install -Denforcer.skip=true

To start Jiiify, from the project directory, type:

    target/startup.sh

The server should then be available at: [https://localhost:8888](https://localhost:8888). The out of the box install uses a self-signed certificate, so you will be warned about this on connecting for the first time.

### Getting Started

[Put something here]

### License

[BSD 3-Clause License](https://raw.githubusercontent.com/ksclarke/jiiify/master/LICENSE.txt)

### Acknowledgements

This project contains [IIIF Presentation API code](https://github.com/datazuul/iiif-presentation-api) written by Ralf Eichinger ([datazuul](https://github.com/datazuul)). This code is licensed by him under the [Apache License (Version 2.0)](https://raw.githubusercontent.com/datazuul/iiif-presentation-api/master/LICENSE) and included in this project according to its license.

### Contact

If you have questions about the Jiiify Image Server feel free to contact Kevin S. Clarke &lt;<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>&gt;.

If you encounter a problem or have a feature to suggest, submit it to the [issue queue](https://github.com/ksclarke/jiiify/issues "GitHub Issue Queue").