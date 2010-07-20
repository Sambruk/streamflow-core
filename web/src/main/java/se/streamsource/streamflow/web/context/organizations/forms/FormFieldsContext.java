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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(FormFieldsContext.Mixin.class)
public interface FormFieldsContext
      extends SubContexts<FormFieldContext>, Interactions
{
   public LinksValue fields();
   public void add( CreateFieldDTO createFieldDTO );

   abstract class Mixin
         extends InteractionsMixin
         implements FormFieldsContext
   {
      public LinksValue fields()
      {
         Fields.Data fields = context.get(Fields.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "field" ).addDescribables( fields.fields() ).newLinks();
      }

      public void add( CreateFieldDTO createFieldDTO )
      {
         Fields fields = context.get(Fields.class);

         fields.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
      }

      private FieldValue getFieldValue( FieldTypes fieldType )
      {
         FieldValue value = null;
         ValueBuilderFactory vbf = module.valueBuilderFactory();
         switch (fieldType)
         {
            case checkboxes:
               value = vbf.newValue( CheckboxesFieldValue.class );
               break;
            case combobox:
               value = vbf.newValue( ComboBoxFieldValue.class );
               break;
            case comment:
               value = vbf.newValue( CommentFieldValue.class );
               break;
            case date:
               value = vbf.newValue( DateFieldValue.class );
               break;
            case listbox:
               value = vbf.newValue( ListBoxFieldValue.class );
               break;
            case number:
               ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
               numberBuilder.prototype().integer().set( true );
               value = numberBuilder.newInstance();
               break;
            case optionbuttons:
               value = vbf.newValue( OptionButtonsFieldValue.class );
               break;
            case textarea:
               ValueBuilder<TextAreaFieldValue> builder = vbf.newValueBuilder( TextAreaFieldValue.class );
               builder.prototype().cols().set( 30 );
               builder.prototype().rows().set( 5 );
               value = builder.newInstance();
               break;
            case text:
               ValueBuilder<TextFieldValue> textBuilder = vbf.newValueBuilder( TextFieldValue.class );
               textBuilder.prototype().width().set( 30 );
               value = textBuilder.newInstance();
               break;
         }
         return value;
      }

      public FormFieldContext context( String id )
      {
         FieldEntity field = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, id );
         context.set( field );
         context.set( field.fieldValue().get() );
         return subContext( FormFieldContext.class );
      }
   }
}
