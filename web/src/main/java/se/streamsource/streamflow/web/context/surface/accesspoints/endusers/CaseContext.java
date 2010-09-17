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

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.resource.caze.EndUserCaseDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.FormDraftsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.requiredforms.RequiredFormsContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.submittedforms.SubmittedFormsContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountCaseConcern.class)
@Mixins(CaseContext.Mixin.class)
public interface CaseContext
      extends IndexContext<EndUserCaseDTO>, Context
{
   // commands
   void sendtoproject();

   @SubContext
   SubmittedFormsContext submittedforms();

   @SubContext
   RequiredFormsContext requiredforms();

   @SubContext
   FormDraftsContext formdrafts();

   abstract class Mixin
         extends ContextMixin
         implements CaseContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public EndUserCaseDTO index()
      {
         Case aCase = roleMap.get( Case.class );

         ValueBuilder<EndUserCaseDTO> builder = vbf.newValueBuilder( EndUserCaseDTO.class );
         builder.prototype().description().set( aCase.getDescription() );
         AccessPointSettings.Data accessPoint = roleMap.get( AccessPointSettings.Data.class );
         Labelable.Data labelsData = roleMap.get( Labelable.Data.class );

         builder.prototype().project().set( accessPoint.project().get().getDescription() );
         builder.prototype().caseType().set( accessPoint.caseType().get().getDescription() );


         for (Label label : labelsData.labels())
         {
            builder.prototype().labels().get().add( label.getDescription() );
         }
         return builder.newInstance();
      }

      public void sendtoproject()
      {
         CaseEntity aCase = roleMap.get( CaseEntity.class);
         EndUserCases cases = roleMap.get( EndUserCases.class );
         cases.sendToFunction( aCase );
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