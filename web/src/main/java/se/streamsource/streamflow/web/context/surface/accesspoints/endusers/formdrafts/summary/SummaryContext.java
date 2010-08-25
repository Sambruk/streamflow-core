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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(SummaryContext.Mixin.class)
public interface SummaryContext
   extends Context, IndexContext<FormSubmissionValue>
{

   void submit();
   
   void submitandsend();

   void gotopage( IntegerDTO page );

   abstract class Mixin
      extends ContextMixin
      implements SummaryContext
   {
      public FormSubmissionValue index()
      {
         return roleMap.get( FormSubmissionValue.class );
      }

      public void submit()
      {
         EndUserCases userCases = roleMap.get( EndUserCases.class );
         AnonymousEndUser user = roleMap.get( AnonymousEndUser.class );
         FormSubmission formSubmission = roleMap.get( FormSubmission.class );
         Case aCase = roleMap.get( Case.class );

         userCases.submitForm( aCase, formSubmission , user );
      }

      public void submitandsend()
      {
         EndUserCases userCases = roleMap.get( EndUserCases.class );
         AnonymousEndUser user = roleMap.get( AnonymousEndUser.class );
         FormSubmission formSubmission = roleMap.get( FormSubmission.class );
         Case aCase = roleMap.get( Case.class );

         userCases.submitFormAndSendCase( aCase, formSubmission, user );
      }

      public void gotopage( IntegerDTO page)
      {
         FormSubmissionValue value = roleMap.get( FormSubmissionValue.class );
         ValueBuilder<FormSubmissionValue> valueBuilder = value.buildWith();

         valueBuilder.prototype().currentPage().set( page.integer().get() );

         FormSubmissionValue newFormValue = valueBuilder.newInstance();
         FormSubmission formSubmission = roleMap.get( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );
      }
   }
}