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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * TODO
 */
@Concerns(EmailCases.EmailCaseCountCacheConcern.class)
@Mixins(EmailCases.Mixin.class)
public interface EmailCases
{
   CaseEntity createCase( Drafts endUser );

   void sendTo( Case caze );

   class Mixin
      implements EmailCases
   {
      @This
      Labelable.Data labelable;

      @This
      AccessPointSettings.Data accesspoint;

      public CaseEntity createCase( Drafts endUser )
      {
         CaseEntity caseEntity = endUser.createDraft();
         if( accesspoint.caseType().get() != null )
         {
            caseEntity.changeDescription( accesspoint.caseType().get().getDescription() );
            caseEntity.changeCaseType( accesspoint.caseType().get() );
         }
         caseEntity.accesspoint().set( (EmailAccessPoint) accesspoint );

         for (Label label : labelable.labels())
         {
            caseEntity.addLabel( label );
         }

         return caseEntity;
      }

      public void sendTo( Case caze )
      {
         CaseEntity caseEntity = (CaseEntity) caze;
         if (caseEntity.isAssigned())
            caseEntity.unassign();
         caseEntity.changeOwner( accesspoint.project().get() );
         caseEntity.open();
      }
   }

   abstract class EmailCaseCountCacheConcern
      extends ConcernOf<EmailCases>
      implements EmailCases
   {
      @This
      AccessPointSettings.Data accesspoint;
      
      Caching caching;

      public void init(@Optional @Service CachingService cache)
      {
         caching = new Caching(cache, Caches.CASECOUNTS);
      }

      public void sendTo( Case caze )
      {
         next.sendTo( caze );

         // Update inbox cache on receiving end
         caching.addToCaseCountCache( ((ProjectEntity)accesspoint.project().get()).identity().get(), 1 );
         caching.addToUnreadCache( ((ProjectEntity)accesspoint.project().get()).identity().get(), 1 );
      }
   }
}
