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
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedPageValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
@Mixins(FormSubmissionContext.Mixin.class)
public interface FormSubmissionContext
   extends Context, IndexContext<SubmittedPageValue>
{
   // queries
   LinksValue pages();

   // commands
   @HasNextPage(true)
   void next();

   @HasPreviousPage
   void previous();

   void updatepage( SubmittedPageValue newPageValue );

   void gotopage( IntegerDTO page );

   @SubContext
   @HasNextPage(false)
   FormSummaryContext summary();

   void discard();

   abstract class Mixin
      extends ContextMixin
      implements FormSubmissionContext
   {
      public SubmittedPageValue index()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );

         return value.pages().get().get( value.currentPage().get() );
      }

      public LinksValue pages()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         int pageIndex = 0;
         for (SubmittedPageValue pageValue : value.pages().get())
         {
            builder.path( "../" );
            builder.command( "gotopage" );
            builder.addLink( pageValue.title().get(), ""+pageIndex );
            pageIndex++;
         }

         return builder.newLinks();
      }

      public void next()
      {
         ValueBuilder<FormSubmissionValue> builder = getFormSubmissionValueBuilder();

         builder.prototype().currentPage().set( builder.prototype().currentPage().get() + 1 );

         updateFormSubmission( builder );
      }

      public void previous()
      {
         ValueBuilder<FormSubmissionValue> builder = getFormSubmissionValueBuilder();

         builder.prototype().currentPage().set( builder.prototype().currentPage().get() - 1 );

         updateFormSubmission( builder );
      }

      public void updatepage( SubmittedPageValue newPageValue )
      {
         ValueBuilder<FormSubmissionValue> builder = getFormSubmissionValueBuilder();

         builder.prototype().pages().get().remove( builder.prototype().currentPage().get().intValue() );
         builder.prototype().pages().get().add( builder.prototype().currentPage().get(), newPageValue );

         updateFormSubmission( builder );
      }

      private ValueBuilder<FormSubmissionValue> getFormSubmissionValueBuilder()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );
         return module.valueBuilderFactory().newValueBuilder( FormSubmissionValue.class ).withPrototype( value );
      }

      private void updateFormSubmission( ValueBuilder<FormSubmissionValue> builder )
      {
         FormSubmissionValue newFormValue = builder.newInstance();
         FormSubmission formSubmission = context.role( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );

         context.playRoles( newFormValue );
      }

      public FormSummaryContext summary()
      {
         return subContext( FormSummaryContext.class );
      }

      public void discard()
      {
         // delete formSubmission
      }

      public void gotopage( IntegerDTO page )
      {
         ValueBuilder<FormSubmissionValue> valueBuilder = getFormSubmissionValueBuilder();

         valueBuilder.prototype().currentPage().set( page.integer().get() );

         updateFormSubmission( valueBuilder );
      }
   }
}