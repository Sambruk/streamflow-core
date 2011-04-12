/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.label.*;

/**
 * TODO
 */
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
         caseEntity.changeDescription( accesspoint.caseType().get().getDescription() );
         caseEntity.changeCaseType( accesspoint.caseType().get() );

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
}
