title=Jiiify Image Server
date=2017-06-20
type=page
status=published
~~~~~~

## Introduction

Jiiify is an experimental, Java-based, IIIF (Version 2, Level 0) image server built with [Vert.x](http://vertx.io/) (an event-driven, non-blocking, reactive tool-kit). Jiiify is still in active development and should not yet be considered ready for production use.

As a Level 0 IIIF image server, Jiiify does not generate images on-the-fly, but pre-generates the tiles and thumbnails necessary to use [Mirador](http://projectmirador.org/) and [OpenSeadragon](https://openseadragon.github.io/) (and perhaps other IIIF compatible image viewers that work with tiled images). It is, by design, de-coupled from other systems, so archival images should be ingested into Jiiify so that tiles can be generated. Jiiify does not store the archival images, just the tiles and other derivative images (thumbnails, etc.) that are created. The archival images should continue to live in their repositories or on their separate archival file systems.

Note that in order to use Jiiify with Mirador, or other similar viewers, one must create IIIF Presentation API manifests and upload them into Jiiify using its Web-based administrative interface. There is not, currently, any mechanism for creating or editing IIIF presentation manifests within Jiiify itself. Currently, at my place of work, we use a script that builds a IIIF manifest from files on the file system and a CSV document with metadata.

## Installing Jiiify

Since there are no stable releases, to install Jiiify you should follow the instructions in the project's GitHub [README](https://github.com/ksclarke/jiiify/blob/master/README.md). This will build the code and start it on your machine.

Jiiify runs behind HTTPS by default. For testing purposes, a self-signed certificate is created with each new build and used when the server is run. If you want to use your own SSL certificate, you can change the `jiiify.jks` setting in the `startup.sh` or `supervisord.conf` files (depending on which method you're using to run Jiiify). To change that value in both files at the point of build, you can supply `-Djiiify.jks=/etc/your.jks` to the build on the command line or store that value in your system's [Maven settings.xml](https://maven.apache.org/settings.html) file.

If you'd like to use Let's Encrypt as a certificate provider, there is a [script](https://github.com/ksclarke/jiiify/blob/master/src/main/scripts/letsencrypt2jks.sh) in the project's `src/main/scripts` folder that will convert a Let's Encrypt certificate to a JKS certificate. It makes the assumption that the Let's Encrypt certificates reside in `/etc/letsencrypt/live/`, which may not be the case for every Let's Encrypt client(?) In any case, it provides the basic steps needed to convert to a JKS keystore (paths can be adjusted as needed).

Other values that you might want to set at build time include: `jiiify.memory`, `jiiify.data.dir`, `jiiify.json.config.path`, `jiiify.logs.dir`, `jiiify.user`, `jiiify.host`, and `jiiify.solr.server`.

These do not need to be set if you're just testing on localhost and using the `startup.sh` script to run Jiiify, but if you want to run somewhere other than localhost you may want to change some of these values.

* `jiiify.memory` - The amount of memory alloted to Jiiify; by default, the Maven build will configure the `startup.sh` script to use 80% of what's available on your system. It will scale the number of image processing cores back if there is not enough memory to run with all the available cores.
* `jiiify.data.dir` - The location of the data Pairtree (where Jiiify stores its data files); by default this is in the same directory from which Jiiify is being run.
* `jiiify.json.config.path` - The location of a configuration file with a few variables; a sample configuration file is available in the project's `src/main/resources` folder (this is what's used if an alternative isn't provided).
* `jiiify.logs.dir` - The location where logs should be written; by default with the `startup.sh` script, this is in the project's `target` directory.
* `jiiify.user` - A user to run Jiiify as; this is useful if you're running Jiiify from Supervisord in some other location than the project directory.
* `jiiify.host` - The host on which Jiiify is being run; by default, this is localhost but if you wanted to run this on a remote machine you'd need to change this value.
* `jiiify.solr.server` - The Solr instance to which Jiiify will connect; by default, the `startup.sh` script tries to install and run Solr in a Docker container on the localhost machine (you need to have Docker installed on your machine for this to work, of course).

## Configuring Authentication

The README in the project's GitHub repository will help you get Jiiify started, but you will not be able to login without first configuring the OAuth system. Fwiw, the current authentication system is just a temporary placeholder for something more sophisticated (to be developed before producing a stable release). Currently, Google is used as the OAuth provider, so you will need to have a Google account and to add your Google email and Google client ID to the [configuration file](https://github.com/ksclarke/jiiify/blob/master/src/main/resources/sample-config.json) that's passed to Jiiify when it starts. The stable release will probably offer multiple OAuth providers in addition to an htdigest option.

To get Google setup as an OAuth provider, you need to log into the [API Manager in Google's Developer Console](https://console.developers.google.com/apis/dashboard). Once there, you should follow the example in the screenshot below and configure "Authorized Javascript Origins" and the "Authorized Redirect URLs" values. If you're just testing on your localhost machine, the localhost values are the only ones you need to supply. If you want to test on a remote server, you'll need to include those domains as well. Make sure to include port numbers if you're running at a non-default HTTPS port (e.g. not 443).

![Screenshot of API Manager in Google Developer Console](api-manager-example.png)

## Ingesting Images

Once you can login to the administrative interface, you can ingest images into Jiiify. There are a couple of methods of doing this, but at this point just the CSV import will be described. To test this method you just need a CSV file in the format described by the screenshot below:

![Screenshot of Jiiify CSV ingest page](csv-ingest-screenshot.png)

Select the Choose File button to upload your file and then click the upload button. If you've already ingested this batch of records, and want to overwrite what's in the system with new tiles, etc., make sure you click the "Overwrite existing data" checkbox. The submission of this form will return a page that indicates the ingest job has been started. In a while, the ingested object will appear in the "Browse" results page.

At this point, there isn't a nice view on the ingest process that will tell you how far along the process is and whether there were any errors. For the time being you can `tail -f` the log file or `grep` it for "ERROR" (without the quotes). A visual view into the process is obviously something that will be needed before Jiiify reaches a stable state and is officially released as a production ready piece of software. There is an underlying Vert.x supplied metrics package already integrated into Jiiify, but the ingest process needs to record events in it, and a nice visual Javascript viewer needs to be written (or borrowed from other sources).

If you want to view ingested images in Jiiify, you can visit the Browse page. Also on the browse page is an option to download a zip file of an ingested object, including all its tiles. This can be seen in the following screenshot:

![Screenshot of Jiiify browse page](browse-screenshot.png)

As a warning, there are parts of the administrative interface that are stubbed out, but do not yet function (for instance, the "refresh" links). There will be a notice of "Yet to be implemented" when navigating to them in the browser.

## Ingesting Manifests

Lastly, to use Jiiify with Mirador, a IIIF Presentation API manifest needs to be created and uploaded into Jiiify through its administrative interface. This is just like uploading the CSV file with images. The option to do this can be found through the Ingest menu item:

![Screenshot of Jiiify manifest ingest page](ingest-manifest-screenshot.png)

Like with CSV file ingests, if you want to overwrite a manifest that already exists in the system, the "Overwrite manifest if it already exists" checkbox needs to be clicked.

At my place of work we use a script that generates manifests from files on the file system and a supplied CSV metadata file. I'll try to include a link to it, here, in the near future, since others might find it useful, too.

Jiiify uses Mirador for the display of images from its browse page. To do this it creates a simple manifest for each item (in addition to the info.json file associated with each object). If you want to view either of these, there are links for them from the Browse page.

## Future Work

There is still a lot of work to be done on Jiiify before I'd consider it stable and ready for general use. There should be corresponding tickets, but I'll list a few of the desired features here that I think need to be done before Jiiify is promoted as a tool others could/should use:

* A ingest process/results viewer (with metrics of how many were processed, how many failed, etc.)
* Rewritten authentication system to use the newer Vert.x OAuth and htdigest modules
* Ansible / Packer.io wrapper so it can be distributed as a VM, AMI, or Docker image
* Updated configuration system to rely more on the configuration file and less on build variables
* Additional tests on the S3 Pairtree (it should be as reliable as the file system implementation)
* Improved documentation (what's provided here is really just a start -- it could be a lot better)
* Polishing of the "Fedora/Camel-Vert.x bridge" and the "Watch Folder" ingest mechanisms
* Finish testing the HTTP/2 implementation (it's currently available, but turned off by default)
* Perhaps an internal implementation of search so that an external Solr wouldn't be required to run
* Testing of Vert.x's clustering feature so that multiple Jiiify instances could better work together

If you're interested in these or other features, you might be interested in the [Architecture](architecture.html) page (also under the Documentation drop down in the top menu).

## Contact

Feel free to <a href="mailto:ksclarke@ksclarke.io">send me</a> comments, suggestions, problems testing it, etc. This is definitely a work in progress and I'd be interested in hearing from others interested in its progress. There is also an [issues queue](https://github.com/ksclarke/jiiify/issues) if you run into a bug or something that could be improved and would like to report it.
