/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.util;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedPageValue;
import se.streamsource.streamflow.web.domain.structure.form.VisibilityRule;

/**
 * This class is able to validate visibility rules for a SubmittedFormValue.
 *
 */
public class FormVisibilityRuleValidator
{
   @Structure
   Module module;

   private SubmittedFormValue submittedForm;
   private FormDraftDTO formDraft;

   public FormVisibilityRuleValidator( @Optional @Uses SubmittedFormValue submittedForm, @Optional @Uses FormDraftDTO formDraft )
   {
      this.submittedForm = submittedForm;
      this.formDraft = formDraft;
   }

   public boolean visible( SubmittedFieldValue submittedFieldValue )
   {
      boolean visible;

      final VisibilityRule fieldRule = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, submittedFieldValue.field().get().identity() );

      if( fieldRule.getRule() == null || Strings.empty( fieldRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         // find submitted field matching rule field and fetch value - check if submitted field is visible - yes - validate
         // this call will be recursive if several fields are chained by visibility rules.
         SubmittedFieldValue fieldValue = Iterables.first( Iterables.filter( new Specification<SubmittedFieldValue>()
         {
            public boolean satisfiedBy( SubmittedFieldValue field )
            {
               return field.field().get().identity().equals( fieldRule.getRule().field().get() );
            }
         }, submittedForm.fields() ) );

         if( visible( fieldValue ) )
         {
            // validate
            visible = fieldRule.validate( fieldValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }

   public boolean visible( SubmittedPageValue submittedPageValue )
   {
      boolean visible;

      final VisibilityRule pageRule = module.unitOfWorkFactory().currentUnitOfWork().get( PageEntity.class, submittedPageValue.page().get().identity() );

      if( pageRule.getRule() == null || Strings.empty( pageRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         //find submitted field matching rule field and fetch value - check if submitted field is visible - yes - validate
         SubmittedFieldValue fieldValue = Iterables.first( Iterables.filter( new Specification<SubmittedFieldValue>()
         {
            public boolean satisfiedBy( SubmittedFieldValue field )
            {
               return field.field().get().identity().equals( pageRule.getRule().field().get() );
            }
         }, submittedForm.fields() ) );

         if( visible( fieldValue ) )
         {
           // validate
           visible = pageRule.validate( fieldValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }

   public boolean visible( FieldSubmissionDTO fieldSubmission )
   {
      boolean visible;

      final VisibilityRule fieldRule = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, fieldSubmission.field().get().field().get().identity() );

      if( fieldRule.getRule() == null || Strings.empty( fieldRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         // find form draft submission field matching rule field and fetch value - check if submitted field is visible - yes - validate
         // this call will be recursive if several fields are chained by visibility rules.
         FieldSubmissionDTO submissionValue = Iterables.first( Iterables.filter( new Specification<FieldSubmissionDTO>()
         {
            public boolean satisfiedBy( FieldSubmissionDTO field )
            {
               return field.field().get().field().get().identity().equals( fieldRule.getRule().field().get() );
            }
         }, Iterables.flatten( Iterables.map( new Function<PageSubmissionDTO, Iterable<FieldSubmissionDTO>>()
         {
            public Iterable<FieldSubmissionDTO> map( PageSubmissionDTO page )
            {
               return page.fields().get();
            }
         } , formDraft.pages().get() ) ) ) );

         if( visible( submissionValue ) )
         {
            // validate
            visible = fieldRule.validate( submissionValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }

   public boolean visible( PageSubmissionDTO pageSubmissionDTO )
   {
      boolean visible;

      final VisibilityRule pageRule = module.unitOfWorkFactory().currentUnitOfWork().get( PageEntity.class, pageSubmissionDTO.page().get().identity() );

      if( pageRule.getRule() == null || Strings.empty( pageRule.getRule().field().get() ))
      {
         visible = true;
      } else
      {
         //find submitted field matching rule field and fetch value - check if submitted field is visible - yes - validate
         FieldSubmissionDTO submissionValue = Iterables.first( Iterables.filter( new Specification<FieldSubmissionDTO>()
         {
            public boolean satisfiedBy( FieldSubmissionDTO field )
            {
               return field.field().get().field().get().identity().equals( pageRule.getRule().field().get() );
            }
         }, Iterables.flatten( Iterables.map( new Function<PageSubmissionDTO, Iterable<FieldSubmissionDTO>>()
         {
            public Iterable<FieldSubmissionDTO> map( PageSubmissionDTO page )
            {
               return page.fields().get();
            }
         }, formDraft.pages().get() ) ) ) );

         if( visible( submissionValue ) )
         {
            // validate
            visible = pageRule.validate( submissionValue.value().get() );

         } else
         {
            visible = false;
         }
      }

      return visible;
   }
}
