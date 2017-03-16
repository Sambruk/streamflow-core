package se.streamsource.streamflow.web.application.entityexport;

/**
 * Created by ruslan on 16.03.17.
 */
public enum DbVendor
{
   mysql, //default
   mssql,
   oracle;

   public static DbVendor from( String dbVendor )
   {
      if ( dbVendor == null )
      {
         return mysql;
      }

      for ( DbVendor vendor : DbVendor.values() )
      {
         if ( dbVendor.toLowerCase().equals( vendor.toString() ) )
         {
            return vendor;
         }
      }

      return mysql;
   }
}
