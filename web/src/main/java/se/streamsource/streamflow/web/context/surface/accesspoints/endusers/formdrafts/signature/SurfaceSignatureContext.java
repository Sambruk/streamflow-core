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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.signature;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignaturesValue;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.UpdateCaseCountFormSummaryConcern;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountFormSummaryConcern.class)
@Mixins(SurfaceSignatureContext.Mixin.class)
public interface SurfaceSignatureContext
   extends Context, IndexContext<FormDraftDTO>
{
   void submit();
   
   void submitandsend();

   abstract class Mixin
      implements SurfaceSignatureContext
   {
      @Structure
      Module module;

      public FormDraftDTO index()
      {
         return role( FormDraftDTO.class );
      }

      public void submit()
      {
         EndUserCases userCases = role( EndUserCases.class );
         EndUser user = role( EndUser.class );
         FormDraft formSubmission = role( FormDraft.class );
         Case aCase = role( Case.class );

         userCases.submitForm( aCase, formSubmission , user );
      }

      public void submitandsend()
      {
         EndUserCases userCases = role( EndUserCases.class );
         EndUser user = role( EndUser.class );
         FormDraft formSubmission = role( FormDraft.class );
         Case aCase = role( Case.class );

         userCases.submitFormAndSendCase( aCase, formSubmission, user );
      }

      public RequiredSignaturesValue signatures()
      {
         FormDraftDTO form = role( FormDraftDTO.class );

         RequiredSignatures.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, form.form().get().identity() );

         ValueBuilder<RequiredSignaturesValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( RequiredSignaturesValue.class );
         valueBuilder.prototype().signatures().get();
         ValueBuilder<RequiredSignatureValue> builder = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );

         for (RequiredSignatureValue signature :  data.requiredSignatures().get())
         {
            builder.prototype().name().set( signature.name().get() );
            builder.prototype().description().set( signature.description().get() );

            valueBuilder.prototype().signatures().get().add( builder.newInstance() );
         }
         return valueBuilder.newInstance();
      }

   }
}