Developer cheat-sheet
#####################

General rules
*************
Keep sprint backlog and Jira issues you are working on synchronized.
Always run mvn clean install on the root pom before committing your code changes.
Naming conventions
When creating new commands and queries in server resources, make sure you only use lowercase characters. I.e. not "fooBar", use "foobar"
instead. On the web you don't use uppercase for names.

Accelerator keys
****************
The component (JXTree for outline, JXTable for task view) has an InputMap with the key combo registered.
You need to register a proxy action for it in the menu,eg "Sök"/"Find".
When a component that has the action that corresponds to the menu selected the accelerator (in this example: cmd-f/ctrl-f) becomes active.

CQRS and EventSourcing
**********************
**Commands** are *requests* to the domain model that something should happen. **Events** are *notifications* that something has happened. Names of commands should be in the **imperative** ("changeThis", "doThat") and names of events should be in **past tense** ("changedThis", "doneThat").

    * Command methods in domain object have the following purposes:
        * Validate that it is ok to perform it
            .. code-block:: java

                if (...) data.eventHappened(DomainEvent.CREATE, param1)

        * Create/invoke event methods on itself
            .. code-block:: java

                eventHappened(DomainEvent.CREATE, param1, param2);

        * Call other commands
            .. code-block:: java

                otherObject.changeDescription("new description");

    * Command methods may not:
        * Change state. If commands change state, then that state will be lost on replay of events
            * ``Wrong`` data.someProperty().set("new value")
            * ``Correct`` data.changedSomeProperty(DomainEvent.CREATE, "new value");

        * Call event methods on other objects. Instead a command on the other object should be called.
            * ``Wrong`` entity.changedDescription("foo")
            * ``Correct`` entity.changeDescription("foo")

        * Change state in other objects. Instead a command on the other object should be called:
            * ``Wrong`` ((Some.Data)other).someProperty().set("new value")
            * ``Correct`` other.changeSomeProperty("new value")

    * Event methods in domain object have the following purposes:

        * Change state
            .. code-block:: java

                data().someProperty().set(param1)

        * Create entities
            .. code-block:: java

                uowf.currentUnitOfWork().newEntity(SomeEntity.class, param1)

            .. note::
                Note that id of entity **MUST** be a parameter to the event method so that on replay the same id is used

    * Event methods may not:
        * Call other event methods. Event methods are only invoked as a response to an event. No other methods may call event methods.

        * Call command methods. If an event method calls a command, it will create new events. An event is a notification of "what happened" and may not cause new things to happen.

Commands and events on the client
*********************************
The client is responsible for firing off commands as REST calls as a response to UI actions. The events that are returned from a command can then be used by the client views and models to refresh state. Below are some guidelines on how to do this:

* All REST calls (cqc.postCommand,cqc.postLink, cqc.delete() etc.) must be invoked by subclasses of CommandTask. The CommandTask will gather the events from the responses, and then distribute them to the client view.

* **@Action** annotated view methods should return **Task** as type, and return instances of **CommandTask** that do the actual work.
* If a view wishes to receive notifications about events that occurred as a response to the commands, it must implement TransactionListener. This will be called on all visible UI components. The method must filter the events to figure out if a refresh is to be made. A refresh is usually done by calling refresh() on the owned model.
    * The view is typically responsible for checking that the event has the id corresponding to the view, and then delegates to the model which checks that an event that causes it to refresh has been called. This gives a combination of id+eventtype checking which properly filters what should cause a refresh.
* When filtering events, make sure to use the util classes Iterables, Events and Specifications as much as possible. The helper methods there should be all you need.

Resources and contexts on the server
************************************
The server-side REST API is implemented using resources and DCI contexts. The resources (subclasses of CommandQueryResource) are responsible for parsing the path of the URL, and maps objects into a RoleMap. Once an interaction (i.e. the last segment in a path, e.g. "/dostuff) is found the corresponding method is located in the context of the last resource. If the interaction is valid (i.e. no interaction constraint annotations fail), then the request will be parsed according to the interaction parameter type, and then invoked. The result is converted to HTML or JSON and then returned as response.

If you need to do anything related to HTTP, such as getting/setting headers, this should be done in a method on the resource that has the same name as the context method, and with no parameters. Call "invoke()" to invoke the context, and then work with the result and request/response objects.

REST representations
********************
Do not use LinkValues for non-link purposes. Instead create a suitable DTO class extending ValueComposite.

Services default configuration
******************************
Keep service default configurations in the ConfigurationAssembly.configurationWithDefaults() method to avoid overwrite situations. Since there are multiple ways and opportunities to set default configurations this will be the best way to keep track of default configurations.

Frequent problems
*****************
Configuration for a configurable service is not showing up in MBeans
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    .. important::
        Make sure the service interface and the Mixin extend the Configuration and Activatable interfaces ( must not be typed ).

        Make sure you inject the desired configuration interface into the Mixin

    .. code-block:: java

        @This
        Configuration<your configuration class> config;

    .. note::
        If your service is not part of the JMX module you need to set the service visibleIn( Visibility.layer ).