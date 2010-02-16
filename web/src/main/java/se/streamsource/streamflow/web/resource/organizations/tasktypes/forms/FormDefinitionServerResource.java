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

package se.streamsource.streamflow.web.resource.organizations.tasktypes.forms;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}
 */
public class FormDefinitionServerResource
      extends CommandQueryServerResource
{
   public FormValue form()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity);

      ValueBuilder<FormValue> builder = vbf.newValueBuilder( FormValue.class );

      builder.prototype().note().set( form.note().get() );
      builder.prototype().description().set( form.description().get() );
      builder.prototype().form().set( EntityReference.parseEntityReference( form.identity().get() ) );

      return builder.newInstance();
   }

   public void changedescription( StringDTO newDescription )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity);

      form.changeDescription( newDescription.string().get() );
   }

   public void changenote( StringDTO newNote )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity);

      form.changeNote( newNote.string().get() );
   }


/*
   public void deleteOperation()
   {
      String formsId = getRequest().getAttributes().get( "forms" ).toString();
      String identity = getRequest().getAttributes().get( "form" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity);

      FormsQueries formsQueries = uow.get( FormsQueries.class, formsId );

      checkPermission( formsQueries );

      Forms forms = (Forms) formsQueries;

      forms.removeForm( form );
   }
*/
}