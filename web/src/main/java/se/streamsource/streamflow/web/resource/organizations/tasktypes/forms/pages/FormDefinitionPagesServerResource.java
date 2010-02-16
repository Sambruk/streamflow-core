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

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.domain.entity.form.PageQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}/pages
 */
public class FormDefinitionPagesServerResource
      extends CommandQueryServerResource
{
   public ListValue pages()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );

      return new ListValueBuilder( vbf ).addDescribableItems( form.pages() ).newList();
   }

   public ListValue pagessummary()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      PageQueries pageQueries = uow.get( PageQueries.class, identity );
      checkPermission( pageQueries );

      return pageQueries.getPagesSummary();
   }

   public void add( StringDTO name )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );

      form.createPage( name.string().get() );
   }
}