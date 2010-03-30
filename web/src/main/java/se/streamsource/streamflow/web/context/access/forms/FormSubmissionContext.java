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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;

import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
@Mixins(FormSubmissionContext.Mixin.class)
public interface FormSubmissionContext
   extends Context, IndexContext<PageSubmissionValue>
{
   // queries
   LinksValue pages();

   // commands
   @HasNextPage
   void next(Representation rep);

   @HasPreviousPage
   void previous(Representation rep);

   void updatefield( FieldSubmissionValue newFieldValue );

   @SubContext
   @HasNextPage(false)
   FormSummaryContext summary();

   void discard();

   abstract class Mixin
      extends ContextMixin
      implements FormSubmissionContext
   {
      public PageSubmissionValue index()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );

         return value.pages().get().get( value.currentPage().get() );
      }

      public LinksValue pages()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         int pageIndex = 0;
         for (PageSubmissionValue pageValue : value.pages().get())
         {
            builder.path( "../" );
            builder.command( "gotopage" );
            builder.addLink( pageValue.title().get(), ""+pageIndex );
            pageIndex++;
         }

         return builder.newLinks();
      }

      public void next( Representation rep)
      {
         Form form = new Form( rep );

         Set<Map.Entry<String, String>> entries = form.getValuesMap().entrySet();

         FormSubmission submission = context.role( FormSubmission.class );

         for (Map.Entry<String, String> entry : entries)
         {
            submission.changeFieldValue( EntityReference.parseEntityReference(entry.getKey() ), entry.getValue());
         }

         ValueBuilder<FormSubmissionValue> builder = context.role( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();

         int pages = builder.prototype().pages().get().size();
         int next = builder.prototype().currentPage().get() + 1;
         if ( next < pages )
         {
            builder.prototype().currentPage().set( next );
         }

         updateFormSubmission( builder );
      }

      public void previous(Representation rep)
      {
         Form form = new Form( rep );

         Set<Map.Entry<String, String>> entries = form.getValuesMap().entrySet();

         FormSubmission submission = context.role( FormSubmission.class );

         for (Map.Entry<String, String> entry : entries)
         {
            submission.changeFieldValue( EntityReference.parseEntityReference(entry.getKey() ), entry.getValue());
         }

         ValueBuilder<FormSubmissionValue> builder = context.role( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();

         int previous = builder.prototype().currentPage().get() - 1;
         if ( previous >= 0 )
         {
            builder.prototype().currentPage().set( previous );
         }

         updateFormSubmission( builder );
      }

      public void updatefield( FieldSubmissionValue newFieldValue )
      {
         ValueBuilder<FormSubmissionValue> builder = getFormSubmissionValueBuilder();


         PageSubmissionValue pageValue = builder.prototype().pages().get().remove( builder.prototype().currentPage().get().intValue() );

         for (FieldSubmissionValue value : pageValue.fields().get())
         {
            if ( value.field().get().field().get().equals( newFieldValue.field().get().field().get() ) )
            {
               value.value().set( newFieldValue.value().get() );
               builder.prototype().pages().get().add( builder.prototype().currentPage().get(), pageValue );
               updateFormSubmission( builder );
               return;
            }
         }
      }

      private ValueBuilder<FormSubmissionValue> getFormSubmissionValueBuilder()
      {
         FormSubmissionValue value = context.role( FormSubmissionValue.class );
         return value.buildWith();
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
         context.playRoles( this );
         return subContext( FormSummaryContext.class );
      }

      public void discard()
      {
         // delete formSubmission
      }
   }
}