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

package se.streamsource.streamflow.web.infrastructure.plugin.address;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.restlet.client.CommandQueryClientFactory;
import se.streamsource.dci.restlet.client.NullResponseHandler;
import se.streamsource.streamflow.server.plugin.address.StreetAddressLookup;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

/**
 * Service that looks up street addresses in a REST plugin
 */
@Mixins(StreetAddressLookupService.Mixin.class)
public interface StreetAddressLookupService
      extends ServiceComposite, StreetAddressLookup, Configuration, Activatable
{
   class Mixin
         implements StreetAddressLookup, Activatable
   {
      @This
      Configuration<PluginConfiguration> config;

      @Structure
      Module module;

      private CommandQueryClient cqc;

      Logger log = LoggerFactory.getLogger( StreetAddressLookupService.class );

      public void activate() throws Exception
      {
         config.configuration();

         if (config.configuration().enabled().get())
         {
            Reference serverRef = new Reference( config.configuration().url().get() );
            Client client = new Client( Protocol.HTTP );
            client.start();

            cqc = module.objectBuilderFactory().newObjectBuilder(CommandQueryClientFactory.class).use( client, new NullResponseHandler() ).newInstance().newClient( serverRef );
         }
      }

      public void passivate() throws Exception
      {
      }

      public StreetList lookup(StreetValue streetTemplate)
      {
         try
         {
            return cqc.query( config.configuration().url().get(), StreetList.class, streetTemplate);
         } catch (Exception e)
         {
            log.error( "Could not get contacts from plugin", e );

            // Return empty list
            return module.valueBuilderFactory().newValue(StreetList.class);
         }
      }
   }
}
