/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.representation.Representation;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;

/**
 * JAVADOC
 */
@Mixins(FormSummaryContext.Mixin.class)
public interface FormSummaryContext
   extends Context, IndexContext<FormSubmissionValue>
{

   void submit();

   void gotopage( IntegerDTO page );

   abstract class Mixin
      extends ContextMixin
      implements FormSummaryContext
   {
      public FormSubmissionValue index()
      {
         return context.role( FormSubmissionValue.class );
      }

      public void submit()
      {
         SubmittedForms submittedForms = context.role( SubmittedForms.class );
         FormSubmission formSubmission = context.role( FormSubmission.class );
         ProxyUser user = context.role( ProxyUser.class );
         submittedForms.submitForm( formSubmission, user );
      }

      public void gotopage( IntegerDTO page)
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );
         ValueBuilder<FormSubmissionValue> valueBuilder = value.buildWith();

         valueBuilder.prototype().currentPage().set( page.integer().get() );

         FormSubmissionValue newFormValue = valueBuilder.newInstance();
         FormSubmission formSubmission = context.role( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );
      }
   }
}