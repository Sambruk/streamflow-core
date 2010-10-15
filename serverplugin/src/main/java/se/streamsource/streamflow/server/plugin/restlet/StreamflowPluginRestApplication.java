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

package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.server.plugin.authentication.LdapAuthenticationVerifier;

/**
 * Application for Streamflow SPI Plugin implementations.
 */
public class StreamflowPluginRestApplication
      extends Application
{
   public static final MediaType APPLICATION_SPARQL_JSON = new MediaType( "application/sparql-results+json", "SPARQL JSON" );

   final Logger logger = LoggerFactory.getLogger( "plugin" );

   ApplicationSPI app;

   private Assembler assembler;

   public StreamflowPluginRestApplication( Context parentContext, Assembler assembler ) throws Exception
   {
      super( parentContext );
      this.assembler = assembler;

      getMetadataService().addExtension( "srj", APPLICATION_SPARQL_JSON );
   }

   /**
    * Creates a root Restlet that will receive all incoming calls.
    */
   @Override
   public Restlet createInboundRoot()
   {
      Router pluginRouter = new Router( getContext() );

      pluginRouter.attach( "/contacts", app.findModule( "Web", "REST" ).objectBuilderFactory().newObject( ContactLookupRestlet.class ), Template.MODE_STARTS_WITH );

      ChallengeAuthenticator ldapGuard = new ChallengeAuthenticator( getContext(), ChallengeScheme.HTTP_BASIC, "Ldap" );
      ldapGuard.setVerifier( (Verifier) app.findModule( "Web", "REST" ).objectBuilderFactory().newObject( LdapAuthenticationVerifier.class ) );
      ldapGuard.setNext( (Restlet) app.findModule( "Web", "REST" ).objectBuilderFactory().newObject( AuthenticationRestlet.class ) );
      pluginRouter.attach( "/authentication", ldapGuard, Template.MODE_STARTS_WITH );

      return pluginRouter;
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
            app = is.newApplication( new PluginApplicationAssembler( assembler ) );

            app.activate();

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

         logger.info( "Passivating Streamflow plugins" );
         app.passivate();
      }
   }
}
