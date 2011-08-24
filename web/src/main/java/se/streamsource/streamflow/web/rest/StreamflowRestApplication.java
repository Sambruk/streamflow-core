/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.rest;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;
import org.restlet.security.Enroler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.application.security.DefaultEnroler;
import se.streamsource.streamflow.web.assembler.StreamflowWebAssembler;
import se.streamsource.streamflow.web.rest.resource.APIRouter;

/**
 * JAVADOC
 */
public class StreamflowRestApplication
        extends Application
{
   public static final MediaType APPLICATION_SPARQL_JSON = new MediaType("application/sparql-results+json", "SPARQL JSON");

   final Logger logger = LoggerFactory.getLogger("streamflow");

   @Structure
   Module module;

   Enroler enroler = new DefaultEnroler();

   @Structure
   ApplicationSPI app;
   private String name = "default";

   public StreamflowRestApplication()
   {
   }

   public StreamflowRestApplication(Context parentContext) throws Exception
   {
      super(parentContext);
      getMetadataService().addExtension("srj", APPLICATION_SPARQL_JSON);
      getMetadataService().addExtension("case", MediaType.register("application/x-streamflow-case+json", "Streamflow Case"));

   }

   /**
    * Creates a root Restlet that will receive all incoming calls.
    */
   @Override
   public Restlet createInboundRoot()
   {
      getContext().setDefaultEnroler(enroler);

      Router versions = new Router(getContext());

      Router api = module.objectBuilderFactory().newObjectBuilder(APIRouter.class).use(getContext()).newInstance();
      versions.attachDefault(api);

      return versions;
   }

   @Override
   public void start() throws Exception
   {
      if (isStopped())
      {
         try
         {
            // Start Qi4j
            Energy4Java is = new Energy4Java();
            StreamflowWebAssembler streamflowWebAssembler = new StreamflowWebAssembler(getMetadataService());

            String name = "StreamflowServer";
            Object host = getContext().getAttributes().get("streamflow.host");
            if (host != null)
               name += "-" + host;

            streamflowWebAssembler.setName(name);
            app = is.newApplication(streamflowWebAssembler);

            app.activate();

            app.findModule("Web", "REST").objectBuilderFactory().newObjectBuilder(StreamflowRestApplication.class).injectTo(this);

            super.start();
         } catch (Exception e)
         {
            e.printStackTrace();
            throw e;
         }
      }
   }

   @Override
   public void stop() throws Exception
   {
      if (isStarted())
      {
         super.stop();

         logger.info("Passivating Streamflow");
         app.passivate();
      }
   }
}
