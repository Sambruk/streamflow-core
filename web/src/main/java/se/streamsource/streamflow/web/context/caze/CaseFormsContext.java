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

package se.streamsource.streamflow.web.context.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionsQueries;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;

/**
 * JAVADOC
 */
@Mixins(CaseFormsContext.Mixin.class)
public interface CaseFormsContext
      extends SubContexts<CaseFormContext>, Context
{
   SubmittedFormsListDTO listsubmittedforms();

   EffectiveFieldsDTO effectivefields();

   SubmittedFormDTO submittedform( IntegerDTO index );

   void createformsubmission( EntityReferenceDTO formDTO );

   LinkValue formsubmission( EntityValue formDTO );

   LinksValue possibleforms();

   abstract class Mixin
         extends ContextMixin
         implements CaseFormsContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      public SubmittedFormsListDTO listsubmittedforms()
      {
         SubmittedFormsQueries forms = roleMap.get( SubmittedFormsQueries.class );
         return forms.getSubmittedForms();
      }

      public EffectiveFieldsDTO effectivefields()
      {
         SubmittedFormsQueries fields = roleMap.get( SubmittedFormsQueries.class );

         return fields.effectiveFields();
      }

      public SubmittedFormDTO submittedform( IntegerDTO index )
      {
         SubmittedFormsQueries forms = roleMap.get( SubmittedFormsQueries.class );

         return forms.getSubmittedForm( index.integer().get() );
      }

      public void createformsubmission( EntityReferenceDTO formDTO )
      {
         FormSubmissions formSubmissions = roleMap.get( FormSubmissions.class );

         Form form = uowf.currentUnitOfWork().get( Form.class, formDTO.entity().get().identity() );

         formSubmissions.createFormSubmission( form );
      }

      public LinkValue formsubmission( EntityValue formDTO )
      {
         Form form = uowf.currentUnitOfWork().get( Form.class, formDTO.entity().get() );

         FormSubmissions formSubmissions = roleMap.get( FormSubmissions.class );

         FormSubmission formSubmission = formSubmissions.getFormSubmission( form );
         if (formSubmission == null)
            throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);

         ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
         builder.prototype().id().set( formSubmission.toString() );
         builder.prototype().text().set(formSubmission.toString());
         builder.prototype().rel().set( "formsubmission" );
         builder.prototype().href().set( formSubmission.toString()+"/" );
         return builder.newInstance();
      }

      public LinksValue possibleforms()
      {
         TypedCase.Data typedCase = roleMap.get( TypedCase.Data.class );

         CaseType caseType = typedCase.caseType().get();

         if (caseType != null)
         {
            SelectedForms.Data forms = (SelectedForms.Data) caseType;
            return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( forms.selectedForms() ).newLinks();
         } else
         {
            return new LinksBuilder( module.valueBuilderFactory() ).newLinks();
         }
      }

      public CaseFormContext context( String id )
      {
         roleMap.set( uowf.currentUnitOfWork().get( FormSubmissionEntity.class, id ) );

         return subContext( CaseFormContext.class );
      }
   }
}
