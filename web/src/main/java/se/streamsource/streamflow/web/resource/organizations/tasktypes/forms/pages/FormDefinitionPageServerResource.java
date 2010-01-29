/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.pages;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}
 */
public class FormDefinitionPageServerResource
      extends CommandQueryServerResource
{
   public PageDefinitionValue page()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );

      ValueBuilder<PageDefinitionValue> builder = vbf.newValueBuilder( PageDefinitionValue.class );
      ValueBuilder<FieldDefinitionValue> fieldBuilder = vbf.newValueBuilder( FieldDefinitionValue.class );
      builder.prototype().title().set( pageEntity.getDescription() );
      builder.prototype().page().set( EntityReference.parseEntityReference( pageEntity.identity().get() ));

      for (Field field : pageEntity.fields())
      {
         FieldEntity fieldEntity = (FieldEntity) field;
         fieldBuilder.prototype().field().set( EntityReference.parseEntityReference( fieldEntity.identity().get() ));
         fieldBuilder.prototype().fieldValue().set( fieldEntity.fieldValue().get() );
         fieldBuilder.prototype().description().set( field.getDescription() );
         fieldBuilder.prototype().note().set( field.getNote() );
         builder.prototype().fields().get().add( fieldBuilder.newInstance() );
      }
      return builder.newInstance();
   }

   public void changedescription( StringDTO newDescription )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );

      pageEntity.changeDescription( newDescription.string().get() );
   }

   public void changenote( StringDTO newNote )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );

      pageEntity.changeNote( newNote.string().get() );
   }

   public void move( IntegerDTO newIndex )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );

      form.movePage( pageEntity, newIndex.integer().get() );
   }

   public void deleteOperation()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );

      form.removePage( pageEntity );
   }

}