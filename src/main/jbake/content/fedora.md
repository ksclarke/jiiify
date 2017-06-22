title=Jiiify / Fedora 4 Integration
date=2017-06-20
type=page
status=published
~~~~~~

As a proof of concept, Jiiify includes a Fedora/Camel integration layer that demonstrates how TIFF images ingested into Fedora can be automatically sent to Jiiify for processing. This integration is accomplished through a Camel feature that leverages the [fcrepo-camel-toolbox](https://github.com/fcrepo4-exts/fcrepo-camel-toolbox). The Jiiify/Fedora Camel feature, that you will deploy into your Fedora/Camel instance can be found in Jiiify's Camel resources folder: [jiiify-rcrepo-ingest.xml](https://github.com/ksclarke/jiiify/blob/master/src/main/resources/camel/jiiify-fcrepo-ingest.xml). For the purposes of testing and demonstration, we'll use the [fcrepo4-docker](https://github.com/fcrepo4-labs/fcrepo4-docker) container.

To spin up your Fedora environment, after you have Docker installed according to your system's requirements, run:

    docker pull yinlinchen/fcrepo4-docker
    docker run -it -p 8080:8080 -p 9080:9080 --name fcrepo4 -d yinlinchen/fcrepo4-docker:4.7.2

<br/>
It will take a little time before the Fedora environment is active. You can check on its progress by visiting http://localhost:8080/fcrepo. Once you see an active site, you can continue. If you want more details about the environment that's spun up, consult the project's [GitHub page](https://github.com/fcrepo4-labs/fcrepo4-docker).

Since we're running a test instance of Jiiify that's using a self-signed SSL certificate, we have to make a hack to our system to be able to connect to it from the Docker container running Fedora. We're going to assign Jiiify, running on localhost, a host name and configure that name in our local `/etc/hosts` file, and in the `/etc/hosts` file running in the Docker container. First, let's go ahead and build Jiiify with this custom host name:

    mvn clean install -Djiiify.host=jiiify

<br/>
After that, add the following to your local `/etc/hosts` file:

     127.0.0.1       jiiify

<br/>
Next, we can go ahead and start Jiiify using the startup.sh file provided by the project. From within the Jiiify project's base directory, type:

     target/startup.sh

<br/>
After that has completed and you see `Successfully started 'JiiifyMainVerticle'`, you should be able to visit the default Jiiify login page located at: https://jiiify:8443

Assuming that worked, we'll move onto installing our Camel feature in the Fedora  Docker container. To log into your Docker container, type:

     docker exec -i -t fcrepo4 /bin/bash

<br/>
After that you should be in the Docker container and can change to the  Karaf directory (since Camel runs inside of Karaf in the container):

     cd /opt/karaf

<br/>
In there you will see a `deploy` directory among other things. Before we deploy the Camel feature, let's go ahead and add the `jiiify` host name to the container's /etc/hosts file. This time instead of using 127.0.0.1, we're going to use the local network IP address at which your machine (not the container) is running. I discover this on my machine by running `ifconfig` (outside of the Docker container). The `ifconfig` script reveals:

    wlp3s0    Link encap:Ethernet  HWaddr a9:38:d9:f2:19:f3
    inet addr:192.168.1.20  Bcast:192.168.1.255  Mask:255.255.255.0
    UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
    RX packets:12609757 errors:0 dropped:0 overruns:0 frame:0
    TX packets:7988398 errors:0 dropped:0 overruns:0 carrier:0 collisions:0 txqueuelen:1000 
    RX bytes:10492791310 (10.4 GB)  TX bytes:4339198229 (4.3 GB)

<br/>
From this, I can tell my laptop is running at 192.168.1.20. I can test this by visiting, https://192.168.1.20:8443/. When I do, I should get the Jiiify self-signed certificate prompt. Make sure to include the 'https' and the port: 8443.

Once I know the IP address that I need, I can go back to the open terminal that has my Docker bash shell open and add the following to the Docker container's /etc/hosts file:

    192.168.1.20       jiiify

<br/>
You will, of course, use whatever IP address your machine is using. After this whole dance has been done, I can now download the self-signed certificate from the Jiiify server to my Docker container and install it inside of the keystore that Java uses so that I will be able to connect to Jiiify without being prompted about the self-signed certificate.

First, from within the /opt/karaf directory in the Docker container, type:

    openssl s_client -showcerts -connect jiiify:8443 </dev/null 2>/dev/null|openssl x509 -outform PEM >jiiify.pem

<br/>
This puts the PEM file on your directory and then you'll need to install it into the Java keystore:

    sudo keytool -import -alias jiiify -keystore /etc/ssl/certs/java/cacerts -file jiiify.pem

<br/>
You'll be presented with a prompt:

    Enter keystore password:

<br/>
To which you'll type the default Java keystore password:

    changeit

<br/>
Now, your Camel (Jiiify/Fedora) feature will be able to connect with Jiiify without getting that annoying self-signed certificate warning. So before installing it, let's take a look at it:

```
<?xml version="1.0" encoding="UTF-8"?>

<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd http://camel.apache.org/schema/blueprint http://camel.apache.org/schema/blueprint/camel-blueprint.xsd">

  <camelContext xmlns="http://camel.apache.org/schema/blueprint" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:ebucore="http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#">

    <route id="JiiifyRouter">
      <from uri="activemq:topic:fedora" />
      <to uri="fcrepo:localhost:8080/fcrepo/rest" />
      <setProperty propertyName="mimetype">
        <xpath>/rdf:RDF/rdf:Description/ebucore:hasMimeType/text()</xpath>
      </setProperty>
      <setProperty propertyName="filename">
        <xpath>/rdf:RDF/rdf:Description/ebucore:filename/text()</xpath>
      </setProperty>
      <setProperty propertyName="about">
        <xpath>/rdf:RDF/rdf:Description/@rdf:about</xpath>
      </setProperty>
      <filter>
        <simple>${property.mimetype} == 'image/tiff'</simple>
        <log message="Sending ${property.filename} to Jiiify for processing" />
        <toD uri="https4://jiiify:8443/fcrepo-event?url=${property.about}&amp;file=${property.filename}" />
      </filter>
      <to uri="mock:result" />
    </route>

  </camelContext>

</blueprint>
```

<br/>
What it does, in essence, is to look at Fedora messages and pick up on messages about binary files with the mimetype 'image/tiff'. It then takes these messages and submits the file name and Fedora URL for the item to a Jiiify endpoint, that is listening for connections from a particular IP address (in the default case: 127.*). Jiiify takes that URL and downloads it using the supplied file name. It then adds it to its processing queue. Once all the tiles and thumbnails, etc., have been processed it will remove the TIFF file from its file system, keeping just the derivative images. This only pulls the image. It does not pull any of the metadata. At this point, creating IIIF manifests is something that needs to be done outside of Jiiify (and uploaded, using Jiiify's adminstrative Web interface).

So let's install the feature. After building the project, we can find the file at:

    ./target/classes/camel/jiiify-fcrepo-ingest.xml

<br/>
We've going to use a convenient little service, [transfer.sh](https://transfer.sh/), to get it to our Docker container. We're not going to encrypt it or anything since it's just a test file. First, we'll type, from our local machine:

    curl --upload-file ./target/classes/camel/jiiify-fcrepo-ingest.xml https://transfer.sh/jiiify-fcrepo-ingest.xml

<br/>
It will return something like:

    https://transfer.sh/10UVAs/jiiify-fcrepo-ingest.xml

<br/>
This is the URL we'll use from within our Docker container. So, first, change back to the terminal with the Docker container open. You should still be in the `/opt/karaf` directory. From there we can change into the `deploy` directory and download the XML file:

    cd deploy
    wget https://transfer.sh/10UVAs/jiiify-fcrepo-ingest.xml

<br/>
This will download the XML file and automatically deploy the file in the Karaf environment. You can see this by going into the Karaf shell:

     ../bin/client

<br/>
And looking at the logs by typing:

     log:display

<br/>
Now to test the integration, we can clear the Karaf logs with: `log:clear` and go to our Fedora instance in our browser: http://localhost:8080/fcrepo (this is connecting to our Fedora in the container even though we're accessing it through `localhost` because we bridged ports 8080 on the localhost machine and the Docker container). From the Fedora Web interface, we can add a TIFF file from our Jiiify project. A sample TIFF file for this purpose can be found in the Jiiify project at:

    ./src/test/resources/images/W102_000059_950.tif

<br/>
We should then be able to toggle back into the Docker container, within the Karaf shell, and run `log:display` again to see a log message from the JiiifyRouter. It will be up a bit in the logs so you might have to scroll through some other triplestore related log messages. Once you've seen the JiiifyRouter log message you can go back to your local machine and check the messages in Jiiify's logs.

When running Jiiify through the startup.sh script, Jiiify puts its logs in the project's `target` directory. You should see a date stamped log file similar to 'jiiify-2017-06-22.log'. You can `cat` that log file and look towards the end for messages related to image ingest or if you're quick enough you can just `tail -f` that log file and watch the tiles being created.

There is still work to be done to ensure communication errors are properly handled, etc., but this serves as a simple proof of concept that shows how an external repository like Fedora can be connected to Jiiify to allow Jiiify to be responsible for the derivative file generation.

If you encounter problems trying this, or would like some assistance stepping through the process, feel free to <a href="mailto:ksclarke@ksclarke.io">contact me</a>. It's just a proof of concept for now, not something that's being used in production so it may have issues. Thanks for sticking through these instructions until the end. There should be some sort of badge or reward for that, right?
