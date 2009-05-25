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
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;
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

    @Service
    ChallengeAuthenticator authenticator;

    @Optional
    @Service
    Authorizer authorizer;

    @Service
    Verifier verifier;

    public StreamFlowRestApplication(@Uses Context parentContext) throws Exception
    {
        super(parentContext);

        getMetadataService().addExtension("srj", APPLICATION_SPARQL_JSON);

        // Start Qi4j
        Energy4Java is = new Energy4Java();
        ApplicationSPI app = is.newApplication(new StreamFlowWebAssembler());
        app.metaInfo().set(this);
        app.metaInfo().set(new ChallengeAuthenticator(parentContext, ChallengeScheme.HTTP_BASIC, "StreamFlow"));

        app.activate();

        app.findModule("Web", "REST").objectBuilderFactory().newObjectBuilder(StreamFlowRestApplication.class).injectTo(this);
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot()
    {
        Router api = new APIv1Router(getContext(), factory);

/*
        Router qi4jRouter = new Router(getContext());
        qi4jRouter.attach("/entity", createFinder(EntitiesResource.class));
        qi4jRouter.attach("/entity/{identity}", createFinder(EntityResource.class));
        qi4jRouter.attach("/type", createFinder(EntityTypesResource.class));
        qi4jRouter.attach("/type/{version}", createFinder(EntityTypeResource.class));
        qi4jRouter.attach("/query", createFinder(SPARQLResource.class));
        qi4jRouter.attach("/query/index", createFinder(IndexResource.class));

//      api.attach("/qi4j", new ExtensionMediaTypeFilter(getContext(), qi4jRouter));
        api.attach("/qi4j", qi4jRouter);
*/


/*
       api.attach( "/entitytype", createFinder( EntityTypesResource.class ) );
       api.attach( "/entitytype/{type}", createFinder( EntityTypeResource.class ) );

       api.attach( "/entity", createFinder( AllEntitiesResource.class ) );
       api.attach( "/entity/{type}", createFinder( EntitiesResource.class ) );

       api.attach( "/query", createFinder( SPARQLResource.class ) );
       api.attach( "/query/index", createFinder( IndexResource.class ) );
*/

        Router versions = new Router(getContext());
        versions.attach("/v1", api);

        Directory directory = new Directory(getContext(), "clap://class/static/");
        directory.setListingAllowed(true);
        versions.attach("version.html", directory);


        // Guard
        authenticator.setVerifier(verifier);
        authenticator.setNext(versions);

        return authenticator;
    }

    private Finder createServerResourceFinder(Class<? extends ServerResource> resource)
    {
        ResourceFinder finder = factory.newObject(ResourceFinder.class);
        finder.setTargetClass(resource);
        return finder;
    }
}
