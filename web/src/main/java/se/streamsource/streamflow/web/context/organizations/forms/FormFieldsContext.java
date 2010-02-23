/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(FormFieldsContext.Mixin.class)
public interface FormFieldsContext
   extends SubContexts<FormFieldContext>, Context
{
   public LinksValue fields();
   public void add( CreateFieldDTO createFieldDTO );

   abstract class Mixin
      extends ContextMixin
      implements FormFieldsContext
   {
      public LinksValue fields()
      {
         Fields.Data fields = context.role(Fields.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "field" ).addDescribables( fields.fields() ).newLinks();
      }

      public void add( CreateFieldDTO createFieldDTO )
      {
         Fields fields = context.role(Fields.class);

         fields.createField( createFieldDTO.name().get(), getFieldValue( createFieldDTO.fieldType().get() ) );
      }

      private FieldValue getFieldValue( FieldTypes fieldType )
      {
         FieldValue value = null;
         ValueBuilderFactory vbf = module.valueBuilderFactory();
         switch (fieldType)
         {
            case text:
               ValueBuilder<TextFieldValue> textBuilder = vbf.newValueBuilder( TextFieldValue.class );
               textBuilder.prototype().width().set( 30 );
               value = textBuilder.newInstance();
               break;
            case number:
               ValueBuilder<NumberFieldValue> numberBuilder = vbf.newValueBuilder( NumberFieldValue.class );
               numberBuilder.prototype().integer().set( true );
               value = numberBuilder.newInstance();
               break;
            case date:
               value = vbf.newValue( DateFieldValue.class );
               break;
            case selection:
               ValueBuilder<SelectionFieldValue> selection = vbf.newValueBuilder( SelectionFieldValue.class );
               value = selection.newInstance();
               break;
            case comment:
               ValueBuilder<CommentFieldValue> comment = vbf.newValueBuilder( CommentFieldValue.class );
               value = comment.newInstance();
               break;
         }
         return value;
      }

      public FormFieldContext context( String id )
      {
         FieldEntity field = module.unitOfWorkFactory().currentUnitOfWork().get( FieldEntity.class, id );
         context.playRoles( field );
         context.playRoles( field.fieldValue().get() );
         return subContext( FormFieldContext.class );
      }
   }
}
