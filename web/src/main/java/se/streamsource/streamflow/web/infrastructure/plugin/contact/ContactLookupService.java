/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.infrastructure.plugin.contact;

import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

      Logger log = LoggerFactory.getLogger( ContactLookupService.class );

      public void activate() throws Exception
      {
         config.configuration();
      }

      public void passivate() throws Exception
      {
      }

      public ContactList lookup( ContactValue contactTemplate )
      {
         try
         {
            ClientResource clientResource = new ClientResource(config.configuration().url().get());

            StringRepresentation post = new StringRepresentation( contactTemplate.toJSON(), MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8 );

            // Call plugin
            Representation result = clientResource.post( post );

            // Parse response
            String json = result.getText();
            return vbf.newValueFromJSON( ContactList.class, json );
         } catch (Exception e)
         {
            log.error( "Could not get contacts from plugin", e );

            // Return empty list
            return vbf.newValue( ContactList.class );
         }
      }
   }
}
