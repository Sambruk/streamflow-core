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

package se.streamsource.streamflow.web.context.access.accesspoints.endusers.formdrafts.summary;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(SummaryContext.Mixin.class)
public interface SummaryContext
   extends Interactions, IndexInteraction<FormSubmissionValue>
{

   void submit();

   void gotopage( IntegerDTO page );

   abstract class Mixin
      extends InteractionsMixin
      implements SummaryContext
   {
      public FormSubmissionValue index()
      {
         return context.get( FormSubmissionValue.class );
      }

      public void submit()
      {
         SubmittedForms submittedForms = context.get( SubmittedForms.class );
         FormSubmission formSubmission = context.get( FormSubmission.class );
         AnonymousEndUser user = context.get( AnonymousEndUser.class );
         submittedForms.submitForm( formSubmission, user );
      }

      public void gotopage( IntegerDTO page)
      {
         FormSubmissionValue value = context.get( FormSubmissionValue.class );
         ValueBuilder<FormSubmissionValue> valueBuilder = value.buildWith();

         valueBuilder.prototype().currentPage().set( page.integer().get() );

         FormSubmissionValue newFormValue = valueBuilder.newInstance();
         FormSubmission formSubmission = context.get( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );
      }
   }
}