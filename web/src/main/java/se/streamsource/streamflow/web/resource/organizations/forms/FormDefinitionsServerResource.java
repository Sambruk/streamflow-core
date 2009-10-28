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

import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.form.FormDefinition;
import se.streamsource.streamflow.web.domain.form.FormDefinitions;
import se.streamsource.streamflow.web.domain.form.FormDefinitionsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/forms
 */
public class FormDefinitionsServerResource
        extends CommandQueryServerResource
{
    public ListValue forms()
    {
        String identity = getRequest().getAttributes().get("organization").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        //TODO: Quick and dirty fix for ClassCastExcepion to be able to deploy to test server - must be removed
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);
        ListValue resp = listBuilder.newList();

        try
        {
            FormDefinitionsQueries forms = uow.get(FormDefinitionsQueries.class, identity);

            resp = forms.formDefinitionList();
        } catch(ClassCastException cce)
        {
        }  

        return resp;
    }

    public void createForm( StringDTO name) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("organization").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormDefinitions formDefinitions = uow.get( FormDefinitions.class, identity );

        formDefinitions.createFormDefinition( name.string().get() );
    }

    public void removeForm(EntityReferenceDTO formReference) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("organization").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormDefinitions formDefinitions = uow.get( FormDefinitions.class, identity );

        FormDefinition form = uow.get( FormDefinition.class, formReference.entity().get().identity() );

        formDefinitions.removeFormDefinition( form );
    }

}