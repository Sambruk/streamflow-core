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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.signature;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.domain.form.RequiredSignatureValue;
import se.streamsource.streamflow.domain.form.RequiredSignaturesValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.UpdateCaseCountFormSummaryConcern;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountFormSummaryConcern.class)
@Mixins(SignatureContext.Mixin.class)
public interface SignatureContext
   extends Context, IndexContext<FormDraftValue>
{
   void submit();
   
   void submitandsend();

   LinksValue providers();

   abstract class Mixin
      extends ContextMixin
      implements SignatureContext
   {
      public FormDraftValue index()
      {
         return roleMap.get( FormDraftValue.class );
      }

      public void submit()
      {
         EndUserCases userCases = roleMap.get( EndUserCases.class );
         AnonymousEndUser user = roleMap.get( AnonymousEndUser.class );
         FormDraft formSubmission = roleMap.get( FormDraft.class );
         Case aCase = roleMap.get( Case.class );

         userCases.submitForm( aCase, formSubmission , user );
      }

      public void submitandsend()
      {
         EndUserCases userCases = roleMap.get( EndUserCases.class );
         AnonymousEndUser user = roleMap.get( AnonymousEndUser.class );
         FormDraft formSubmission = roleMap.get( FormDraft.class );
         Case aCase = roleMap.get( Case.class );

         userCases.submitFormAndSendCase( aCase, formSubmission, user );
      }

      public RequiredSignaturesValue signatures()
      {
         FormDraftValue form = roleMap.get( FormDraftValue.class );

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

      public LinksValue providers()
      {
         //var url = "https://175.145.48.194:8443/eid/sign/";
         return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
   }
}