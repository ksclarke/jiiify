# Jiiify Image Server [![Build Status](https://travis-ci.org/ksclarke/jiiify.png?branch=master)](https://travis-ci.org/ksclarke/jiiify)

The Jiiify Image Server is a Java-based IIIF image server that, among its other features, provides backwards API compatibility with the now defunct adore-djatoka image server.  It is designed to be simple to install, run, and scale.  For more detailed information about it, consult its [project page](http://projects.freelibrary.info/jiiify).

### Installation

There are no stable Jiiify releases yet.

To install a development release, you will need [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [git](https://git-scm.com/), and [Maven](http://maven.apache.org/) installed on your local machine (this is left as an exercise for the reader).

Once these pre-requisites are installed, you can install a development copy of the Jiiify Image Server:

    git clone https://github.com/ksclarke/jiiify.git
    mvn clean install -Denforcer.skip=true

To start Jiiify, from the project directory, type:

    target/startup.sh

The server should then be available at: [https://localhost:8443](https://localhost:8443). The out of the box install uses a self-signed certificate, so you will be warned about this on connecting for the first time.

### Getting Started

[Put something here]

### Connecting a JDWP agent or JMX monitor

You can build the project with support for connecting a JDWP agent by running with:

    mvn clean install -Ddev.tools=JDWP_AGENTLIB

Or with support for a JMX monitor:

    mvn clean install -Ddev.tools=JMX_REMOTE

Or with both:

    mvn clean install -Ddev.tools="JDWP_AGENTLIB JMX_REMOTE"

You can also supply the `dev.tools` variable in a default Maven profile. See `src/main/resources/settings.xml` for an example.

### License

[BSD 3-Clause License](https://raw.githubusercontent.com/ksclarke/jiiify/master/LICENSE.txt)

### Contact

If you have questions about the Jiiify Image Server feel free to contact Kevin S. Clarke &lt;<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>&gt;.

If you encounter a problem or have a feature to suggest, submit it to the [issue queue](https://github.com/ksclarke/jiiify/issues "GitHub Issue Queue").