package se.streamsource.streamflow.web.application.entityexport;

import java.sql.Connection;

/**
 * Created by ruslan on 06.03.17.
 */
public class ValueExportHelper
{

   protected String name;
   protected Object value;
   protected Connection connection;

   public String help() {

      return "not_implemented;1234:separator:not_implemented;4321";
   }

   // setters

   public void setName( String name )
   {
      this.name = name;
   }

   public void setValue( Object value )
   {
      this.value = value;
   }

   public void setConnection( Connection connection )
   {
      this.connection = connection;
   }
}
