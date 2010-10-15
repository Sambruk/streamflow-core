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

package se.streamsource.streamflow.web.application.security;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

/**
 * Factory for Authentication filters.
 */
@Mixins(AuthenticationFilterFactoryService.Mixin.class)
public interface AuthenticationFilterFactoryService
      extends AuthenticationFilterFactory, ServiceComposite, Configuration, Activatable
{
   abstract class Mixin
         implements AuthenticationFilterFactoryService
   {

      @This
      Configuration<PluginConfiguration> config;

      @Service
      CachingService cachingService;

      Caching caching;

      @Structure
      ObjectBuilderFactory obf;

      public void activate() throws Exception
      {
         config.configuration();

         caching = new Caching( cachingService, Caches.VERIFIEDUSERS );
      }

      public void passivate() throws Exception
      {

      }

      public Filter createFilter( Context context, Restlet next )
      {
         return obf.newObjectBuilder( AuthenticationFilter.class ).use( context, next, caching, config ).newInstance();
      }
   }
}
