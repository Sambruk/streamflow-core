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

package se.streamsource.streamflow.web.resource.organizations.forms;

import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/forms
 */
public class FormTemplatesServerResource
      extends CommandQueryServerResource
{
/*
   public ListValue forms()
   {
      String identity = getRequest().getAttributes().get( "organization" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      //TODO: Quick and dirty fix for ClassCastExcepion to be able to deploy to test server - must be removed
      ListValueBuilder listBuilder = new ListValueBuilder( vbf );
      ListValue resp = listBuilder.newList();

      try
      {
         FormQueries forms = uow.get( FormQueries.class, identity );

         resp = forms.getForms();
      } catch (ClassCastException cce)
      {
      }

      return resp;
   }

   public void createTemplate( EntityReferenceDTO formDTO ) throws ResourceException
   {
      String identity = getRequest().getAttributes().get( "organization" ).toString();

      UnitOfWork uow = uowf.currentUnitOfWork();

      FormTemplates templates = uow.get( FormTemplates.class, identity );
      Form form = uow.get( Form.class, formDTO.entity().get().identity() );

      templates.createFormTemplate( form );
   }
*/
}