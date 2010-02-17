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

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}/pages/{page}
 */
public class FormDefinitionPageServerResource
      extends CommandQueryServerResource
{
   public PageDefinitionValue page() throws ResourceException
   {
      PageEntity pageEntity = getPageEntity();

      ValueBuilder<PageDefinitionValue> builder = vbf.newValueBuilder( PageDefinitionValue.class );
      builder.prototype().description().set( pageEntity.getDescription() );
      builder.prototype().page().set( EntityReference.parseEntityReference( pageEntity.identity().get() ));
      return builder.newInstance();
   }

   public void changedescription( StringDTO newDescription ) throws ResourceException
   {
      getPageEntity().changeDescription( newDescription.string().get() );
   }

   public void changenote( StringDTO newNote ) throws ResourceException
   {
      getPageEntity().changeNote( newNote.string().get() );
   }

   private PageEntity getPageEntity()
         throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String pageId = getRequest().getAttributes().get( "page" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      PageEntity pageEntity = uow.get( PageEntity.class, pageId );
      if (!form.pages().contains( pageEntity ) )
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
      }
      return pageEntity;
   }

   public void move( StringDTO direction ) throws ResourceException
   {
      PageEntity pageEntity = getPageEntity();
      String identity = getRequest().getAttributes().get( "form" ).toString();
      FormEntity form = uowf.currentUnitOfWork().get( FormEntity.class, identity );

      int index = form.pages().toList().indexOf( pageEntity );
      if ( direction.string().get().equalsIgnoreCase( "UP" ))
      {
         try
         {
            form.movePage( pageEntity, index-1 );
         } catch(ConstraintViolationException e) {}
      } else
      {
         form.movePage( pageEntity, index+1);
      }
   }

   public void deleteOperation() throws ResourceException
   {
      PageEntity pageEntity = getPageEntity();
      String identity = getRequest().getAttributes().get( "form" ).toString();
      FormEntity form = uowf.currentUnitOfWork().get( FormEntity.class, identity );

      form.removePage( pageEntity );
   }

}