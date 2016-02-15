
Example from "Getting Started with Apache Servicemix" webinar and Camel in Action
========================================================

The Camel routes used in this example are explained by the following diagram:

![EIP Diagram](https://raw.github.com/FuseByExample/rider-auto-osgi/master/doc/EIP_Routes_Diagram.png)

To run this example project, build the project and deploy to JBoss Fuse 6.1  
according to the steps below. 

Setup
==============================

- Install JBoss Developer Studio 7.1.1 [https://www.jboss.org/products/devstudio.html]
- Install Apache Maven 3+ [http://maven.apache.org]
- Install JBoss Fuse  6.1.x [https://www.jboss.org/products/fuse.html]

Build & Run (Stand alone Fuse)
==============================

### Build this project so bundles are deployed into your local maven repo

><project home> $ mvn clean install

### Start JBoss Fuse

><JBoss Fuse home>  $ bin/fuse

### Add this projects _features.xml_ config to Fuse from the Console
   (makes it easier to install bundles with all required dependencies)

>JBossFuse:karaf@root>  features:addUrl mvn:org.jboss.fuse.examples/rider-auto-common/5.0-SNAPSHOT/xml/features

### Install the project.

>JBossFuse:karaf@root>  features:install rider-auto-osgi

### To test the file processing, there are existing files in the
   `rider-auto-common` module.

><project home> $ cp rider-auto-common/src/data/message1.xml <JBoss Fuse home>/target/placeorder

To see what happened look at the log file, either from the console

>JBossFuse:karaf@root>  log:display

or from the command line

><JBoss Fuse home> $ tail -f data/log/fuse.log

### To test the WS, use your favorite WS tool (e.g. SoapUI) against the following
   WSDL hosted by the `rider-auto-ws` bundle.
 
>http://localhost:8182/cxf/order?wsdl

Build & Run (Fuse Fabric)
=========================
These steps are for building and deploying into a JBoss Fuse Fabric. Fabric is built on the Fabric8 1.x 
community releases. For more information, visit the [Fabric8.io website for the 1.x documentation](http://fabric8.io/gitbook/index.html). The steps will also show you how to quickly start up Fabric from a _standalone_ container:

### With Fuse running, create a fabric

>JBossFuse:karaf@root>  fabric:create --clean --wait-for-provisioning

### Check the console to verify fabric created

Navigate to [http://localhost:8181](http://localhost:8181) and verify the console has changed to a 
Fabric management perspective and not the standalone console (ie, you see a Runtime, Wiki, etc tab)

### Build and deploy the profiles from your project

From the root of the project, run the following command to build and deploy the profiles (note, this depends on
the fabric8 maven plugin being set up correctly. NOTE: See the [docs for the fabric8 maven plugin](http://fabric8.io/gitbook/index.html) to see how to do that.

><project home> $ maven fabric8:deploy

### Check the Fabric console

At this point, if you check the profile tree in Fabric (eg, under the "Wiki" tab), you should see 
a hierarchy like this:

rider.auto.osgi/backend
rider.auto.osgi/ws
rider.auto.osgi/file
rider.auto.osgi/normalizer

### Deploy
At this point you can now deploy the profiles to Fuse containers using the Fabric management tools. Note,
at the moment the connections to the broker are coded to expect a broker at `tcp://localhost:61616`. If you wish
to take advantage of Fabric broker discovery, you'll need to update the broker URIs or use the `amq:` component

Getting Help
============================

If you hit any problems please let the Fuse team know on the forums
  [https://community.jboss.org/en/jbossfuse]
