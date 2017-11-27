Admin Tool
##########

Each Streamflow Server can be reached through the REST API when logging in as administrator you have access to the automatic generated REST API.On the first page behind the URL http://<streamflowserver>:<port>/streamflow there is a link to the built-in Streamflow Admin Tool.

    .. note::
        In a production environment only use tab Console not any of the others or you might hang the server!

Console
=======
The console is used for queries or data manipulations in the jdbm data store.
It is a Bean Shell like console capable of executing java code. Check the provided Help page for guidance.

FindRemovedFormEntity
---------------------

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