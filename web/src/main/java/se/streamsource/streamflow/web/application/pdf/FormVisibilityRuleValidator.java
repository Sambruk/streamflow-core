package se.streamsource.streamflow.web.application.pdf;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
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

   public FormVisibilityRuleValidator( @Uses SubmittedFormValue submittedForm )
   {
      this.submittedForm = submittedForm;
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
}
