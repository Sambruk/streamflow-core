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

package se.streamsource.streamflow.web.context.access.accesspoints.endusers;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
import se.streamsource.streamflow.web.context.access.accesspoints.endusers.CaseContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(EndUserContext.Mixin.class)
public interface EndUserContext
   extends SubContexts<CaseContext>, Interactions, IndexInteraction<LinksValue>
{
   // command
   void createcase( StringValue description );

   abstract class Mixin
      extends InteractionsMixin
      implements EndUserContext
   {

      public LinksValue index()
      {
         DraftsQueries draftsQueries = context.get( DraftsQueries.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( draftsQueries.drafts().newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));

         return linksBuilder.newLinks();
      }

      public void createcase( StringValue description )
      {
         Drafts drafts = context.get( Drafts.class );
         AccessPoint.Data data = context.get( AccessPoint.Data.class );
         AnonymousEndUser creator = context.get( AnonymousEndUser.class );
         CaseEntity caseEntity = drafts.createDraft();
         caseEntity.changeDescription( description.string().get() );
         caseEntity.changeCaseType( data.caseType().get() );
         caseEntity.createdBy().set( creator );

         for (Label label : data.labels())
         {
            caseEntity.addLabel( label );
         }
      }

      public CaseContext context( String id)
      {
         CaseEntity caseEntity = module.unitOfWorkFactory().currentUnitOfWork().get( CaseEntity.class, id );

         context.set( caseEntity );
         return subContext( CaseContext.class );
      }
   }
}