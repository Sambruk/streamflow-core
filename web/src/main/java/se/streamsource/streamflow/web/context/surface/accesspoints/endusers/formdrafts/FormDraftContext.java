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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SummaryContext;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;

import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
@Mixins(FormDraftContext.Mixin.class)
public interface FormDraftContext
   extends Interactions, IndexInteraction<PageSubmissionValue>
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
   SummaryContext summary();

   void discard();

   abstract class Mixin
      extends InteractionsMixin
      implements FormDraftContext
   {
      public PageSubmissionValue index()
      {
         FormSubmissionValue value = context.get( FormSubmissionValue.class );

         return value.pages().get().get( value.currentPage().get() );
      }

      public LinksValue pages()
      {
         FormSubmissionValue value = context.get( FormSubmissionValue.class );

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
         updateFieldValues( rep );

         ValueBuilder<FormSubmissionValue> builder = incrementPage( 1 );

         updateFormSubmission( builder );
      }

      public void previous(Representation rep)
      {
         updateFieldValues( rep );

         ValueBuilder<FormSubmissionValue> builder = incrementPage( -1 );

         updateFormSubmission( builder );
      }

      private ValueBuilder<FormSubmissionValue> incrementPage( int increment )
      {
         ValueBuilder<FormSubmissionValue> builder = context.get( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();
         int page = builder.prototype().currentPage().get() + increment;
         int pages = builder.prototype().pages().get().size();

         if ( page < pages && page >= 0 )
         {
            builder.prototype().currentPage().set( page );
         }
         return builder;
      }

      private void updateFieldValues( Representation rep )
      {
         Form form = new Form( rep );

         Set<Map.Entry<String, String>> entries = form.getValuesMap().entrySet();

         FormSubmission submission = context.get( FormSubmission.class );

         for (Map.Entry<String, String> entry : entries)
         {
            String value = entry.getValue();
            if ( value != null)
            {
               submission.changeFieldValue( EntityReference.parseEntityReference(entry.getKey() ), value );
            }
         }
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
         FormSubmissionValue value = context.get( FormSubmissionValue.class );
         return value.buildWith();
      }

      private void updateFormSubmission( ValueBuilder<FormSubmissionValue> builder )
      {
         FormSubmissionValue newFormValue = builder.newInstance();
         FormSubmission formSubmission = context.get( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );

         context.set( newFormValue );
      }

      public SummaryContext summary()
      {
         context.set( this );
         return subContext( SummaryContext.class );
      }

      public void discard()
      {
         // delete formSubmission
      }
   }
}