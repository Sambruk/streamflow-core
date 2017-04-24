/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.entityexport;

/**
 * Uses to wrap string value with enum for appropriate value.
 * <p/>
 * Example of usage:
 * <code>
 *    <pre>
 *    final DataSourceConfiguration dsConfig =...;
 *    final DbVendor dbVendor = DbVendor.from( dsConfig.dbVendor().get() );
 *    </pre>
 * </code>
 * @see se.streamsource.infrastructure.database.DataSourceConfiguration
 */
public enum DbVendor
{
   mysql, //default
   mssql,
   oracle;

   public static DbVendor from( String dbVendor )
   {
      if ( dbVendor != null )
      {
         for ( DbVendor vendor : DbVendor.values() )
         {
            if ( dbVendor.toLowerCase().equals( vendor.name() ) )
            {
               return vendor;
            }
         }
      }

      return mysql;
   }
}
