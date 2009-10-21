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

package se.streamsource.streamflow.web.resource.task.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.web.domain.form.SubmittedFormsQueries;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;

/**
 * Mapped to:
 * /tasks/{task}/forms/{index}
 */
public class TaskSubmittedFormServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    Qi4jSPI spi;

    public TaskSubmittedFormServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    public SubmittedFormDTO form() throws ResourceException
    {
        String formsQueryId = getRequest().getAttributes().get("task").toString();
        String index = getRequest().getAttributes().get("index").toString();
        SubmittedFormsQueries forms = uowf.currentUnitOfWork().get(SubmittedFormsQueries.class, formsQueryId);
        return forms.getSubmittedForm(Integer.parseInt(index));
    }

    @Override
    protected String getConditionalIdentityAttribute()
    {
        return "task";
    }
}