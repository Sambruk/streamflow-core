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

package se.streamsource.streamflow.web.resource.organizations.projects.labels;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.label.LabelEntity;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{labels}/labels/{label}
 */
public class LabelServerResource
        extends CommandQueryServerResource
{
    public void describe(StringDTO stringValue)
    {
        String taskId = (String) getRequest().getAttributes().get("label");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.describe(stringValue.string().get());
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Delete label"));

        String labelsId = getRequest().getAttributes().get("labels").toString();
        String identity = getRequest().getAttributes().get("label").toString();

        Labels labels = uow.get(Labels.class, labelsId);

        LabelEntity group = uow.get(LabelEntity.class, identity);
        labels.removeLabel(group);

        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            throw new ResourceException(e);
        }

        return null;
    }

}