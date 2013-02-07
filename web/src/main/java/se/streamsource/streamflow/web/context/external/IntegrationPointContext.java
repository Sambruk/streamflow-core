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
package se.streamsource.streamflow.web.context.external;


import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.api.external.ShadowCaseDTO;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCase;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCases;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCasesQueries;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoint;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( IntegrationPointContext.Mixin.class )
public interface IntegrationPointContext
   extends Context, UpdateContext<ShadowCaseDTO>
{
   public void create( ShadowCaseDTO shadowCase );

   public void remove( String externalId );

   abstract class Mixin
      implements IntegrationPointContext
   {
      public void create( ShadowCaseDTO shadowCase )
      {
         role( Organization.class ).createCase( shadowCase );
      }

      public void remove( String externalId )
      {
         ShadowCasesQueries queries = role( ShadowCasesQueries.class );

         ShadowCase caze = queries.findExternalCase( role( IntegrationPoint.class ).getDescription().toLowerCase(), externalId );
         ((ShadowCases)queries).removeCase( caze );
      }

      public void update( ShadowCaseDTO updateCase )
      {
         ShadowCasesQueries queries = role( ShadowCasesQueries.class );

         ShadowCase caze = queries.findExternalCase( role( IntegrationPoint.class ).getDescription().toLowerCase(), updateCase.externalId().get() );
         caze.updateContent( updateCase.content().get() );
         caze.updateLog( updateCase.log().get() );
      }
   }
}
