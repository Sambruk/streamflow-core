/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import java.util.List;

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.Requires;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.form.DatatypeDefinitionEntity;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.structure.form.Datatype;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinitions;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroup;
import se.streamsource.streamflow.web.domain.structure.form.FieldId;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.Mandatory;

/**
 * JAVADOC
 */
@Mixins(FormFieldContext.Mixin.class)
public interface FormFieldContext
      extends DeleteContext, Context
{
   public FieldDefinitionAdminValue field();

   public LinksValue possibledatatypes();
   
   public void changedescription( @MaxLength(100) @Name("description") String description );

   public void changemandatory( @Name("mandatory") boolean mandatory );

   public void changefieldid( @Name("id") String id );
   
   public void changedatatype( @Name("datatype") EntityValue dto);

   public void changehint( @Name("hint") String hint );

   @Requires(TextFieldValue.class)
   public void changewidth( @Name("width") int newWidth );

   @Requires(TextFieldValue.class)
   public void changeregularexpression( @Name("expression") String regularExpression );

   @Requires(TextAreaFieldValue.class)
   public void changerows( @Name("rows") int newRows );

   @Requires(TextAreaFieldValue.class)
   public void changecols( @Name("columns") int newRows );

   @Requires(NumberFieldValue.class)
   public void changeinteger( @Name("integer") boolean isInteger );

   @Requires(SelectionFieldValue.class)
   public void addselectionelement( @Name("selection") String name );

   @Requires(SelectionFieldValue.class)
   public void removeselectionelement( @Name("index") int index );

   @Requires(SelectionFieldValue.class)
   public void moveselectionelement( @Name("name") String name, @Name("index") int index);

   @Requires(SelectionFieldValue.class)
   public void changeselectionelementname( @Name("name") String name, @Name("index") int index );

   @Requires(OpenSelectionFieldValue.class)
   public void changeopenselectionname( @Name("name") String name );

   public void move( @Name("direction") String direction );

   abstract class Mixin
         implements FormFieldContext
   {
      @Structure
      Module module;

      public FieldDefinitionAdminValue field()
      {
         FieldEntity fieldEntity = RoleMap.role( FieldEntity.class );

         ValueBuilder<FieldDefinitionAdminValue> builder = module.valueBuilderFactory().newValueBuilder( FieldDefinitionAdminValue.class );
         builder.prototype().field().set( EntityReference.getEntityReference( fieldEntity ) );
         builder.prototype().note().set( fieldEntity.note().get() );
         builder.prototype().description().set( fieldEntity.description().get() );
         builder.prototype().fieldId().set( fieldEntity.fieldId().get() );
         if (fieldEntity.datatype().get() != null)
         {
            ValueBuilder<LinkValue> linkValueBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            linkValueBuilder.prototype().href().set( fieldEntity.datatype().get().getUrl() );
            linkValueBuilder.prototype().text().set( fieldEntity.datatype().get().getDescription() );
            linkValueBuilder.prototype().id().set(EntityReference.getEntityReference( fieldEntity.datatype().get()).identity() );
            linkValueBuilder.prototype().rel().set( "datatype" );
            builder.prototype().datatype().set( linkValueBuilder.newInstance());
         }
         if (fieldEntity.fieldValue().get() instanceof FieldGroupFieldValue)
         {
            ValueBuilder<LinkValue> linkValueBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
            FieldGroup fieldGroup = module.unitOfWorkFactory().currentUnitOfWork().get( FieldGroup.class, ((FieldGroupFieldValue) fieldEntity.fieldValue().get()).fieldGroup().get().identity());
            linkValueBuilder.prototype().href().set( "" );
            linkValueBuilder.prototype().text().set( fieldGroup.getDescription());
            linkValueBuilder.prototype().id().set(EntityReference.getEntityReference( fieldGroup ).identity());
            linkValueBuilder.prototype().rel().set( "fieldgroup" );
            builder.prototype().fieldgroup().set( linkValueBuilder.newInstance());
         }
         builder.prototype().fieldValue().set( fieldEntity.fieldValue().get() );
         builder.prototype().mandatory().set( fieldEntity.isMandatory() );

         return builder.newInstance();
      }

      public LinksValue possibledatatypes()
      {
         List<DatatypeDefinition> definitions = RoleMap.role( DatatypeDefinitions.Data.class ).datatypeDefinitions().toList();
         
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         builder.rel( "datatype" );
         for (DatatypeDefinition dataTypeDefinition : definitions)
         {
            builder.addLink( dataTypeDefinition.getDescription(), EntityReference.getEntityReference( dataTypeDefinition).identity() );
         }

         return builder.newLinks();
      }

      public void changedescription( String description )
      {
         Describable describable = RoleMap.role( Describable.class );
         describable.changeDescription( description );
      }

      public void changemandatory( boolean mandatory )
      {
         Mandatory mandatoryField = RoleMap.role( Mandatory.class );

         mandatoryField.changeMandatory( mandatory );
      }

      public void changefieldid( String id )
      {
         FieldId fieldId = RoleMap.role( FieldId.class );

         fieldId.changeFieldId( id );
      }

      public void changedatatype( EntityValue dto )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         Datatype datatype = RoleMap.role( Datatype.class );
         String entityReference = dto.entity().get();
         if (entityReference != null)
         {
            DatatypeDefinitionEntity dataTypeDefinitionEntity = uow.get( DatatypeDefinitionEntity.class, entityReference );
            datatype.changeDatatype( dataTypeDefinitionEntity );
         } else 
         {
            datatype.changeDatatype( null );
         }
         
      }

      public void changehint( String hint )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         TextFieldValue value = RoleMap.role( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().hint().set( hint );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changewidth( int newWidth )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         TextFieldValue value = RoleMap.role( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().width().set( newWidth );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeregularexpression( String regularExpression )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         TextFieldValue value = RoleMap.role( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().regularExpression().set( regularExpression );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changerows( int newRows )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         TextAreaFieldValue value = RoleMap.role( TextAreaFieldValue.class );

         ValueBuilder<TextAreaFieldValue> builder = value.buildWith();
         builder.prototype().rows().set( newRows );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changecols( int newCols )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         TextAreaFieldValue value = RoleMap.role( TextAreaFieldValue.class );

         ValueBuilder<TextAreaFieldValue> builder = value.buildWith();
         builder.prototype().cols().set( newCols );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeinteger( boolean isInteger )
      {
         FieldValueDefinition definition = RoleMap.role( FieldValueDefinition.class );
         NumberFieldValue value = RoleMap.role( NumberFieldValue.class );

         ValueBuilder<NumberFieldValue> builder = value.buildWith();
         builder.prototype().integer().set( isInteger );

         definition.changeFieldValue( builder.newInstance() );
      }

      public void addselectionelement( String name )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         SelectionFieldValue value = RoleMap.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().add( name );
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void removeselectionelement( int index )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         SelectionFieldValue value = RoleMap.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         if (builder.prototype().values().get().size() > index)
         {
            builder.prototype().values().get().remove( index );
            fieldValueDefinition.changeFieldValue( builder.newInstance() );
         }
      }

      public void moveselectionelement( String name, int index )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         SelectionFieldValue value = RoleMap.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         String element = builder.prototype().values().get().remove( index );
         if ("up".equals( name ))
         {
            builder.prototype().values().get().add( index - 1, element );
         } else
         {
            builder.prototype().values().get().add( index + 1, element );
         }
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeselectionelementname( String name, int index )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         SelectionFieldValue value = RoleMap.role( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().set( index, name );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeopenselectionname( String name )
      {
         FieldValueDefinition fieldValueDefinition = RoleMap.role( FieldValueDefinition.class );
         OpenSelectionFieldValue value = RoleMap.role( OpenSelectionFieldValue.class );

         ValueBuilder<OpenSelectionFieldValue> builder = value.buildWith();
         builder.prototype().openSelectionName().set( name );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void move( String direction )
      {
         Field field = RoleMap.role( Field.class );
         Fields fields = RoleMap.role( Fields.class );
         Fields.Data fieldsData = RoleMap.role( Fields.Data.class );

         int index = fieldsData.fields().toList().indexOf( field );
         if (direction.equalsIgnoreCase( "up" ))
         {
            try
            {
               fields.moveField( field, index - 1 );
            } catch (ConstraintViolationException e)
            {
            }
         } else
         {
            fields.moveField( field, index + 1 );
         }
      }

      public void delete()
      {
         Field field = RoleMap.role( Field.class );
         Fields fields = RoleMap.role( Fields.class );

         fields.removeField( field );
      }
   }
}
