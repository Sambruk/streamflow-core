/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.surface.customers;


import org.joda.time.DateTime;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.external.ShadowCaseLinkValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCase;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCasesQueries;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( MyShadowCasesContext.Mixin.class )
public interface MyShadowCasesContext
   extends Context, IndexContext<LinksValue>

{

   abstract class Mixin
      implements MyShadowCasesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         ShadowCasesQueries shadowCasesQueries = role( ShadowCasesQueries.class );
         String contactId = role( Contactable.Data.class ).contact().get().contactId().get();

         Query<ShadowCase> myShadowCases = shadowCasesQueries.findCases( contactId );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         ValueBuilder<ShadowCaseLinkValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( ShadowCaseLinkValue.class );
         for( ShadowCase caze : myShadowCases )
         {
            valueBuilder.prototype().contactId().set( ((ShadowCase.Data)caze).contactId().get() );
            valueBuilder.prototype().content().set( ((ShadowCase.Data)caze).content().get() );
            valueBuilder.prototype().createdOn().set( caze.createdOn().get() != null ? new DateTime( caze.createdOn().get()) : null );
            valueBuilder.prototype().creationDate().set( ((ShadowCase.Data)caze).creationDate().get() );
            valueBuilder.prototype().description().set( caze.getDescription() );
            valueBuilder.prototype().externalId().set( ((ShadowCase.Data)caze).externalId().get() );
            valueBuilder.prototype().log().set( ((ShadowCase.Data)caze).log().get() );
            valueBuilder.prototype().systemName().set( ((ShadowCase.Data) caze).systemName().get() );
            valueBuilder.prototype().href().set( ((Identity)caze).identity().get() + "/" );
            valueBuilder.prototype().id().set( ((Identity)caze).identity().get() );
            valueBuilder.prototype().text().set( caze.getDescription() );

            linksBuilder.addLink( valueBuilder.newInstance() );
         }

         return linksBuilder.newLinks();
      }
   }
}
