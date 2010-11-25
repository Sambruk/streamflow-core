/*
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

package se.streamsource.dci.restlet.client;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.Uniform;
import org.restlet.data.Reference;

/**
 * Builder for CommandQueryClient
 */
public class CommandQueryClientFactory
{
   @Structure
   private Qi4jSPI spi;

   @Structure
   private Module module;

   private ClientCache cache ;

   @Uses
   @Optional
   private ResponseHandler handler = new NullResponseHandler();

   @Uses
   private Uniform client;

   public void setCache( @Uses @Optional ClientCache cache )
   {
      this.cache = cache;
      if (cache == null)
         this.cache = new ClientCache();
   }

   public CommandQueryClient newClient( Reference reference )
   {
      return module.objectBuilderFactory().newObjectBuilder( CommandQueryClient.class ).use( this, reference ).newInstance();
   }

   ResponseHandler getHandler()
   {
      return handler;
   }

   ClientCache getCache()
   {
      return cache;
   }

   Uniform getClient()
   {
      return client;
   }

   <T extends ValueComposite> T newValue( Class<T> queryResult, String jsonValue )
   {
      return module.valueBuilderFactory().newValueFromJSON( queryResult, jsonValue);
   }

   Qi4jSPI getSPI()
   {
      return spi;
   }
}
