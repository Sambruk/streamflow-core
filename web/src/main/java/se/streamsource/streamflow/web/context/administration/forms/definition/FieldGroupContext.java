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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldTypes;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroup;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroups;
import se.streamsource.streamflow.web.domain.structure.form.Fields;

/**
 * JAVADOC
 */
public class FieldGroupContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

      FieldGroup fieldGroup = RoleMap.role( FieldGroup.class );
      for (Field field : ((Fields.Data)fieldGroup).fields())
      {
         linksBuilder.rel( "field" );
         linksBuilder.addDescribable( field );
         linksBuilder.path( null );
      }

      return linksBuilder.newLinks();
   }


   public void delete()
   {
      FieldGroup fieldGroup = RoleMap.role( FieldGroup.class );
      FieldGroups fieldGroups = RoleMap.role( FieldGroups.class );

      fieldGroups.removeFieldGroup( fieldGroup );
   }

   public void create( CreateFieldDTO createFieldDTO )
   {
      Fields fields = RoleMap.role( Fields.class );

      fields.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
   }

   private FieldValue getFieldValue( FieldTypes fieldType )
   {
      FieldValue value = null;
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      switch (fieldType)
      {
         case attachment:
            value = vbf.newValue( AttachmentFieldValue.class );
            break;
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
         case openselection:
            ValueBuilder<OpenSelectionFieldValue> valueBuilder = vbf.newValueBuilder( OpenSelectionFieldValue.class );
            valueBuilder.prototype().openSelectionName().set( "" );
            value = valueBuilder.newInstance();
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
}
