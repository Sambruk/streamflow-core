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
package se.streamsource.streamflow.web.domain.structure.external;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.web.domain.Removable;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Contains queries for ShadowCases
 */
@Mixins(ShadowCasesQueries.Mixin.class)
public interface ShadowCasesQueries
{
   Query<ShadowCase> findCases( String contactId );

   ShadowCase findExternalCase( String systemName, String externalId );

   Query<ShadowCase> findExternalCases( String externalSystem );

   abstract class Mixin
      implements ShadowCasesQueries
   {
      @Structure
      Module module;

      public Query<ShadowCase> findCases( String contactId )
      {
         return module.queryBuilderFactory().newQueryBuilder( ShadowCase.class )
               .where( and(
                     eq( templateFor( Removable.Data.class ).removed(), false ),
                     eq( templateFor( ShadowCase.Data.class ).contactId(), contactId )))
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      }

      public ShadowCase findExternalCase( String systemName, String externalId )
      {
         return module.queryBuilderFactory().newQueryBuilder( ShadowCase.class )
               .where( and(
                     eq( templateFor( Removable.Data.class ).removed(), false ),
                     eq( templateFor( ShadowCase.Data.class ).systemName(), systemName ),
                     eq( templateFor( ShadowCase.Data.class ).externalId(), externalId )))
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() )
               .find();
      }

      public Query<ShadowCase> findExternalCases( String systemName )
      {
         return module.queryBuilderFactory().newQueryBuilder( ShadowCase.class )
               .where( and(
                     eq( templateFor( Removable.Data.class ).removed(), false ),
                     eq( templateFor( ShadowCase.Data.class ).systemName(), systemName )))
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      }
   }
}
