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
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
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
   void nextpage( IntegerDTO page );

   @HasPreviousPage
   void previouspage(IntegerDTO page);

   void updatefield( FieldDTO field );

   @SubContext
   @HasNextPage(false)
   SummaryContext summary();

   // parameter only needed to make the command work
   void discard( IntegerDTO dummy );

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

      public void nextpage( IntegerDTO page )
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( 1 );

         if ( builder != null )
            updateFormSubmission( builder );
      }

      public void previouspage(IntegerDTO page)
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( -1 );

         if ( builder != null )
            updateFormSubmission( builder );
      }

      /*public void next( Representation rep)
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
      } */

      private ValueBuilder<FormSubmissionValue> incrementPage( int increment )
      {
         ValueBuilder<FormSubmissionValue> builder = context.get( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();
         int page = builder.prototype().currentPage().get() + increment;
         int pages = builder.prototype().pages().get().size();

         if ( page < pages && page >= 0 )
         {
            builder.prototype().currentPage().set( page );
            return builder;
         }
         return null;
      }

      /*private void updateFieldValues( Representation rep )
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
      } */

      public void updatefield( FieldDTO field )
      {
         FormSubmission formSubmission = context.get( FormSubmission.class );

         formSubmission.changeFieldValue( EntityReference.parseEntityReference( field.field().get() ), field.value().get() );

         /*
         FormSubmissionValue formValue = context.get( FormSubmissionValue.class );

         PageSubmissionValue value = formValue.pages().get().get( formValue.currentPage().get() );

         ValueBuilder<FieldSubmissionValue> builder = null;

         for (FieldSubmissionValue fieldSubmissionValue : value.fields().get())
         {
            if ( fieldSubmissionValue.field().get().field().get().identity().equals( field.field().get() ) )
            {
               builder = module.valueBuilderFactory().newValueBuilder( FieldSubmissionValue.class ).withPrototype( fieldSubmissionValue );
               builder.prototype().value().set( field.value().get() );
            }
         }

         if ( builder == null )
            return;

         FieldSubmissionValue newFieldValue = builder.newInstance();

         ValueBuilder<FormSubmissionValue> formBuilder = getFormSubmissionValueBuilder();

         PageSubmissionValue pageValue = formBuilder.prototype().pages().get().remove( formBuilder.prototype().currentPage().get().intValue() );

         for (FieldSubmissionValue fieldValue : pageValue.fields().get())
         {
            if ( fieldValue.field().get().field().get().equals( newFieldValue.field().get().field().get() ) )
            {
               fieldValue.value().set( newFieldValue.value().get() );
               formBuilder.prototype().pages().get().add( formBuilder.prototype().currentPage().get(), pageValue );
               updateFormSubmission( formBuilder );
               return;
            }
         }
         */
      }

      /*private ValueBuilder<FormSubmissionValue> getFormSubmissionValueBuilder()
      {
         FormSubmissionValue value = context.get( FormSubmissionValue.class );
         return value.buildWith();
      } */

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

      public void discard( IntegerDTO dummy )
      {
         // todo
      }
   }
}