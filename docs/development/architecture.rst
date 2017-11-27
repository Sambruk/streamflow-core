Architecture
############

This page outlines the Streamflow Core architecture, on a deployment level, system level, and some internal details that makes it easier to understand how it all works. Target audience are mainly developers wanting to understand Streamflow as a software system.

Deployment
**********
From a deployment point of view, Streamflow Core has been designed as a standard JavaEE Web application, packaged as two WAR files. One WAR file contain the server part of the application, and the other contains a Java WebStart client that can be downloaded in order to interact with the system. Both WAR files are typically deployed in the same JavaEE container, such as Tomcat or Glassfish.

Once deployed, a user can access and install the Java WebStart client by accessing /client, from where he can download the client. This client then utilizes the REST API (Level 3 on maturity model) to perform all use cases in Streamflow. A standard web browser can also be used to inspect the REST API, which is deployed to the /streamflow web context.

Web application architecture
****************************

The Streamflow Core server application is designed as follows. The web application delegates to the Restlet framework, which in turn delegates to a Streamflow Restlet application. This application then instantiates the Qi4j application development framework, and assemble the Streamflow application, and instantiates an instance of it. This application has the following seven layers:

    * Management

    * REST API

    * Context. This is where the use cases are implemented

    * Application services

    * Domain model

    * Infrastructure services

    * Configuration

For details on how these layers are connected to each other, please see StreamflowWebAssembler.

Configuration
=============
The configuration of Streamflow is implemented using the Qi4j service configuration mechanism. Defaults for settings are provided in properties files, and once the application is instantiated a live configuration instance is created, which is stored using the Preferences API. Each platform uses different means for backing this (Windows Registry, .plist files, XML files).

Infrastructure services
=======================
The main infrastructures relate to data persistence, indexing, and event sourcing store. The data persistence and event source store are implemented using JDBM, with implementations coming from the Qi4j project. The indexing is done using an RDF store called Sesame, again using the Qi4j implementation. For free text searching there is an additional indexing using Solr/Lucene. These three are the backbone of how data is stored, queried, and shared.

Domain model
============
The domain model is implemented as Qi4j Entities and Values. Each Entity consist of an EntityComposite interface, which in turn extends any number of interfaces that each represent the various aspects of the entity. To add new types of entities, create a new EntityComposite subtype. To add more functionality to an existing entity, either extend one of the current mixin interfaces, or create new ones. Each aspect should have a Data part (for state), an Events part (for events), and a part with commands to be invoked to trigger the changes.

Application services
====================
The application services provide a wide range of service-oriented functionality, such as sending/receiving emails, creating statistics, and consuming domain events in order to perform use cases as a "trigger". Any services that involve external systems should use circuit breakers, to allow for easy monitoring of the health of the system.

Context
=======
The use cases in Streamflow are implemented in the context layer using the DCI paradigm. This means that each use case is represented by one or more contexts which perform selection of objects, and then allow interactions on those objects to be performed. They then invoke methods on the domain model to perform state changes.

REST API
========
The contexts fo1rm the basis for the REST API, where each context (or set of contexts) are exposed as REST resources. Each "/" in a URL represent a context, and each query/command interaction in that context is then exposed as "/somename" resources. Subresources, which are used for sub use cases or object selections, are then exposed as "/someothername/", i.e. with "/" at the end again. Links are used extensively to drive use cases forward, and these are used by the client as much as possible.

The parsing of the URL is done hierarchically, and so constraints and authorization checks can be done at any level, thus implementing "fail-fast" with regard to request handling.

Management
==========
The management layer provides JMX beans that expose services, circuit breakers, and other features that help in managing the application as a whole. These can be accessed through VisualVM, either through local connection or the remote JMX connector.

Client architecture
*******************
The client is deployed as a Java WebStart client, which internally uses the Better Swing Application Framework. It also uses Qi4j extensively to perform dependency injection. It is based on the MVC paradigm, and so each part of the client has a Model, a Controller, and a Swing view that is instantiated and controlled through the Controller. The client also makes extensive use of GlazedLists and JGoodies FormBuilder, to deal with the remote REST API and also layouting.

Model-View-Controller interaction
=================================
Each part typically has a structure like this: a View is instantiated which hooks up the @Actions to the Swing components. On refresh (typically done when view is shown) the REST API is queried, and the model updated. When actions are triggered they will delegate to the model, which calls the REST API to perform any changes. Results are typically a list of domain events, which are distributed through the view, which may optionally pass them on to the model.

Data migration
**************
Each object is stored in JDBM as a serialized JSON-object, which includes the version which was used to store it. Whenever the data for an entity is changed (added, removed, renamed, etc.) a migration of that state has to be done in order to convert it to the latest version. This is done by using the Qi4j Migration API.

Essentially, for each new version that requires data changes a set of migration rules needs to be added. This is done in MigrationAssembler. Here is an example:

    .. code-block:: java

        toVersion("0.3.20.962").
        renamePackage("se.streamsource.streamflow.web.domain.form", "se.streamsource.streamflow.web.domain.entity.form").
        withEntities("FieldEntity",
            "FieldTemplateEntity",
            "FormEntity",
             "FormTemplateEntity").
        end().

For more details and examples, see MigrationAssembler. These rules can either be applied at startup or whenever an object is accessed for the first time with the new version.