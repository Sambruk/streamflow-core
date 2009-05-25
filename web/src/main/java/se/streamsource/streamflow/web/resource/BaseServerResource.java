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

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.InputStream;

/**
 * Base class for server-side resources.
 */
public class BaseServerResource
        extends ServerResource
{
    protected Representation getHtml(String resourceName) throws ResourceException
    {
        InputStream asStream = getClass().getResourceAsStream(resourceName);
        if (asStream == null)
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
        else
            return new InputRepresentation(asStream, MediaType.TEXT_HTML);
    }
}
