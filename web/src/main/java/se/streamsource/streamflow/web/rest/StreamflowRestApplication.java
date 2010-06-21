/**
 *
 * Copyright 2009-2010 Streamsource AB
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
import org.restlet.security.Enroler;
import org.restlet.security.Verifier;
import se.streamsource.streamflow.web.assembler.StreamflowWebAssembler;
import se.streamsource.streamflow.web.application.security.DefaultEnroler;
import se.streamsource.streamflow.web.resource.APIv1Router;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class StreamflowRestApplication
      extends Application
{
   public static final MediaType APPLICATION_SPARQL_JSON = new MediaType( "application/sparql-results+json", "SPARQL JSON" );

   @Structure
   ObjectBuilderFactory factory;
   @Structure
   UnitOfWorkFactory unitOfWorkFactory;

   @Optional
   @Service
   Verifier verifier;

   Enroler enroler = new DefaultEnroler();

   @Structure
   ApplicationSPI app;

   public StreamflowRestApplication( @Uses Context parentContext ) throws Exception
   {
      super( parentContext );

      getMetadataService().addExtension( "srj", APPLICATION_SPARQL_JSON );

   }

   /**
    * Creates a root Restlet that will receive all incoming calls.
    */   
   @Override
   public Restlet createInboundRoot()
   {
      getContext().setDefaultVerifier( verifier );
      getContext().setDefaultEnroler( enroler );

      Router versions = new Router( getContext() );

      Router api = factory.newObjectBuilder( APIv1Router.class ).use( getContext() ).newInstance();
      versions.attach( "/v1", api );

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
            app = is.newApplication( new StreamflowWebAssembler( this, getMetadataService() ) );
            app = is.newApplication( new StreamflowWebAssembler( getMetadataService() ) );

            app.activate();

            app.findModule( "Web", "REST" ).objectBuilderFactory().newObjectBuilder( StreamflowRestApplication.class ).injectTo( this );

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

         Logger.getLogger( "streamflow" ).info( "Passivating Streamflow" );
         app.passivate();
      }
   }
}
