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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.signature.SignatureContext;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SummaryContext;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;

/**
 * JAVADOC
 */
@Mixins(FormDraftContext.Mixin.class)
public interface FormDraftContext
   extends Context, IndexContext<FormDraftValue>
{
   // commands
   void updatefield( FieldDTO field );

   @SubContext
   SummaryContext summary();

   @SubContext
   SignatureContext signature();

   void discard( );

   abstract class Mixin
      extends ContextMixin
      implements FormDraftContext
   {
      public FormDraftValue index()
      {

         return roleMap.get( FormDraftValue.class );
      }

      public void updatefield( FieldDTO field )
      {
         FormDraft formSubmission = roleMap.get( FormDraft.class );

         formSubmission.changeFieldValue( EntityReference.parseEntityReference( field.field().get() ), field.value().get() );
      }

      private void updateFormSubmission( ValueBuilder<FormDraftValue> builder )
      {
         FormDraftValue newFormValue = builder.newInstance();
         FormDraft formSubmission = roleMap.get( FormDraft.class );
         formSubmission.changeFormSubmission( newFormValue );

         roleMap.set( newFormValue );
      }

      public SummaryContext summary()
      {
         roleMap.set( this );
         return subContext( SummaryContext.class );
      }

      /**
       * discard form and remove case
       */
      public void discard( )
      {
         FormDraft formSubmission = roleMap.get( FormDraft.class );
         FormDrafts data = roleMap.get( FormDrafts.class );
         data.discardFormDraft( formSubmission );

         EndUserCases cases = roleMap.get( EndUserCases.class );
         cases.discardCase( roleMap.get( Case.class ) );
      }

      public SignatureContext signature()
      {
         return subContext( SignatureContext.class );
      }
   }
}