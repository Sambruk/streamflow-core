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

package se.streamsource.dci.test;

import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.bootstrap.*;
import org.qi4j.spi.structure.*;
import org.restlet.Application;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.routing.*;
import org.restlet.security.*;
import org.slf4j.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.sitemesh.*;

import javax.management.*;
import java.util.*;

/**
 * JAVADOC
 */
public class TestRestletApplication
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

   @Structure
   ApplicationSPI app;

   Thread shutdownHook = new Thread()
   {
      @Override
      public void run()
      {
         try
         {
            LoggerFactory.getLogger( "test" ).info( "VM shutdown; passivating application" );
            app.passivate();
         } catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   };

   public TestRestletApplication( @Uses Context parentContext ) throws Exception
   {
      super( parentContext );

      getMetadataService().addExtension( "srj", APPLICATION_SPARQL_JSON );

   }

   @Override
   public Restlet createInboundRoot()
   {
      getContext().setDefaultVerifier( verifier );

      Restlet cqr = factory.newObjectBuilder( CommandQueryRestlet2.class ).use( getContext() ).newInstance();

//      ViewFilter viewFilter = factory.newObjectBuilder( ViewFilter.class ).use( getContext(), cqr ).newInstance();

      RestletFilterBuilder builder = new RestletFilterBuilder().
            setContext( getContext() ).
            setNext( cqr ).
            addDecoratorPath( "/files*", "clap://class/decorator/menu.html" ).
            addDecoratorPath( "/", "clap://class/decorator/main.html" );
      Filter siteMeshFilter = builder.create();
      getContext().getClientDispatcher().setProtocols( Arrays.asList( Protocol.CLAP ) );
      return new ExtensionMediaTypeFilter( getContext(), cqr );
   }

   @Override
   public void start() throws Exception
   {
      try
      {
         MBeanServerFactory.createMBeanServer();

         // Start Qi4j
         Energy4Java is = new Energy4Java();
         app = is.newApplication( new TestAssembler( getMetadataService() ) );
         app.activate();

         Module module = app.findModule( "Web", "REST" );
         module.objectBuilderFactory().newObjectBuilder( TestRestletApplication.class ).injectTo( this );

         Runtime.getRuntime().addShutdownHook( shutdownHook );

         super.start();
      } catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();

      app.passivate();

      Runtime.getRuntime().removeShutdownHook( shutdownHook );
   }
}