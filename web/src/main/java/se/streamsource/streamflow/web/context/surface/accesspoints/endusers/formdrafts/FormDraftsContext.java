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
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.EndUserFormDraftValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.EndUserFormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(FormDraftsContext.Mixin.class)
public interface FormDraftsContext
   extends Interactions, IndexInteraction<LinksValue>, SubContexts<FormDraftContext>
{
   FormDraftContext context( String id );

   abstract class Mixin
      extends InteractionsMixin
      implements FormDraftsContext
   {

      public LinksValue index()
      {
         SelectedForms.Data forms = context.get( SelectedForms.Data.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         for (Form form : forms.selectedForms())
         {
            builder.addLink( form.getDescription(), EntityReference.getEntityReference( form ));
         }
         return builder.newLinks();
      }

      public FormDraftContext context( String id )
      {
         EndUserFormSubmissions formSubmissions = context.get( EndUserFormSubmissions.class );
         AnonymousEndUser endUser = context.get( AnonymousEndUser.class );
         Form form = module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, id );

         EndUserFormDraftValue formDraftValue = formSubmissions.getFormDraft( form, endUser );
         if ( formDraftValue == null )
         {
            formDraftValue = formSubmissions.createFormDraft( form, endUser );
         }

         FormSubmission formSubmission = module.unitOfWorkFactory().currentUnitOfWork().get( FormSubmission.class, formDraftValue.formsubmission().get().identity() );
         context.set( form );
         context.set( formSubmission );
         context.set( formSubmission.getFormSubmission() );
         return subContext( FormDraftContext.class );
      }
   }
}