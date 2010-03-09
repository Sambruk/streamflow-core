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
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;

/**
 * JAVADOC
 */
@Mixins(FormPageContext.Mixin.class)
public interface FormPageContext
   extends Context, IndexContext<SubmittedPageValue>
{
   // commands
   @HasNextPage(true)
   void next( SubmittedPageValue newValue );

   @HasPreviousPage
   void previous( SubmittedPageValue newValue );

   @SubContext
   @HasNextPage(false)
   FormSummaryContext summary();

   void discard();

   abstract class Mixin
      extends ContextMixin
      implements FormPageContext
   {

      public SubmittedPageValue index()
      {
         return context.role( SubmittedPageValue.class ); 
      }

      public void next( SubmittedPageValue newValue )
      {
         SubmittedPageValue value = context.role( SubmittedPageValue.class );
         FormSubmissionValue formSubmissionValue = context.role( FormSubmissionValue.class );

         int index = formSubmissionValue.pages().get().indexOf( value );
         ValueBuilder<FormSubmissionValue> builder = module.valueBuilderFactory().newValueBuilder( FormSubmissionValue.class ).withPrototype( formSubmissionValue );
         builder.prototype().pages().get().remove( index );
         builder.prototype().pages().get().add( index, newValue );
         FormSubmission formSubmission = context.role( FormSubmission.class );
         formSubmission.changeFormSubmission( builder.newInstance() );

         SubmittedPageValue next = formSubmissionValue.pages().get().get( index + 1 );

         context.playRoles( next );

         // redirect...
      }

      public void previous( SubmittedPageValue newValue )
      {
         SubmittedPageValue value = context.role( SubmittedPageValue.class );
         FormSubmissionValue formSubmissionValue = context.role( FormSubmissionValue.class );

         int index = formSubmissionValue.pages().get().indexOf( value );
         ValueBuilder<FormSubmissionValue> builder = module.valueBuilderFactory().newValueBuilder( FormSubmissionValue.class ).withPrototype( formSubmissionValue );
         builder.prototype().pages().get().remove( index );
         builder.prototype().pages().get().add( index, newValue );
         FormSubmission formSubmission = context.role( FormSubmission.class );
         formSubmission.changeFormSubmission( builder.newInstance() );

         SubmittedPageValue next = formSubmissionValue.pages().get().get( index - 1 );

         context.playRoles( next );

         // redirect...
      }

      public FormSummaryContext summary()
      {

         return subContext( FormSummaryContext.class );
      }

      public void discard()
      {
         
      }
   }
}