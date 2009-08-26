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

package se.streamsource.streamflow.web.resource;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.InputStream;

/**
 * Base class for server-side resources.
 */
public class BaseServerResource
        extends ServerResource
{
    protected @Structure
    UnitOfWorkFactory uowf;

    protected @Structure
    Qi4jSPI spi;
    Usecase usecase = UsecaseBuilder.newUsecase("Get identity");

    protected Representation getHtml(String resourceName) throws ResourceException
    {
        InputStream asStream = getClass().getResourceAsStream(resourceName);
        if (asStream == null)
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        else
            return new InputRepresentation(asStream, MediaType.TEXT_HTML);
    }

    @Override
    protected RepresentationInfo getInfo(Variant variant) throws ResourceException
    {
        RepresentationInfo info = new RepresentationInfo();

        String id = getConditionalIdentityAttribute();
        if (id != null)
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            if (uow == null)
                uow = uowf.newUnitOfWork(usecase);

            Object task = uow.get(Object.class, getRequestAttributes().get(id).toString());

            String eTag = spi.getEntityState((EntityComposite) task).version();
            info.setTag(new Tag(eTag));

            if (uow.usecase().name().equals("Get identity"))
                uow.discard();
        }

        return info;
    }

    protected String getConditionalIdentityAttribute()
    {
        return null;
    }
}
