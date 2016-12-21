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
package se.streamsource.streamflow.web.context.external.integrationpoints;


import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.external.ShadowCaseDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCase;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCases;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( IntegrationPointContext.Mixin.class )
public interface IntegrationPointContext
   extends Context, IndexContext<LinksValue>
{
   public void create( ShadowCaseDTO shadowCase );

   abstract class Mixin
      implements IntegrationPointContext
   {
      @Structure
      Module module;

      public void create( ShadowCaseDTO shadowCase )
      {
         role( Organization.class ).createCase( shadowCase );
      }

      /**
       *  This index has to return externalId as link id, not UUID!!
       * @return externalId - the id given of the external system.
       */
      public LinksValue index()
      {
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         for( ShadowCase caze : role( ShadowCases.class ).findExternalCases( role( IntegrationPoint.class ).getDescription().toLowerCase() ) )
         {
            linksBuilder.addLink( ((ShadowCase.Data)caze).externalId().get(), ((ShadowCase.Data)caze).externalId().get() );
         }
         return linksBuilder.newLinks();
      }
   }
}
