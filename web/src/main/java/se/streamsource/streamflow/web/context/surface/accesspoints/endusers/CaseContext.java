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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.caze.ProxyUserCaseDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.FormDraftsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms.RequiredFormsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SubmittedFormsContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;

/**
 * JAVADOC
 */
@Mixins(CaseContext.Mixin.class)
public interface CaseContext
      extends IndexInteraction<ProxyUserCaseDTO>, Interactions
{
   // commands
   void changedescription( StringValue newDescription );
   void sendtofunction();

   @SubContext
   SubmittedFormsContext submittedforms();

   @SubContext
   RequiredFormsContext requiredforms();

   @SubContext
   FormDraftsContext formdrafts();

   abstract class Mixin
         extends InteractionsMixin
         implements CaseContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public ProxyUserCaseDTO index()
      {
         Case aCase = context.get( Case.class );

         ValueBuilder<ProxyUserCaseDTO> builder = vbf.newValueBuilder( ProxyUserCaseDTO.class );
         builder.prototype().description().set( aCase.getDescription() );
         AccessPoint.Data accessPoint = context.get( AccessPoint.Data.class );
         Labelable.Data labelsData = context.get( Labelable.Data.class );

         builder.prototype().project().set( accessPoint.project().get().getDescription() );
         builder.prototype().caseType().set( accessPoint.caseType().get().getDescription() );


         for (Label label : labelsData.labels())
         {
            builder.prototype().labels().get().add( label.getDescription() );
         }
         return builder.newInstance();
      }

      public void changedescription( StringValue newDescription )
      {
         Describable describable = context.get( Describable.class );
         describable.changeDescription( newDescription.string().get() );
      }

      public void sendtofunction()
      {
         CaseEntity aCase = context.get( CaseEntity.class);
         ProjectEntity project = (ProjectEntity) context.get( AccessPoint.Data.class ).project().get();

         aCase.unassign();
         aCase.sendTo( project );
         aCase.open();
      }

      public SubmittedFormsContext submittedforms()
      {
         return subContext( SubmittedFormsContext.class );
      }

      public RequiredFormsContext requiredforms()
      {
         
         return subContext( RequiredFormsContext.class );
      }

      public FormDraftsContext formdrafts()
      {
         return subContext( FormDraftsContext.class );
      }
   }
}