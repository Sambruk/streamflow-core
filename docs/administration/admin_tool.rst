Admin Tool
##########

Each Streamflow Server can be reached through the REST API when logging in as administrator you have access to the automatic generated REST API.On the first page behind the URL http://<streamflowserver>:<port>/streamflow there is a link to the built-in Streamflow Admin Tool.

    .. note::
        In a production environment only use tab Console not any of the others or you might hang the server!

Console
=======
The console is used for queries or data manipulations in the jdbm data store.
It is a Bean Shell like console capable of executing java code. Check the provided Help page for guidance.

Can be accesed at  http://<server>/streamflow/admin/tools/console

Console help
============

The admin console allows you to perform BeanShell scripts inside the Streamflow server. Enter the script in the textarea and click "Run script" to execute it. The "Result" part will contain all output from "print" commands in the script. The "Log" part will contain all the log events caused by the execution of the script.
For more information about **BeanShell**, see their website.
Predefined variables

The following variables and commands are already bound in the scripting environment:

Variables
"""""""""
    * ``qi4j`` -  The Qi4j runtime
    * ``services`` - A services finder in the web layer
    * ``uow`` - The UnitOfWork for the script execution
    * ``query`` - A QueryBuilderFactory in the web layer

Commands
""""""""
    * ``methods(object)`` - list methods of object
    * ``state(entity)`` - list state of EntityComposite

Sample scripts
==============

Here are some sample scripts for common tasks.

#. Get user

    .. code-block:: java

        import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
        admin = uow.get(UserEntity.class, "administrator");
        state(admin); // Print state of admin user

#. Find user

    .. code-block:: java

        import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
        import org.qi4j.api.query.QueryExpressions;
        template = QueryExpressions.templateFor(UserEntity.class);
        admin = query.newQueryBuilder(UserEntity.class).where(QueryExpressions.eq(template.identity(), "administrator")).newQuery(uow).find();
        print(admin);

#. Delete user

    .. code-block:: java

        import se.streamsource.streamflow.web.domain.structure.user.User;
        entity = uow.get(User.class,"username");
        uow.remove(entity);

    .. attention::
        Any operation that alters the content of the entity store demands the ElasticSearch and Solr index to be reindexed.
        To do so start jconsole and connect to the JMXConnector and trigger a reindex in Streamflow/Manager.

#. Find removed form entity

    .. code-block:: java

        import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
        import se.streamsource.streamflow.web.domain.Removable$Data;
        import se.streamsource.streamflow.web.domain.Describable$Data;
        import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable$Data;

        import org.qi4j.api.query.QueryExpressions;

        forms = query.newQueryBuilder(FormEntity.class)
        .where( QueryExpressions.matches( QueryExpressions.templateFor( Describable$Data.class).description(), "" ) )
        //.where( QueryExpressions.eq( QueryExpressions.templateFor( Removable$Data.class ).removed(), true) )
        .newQuery(uow);

        for( FormEntity form : forms.iterator() ){
            print( form.getDescription() + "    -->   Borttagen: " + ((Removable$Data)form).removed().get() + "    -->    Ägd av: " + ((Describable$Data) ((Ownable$Data)form).owner().get()).description()  );
        }