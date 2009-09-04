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

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.Verifier;
import se.streamsource.streamflow.web.StreamFlowWebAssembler;
import se.streamsource.streamflow.web.resource.APIv1Router;

/**
 * JAVADOC
 */
public class StreamFlowRestApplication
        extends Application
{
    public static final MediaType APPLICATION_SPARQL_JSON = new MediaType("application/sparql-results+json", "SPARQL JSON");

    @Structure
    ObjectBuilderFactory factory;
    @Structure
    UnitOfWorkFactory unitOfWorkFactory;

    @Optional
    @Service
    Verifier verifier;

    @Structure
    org.qi4j.api.structure.Application app;

    public StreamFlowRestApplication(@Uses Context parentContext) throws Exception
    {
        super(parentContext);

        getMetadataService().addExtension("srj", APPLICATION_SPARQL_JSON);

        // Start Qi4j
        Energy4Java is = new Energy4Java();
        final ApplicationSPI app = is.newApplication(new StreamFlowWebAssembler(this));

        app.activate();

        app.findModule("Web", "REST").objectBuilderFactory().newObjectBuilder(StreamFlowRestApplication.class).injectTo(this);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    app.passivate();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot()
    {
        getContext().setVerifier(verifier);

        Router api = factory.newObjectBuilder(APIv1Router.class).use(getContext()).newInstance();

        Router versions = new Router(getContext());
        versions.attach("/v1", api);

        return versions;
    }
}
