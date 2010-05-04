/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.access.accesspoints;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.context.access.accesspoints.endusers.EndUsersContext;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;

/**
 * JAVADOC
 */
@Mixins(ProxyUserContext.Mixin.class)
public interface ProxyUserContext
   extends Interactions, IndexInteraction<LinksValue>
{
   @SubContext
   EndUsersContext endusers(); 

   abstract class Mixin
      extends InteractionsMixin
      implements ProxyUserContext
   {

      public LinksValue index()
      {
         DraftsQueries draftsQueries = context.get( DraftsQueries.class );
         Describable describable = context.get( Describable.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( draftsQueries.drafts().newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));
         linksBuilder.addTitle( describable.getDescription() );

         return linksBuilder.newLinks();
      }

      public EndUsersContext endusers()
      {
         return subContext( EndUsersContext.class );
      }
   }
}