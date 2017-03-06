package se.streamsource.streamflow.web.application.entityexport;

import se.streamsource.streamflow.web.application.entityexport.valueexport.CaseLogEntryValueExportHelper;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;

import java.sql.Connection;

/**
 * Created by ruslan on 06.03.17.
 */
public abstract class ValueExportHelper
{

   protected String name;
   protected Object value;
   protected Connection connection;

   public abstract String help();

   public static ValueExportHelper fromClass( Class<?> clazz )
   {
      if ( clazz.equals( CaseLogEntryValue.class ) )
      {
         return new CaseLogEntryValueExportHelper();
      }

      return null;
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
