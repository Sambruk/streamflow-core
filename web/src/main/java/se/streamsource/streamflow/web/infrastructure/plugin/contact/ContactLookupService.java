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

package se.streamsource.streamflow.web.infrastructure.plugin.contact;

import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.object.*;
import org.qi4j.api.service.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.slf4j.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.server.plugin.contact.*;
import se.streamsource.streamflow.web.infrastructure.plugin.*;

/**
 * Service that looks up contacts in a REST plugin
 */
@Mixins(ContactLookupService.Mixin.class)
public interface ContactLookupService
      extends ServiceComposite, ContactLookup, Configuration, Activatable
{
   class Mixin
         implements ContactLookup, Activatable
   {
      @This
      Configuration<PluginConfiguration> config;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      private ObjectBuilderFactory obf;

      private CommandQueryClient cqc;

      Logger log = LoggerFactory.getLogger( ContactLookupService.class );

      public void activate() throws Exception
      {
         config.configuration();

         if (config.configuration().enabled().get())
         {
            Reference serverRef = new Reference( config.configuration().url().get() );
            Client client = new Client( Protocol.HTTP );
            client.start();

            cqc = obf.newObjectBuilder( CommandQueryClientFactory.class).use( client, new NullResponseHandler() ).newInstance().newClient( serverRef );
         }
      }

      public void passivate() throws Exception
      {
      }

      public ContactList lookup( ContactValue contactTemplate )
      {
         try
         {
            return cqc.query( config.configuration().url().get(), contactTemplate, ContactList.class );
//         ClientResource clientResource = new ClientResource( config.configuration().url().get() );
//
//         setQueryParameters( clientResource.getReference(), contactTemplate );
//         // Call plugin
//         Representation result = clientResource.get();
//
//         // Parse response
//         String json = result.getText();
//         return vbf.newValueFromJSON( ContactList.class, json );
         } catch (Exception e)
         {
            log.error( "Could not get contacts from plugin", e );

            // Return empty list
            return vbf.newValue( ContactList.class );
         }
      }
   }
}
