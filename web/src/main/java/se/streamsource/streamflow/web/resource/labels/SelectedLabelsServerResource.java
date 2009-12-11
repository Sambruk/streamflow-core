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

package se.streamsource.streamflow.web.resource.labels;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.domain.label.SelectedLabels;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{labels}/labels
 * /organizations/{organization}/organizationalunits/{labels}/labels
 * /organizations/{organization}/organizationalunits/{ou}/projects/{labels}/labels
 */
public class SelectedLabelsServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public ListValue selectedlabels()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("labels");
        SelectedLabels.Data labels = uow.get( SelectedLabels.Data.class, id);

        return new ListValueBuilder(vbf).addDescribableItems( labels.selectedLabels() ).newList();
    }

    public ListValue possiblelabels()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("labels");
        String organization = (String) getRequest().getAttributes().get("organization");
        SelectedLabels.Data selectedLabels = uow.get( SelectedLabels.Data.class, id);
        Labels.Data labels = uow.get( Labels.Data.class, organization);
        
        return selectedLabels.possibleLabels( labels.labels() );
    }

    public void createlabel(StringDTO name) throws ResourceException
    {
        String org = getRequest().getAttributes().get("organization").toString();
        String identity = getRequest().getAttributes().get("labels").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        Labels labels = uow.get(Labels.class, org);
        SelectedLabels selectedLabels = uow.get(SelectedLabels.class, identity);

        selectedLabels.createLabel(labels, name.string().get());
    }

    public void addlabel( EntityReferenceDTO labelDTO) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("labels").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        SelectedLabels labels = uow.get(SelectedLabels.class, identity);
        Label label = uow.get(Label.class, labelDTO.entity().get().identity());

        labels.addLabel( label );
    }
}