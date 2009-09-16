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

package se.streamsource.streamflow.web.rest;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import se.streamsource.streamflow.web.resource.CompositeCommandQueryServerResource;

/**
 * JAVADOC
 */
public class CompositeFinder extends Finder
{
    @Structure
    private ObjectBuilderFactory factory;

    @Structure
    private TransientBuilderFactory compositeFactory;

    public CompositeFinder()
    {
    }

    @Override
    public ServerResource create(Request request, Response response)
    {
        Object resource = compositeFactory.newTransientBuilder(getTargetClass()).use(request, response).newInstance();

        return factory.newObjectBuilder(CompositeCommandQueryServerResource.class).use(resource).newInstance();
    }
}