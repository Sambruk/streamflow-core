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
package se.streamsource.streamflow.web.management;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
@Mixins(UpdateService.Mixin.class)
public interface UpdateService
      extends ServiceComposite, Configuration, Activatable
{
   abstract class Mixin
         implements UpdateService, Activatable
   {
      @Structure
      Application app;

      @Structure
      Module module;

      @This
      Configuration<UpdateConfiguration> config;

      @Uses
      ServiceDescriptor descriptor;

      Logger log;

      public void activate() throws Exception
      {
         UpdateBuilder builder = descriptor.metaInfo( UpdateBuilder.class );

         log = LoggerFactory.getLogger(UpdateService.class);

         String version = app.version();
         String lastVersion = config.configuration().lastStartupVersion().get();

         // Run general rules if version has changed
         if (!version.equals(lastVersion))
         {
            Iterable<UpdateRule> rules = builder.getRules().getRules(lastVersion, version);
            try
            {
               if (rules != null)
               {
                  for (UpdateRule rule : rules)
                  {
                     rule.getOperation().update(app, module);
                     log.debug(rule.toString());
                  }

                  log.info("Updated to " + version);
               }

               config.configuration().lastStartupVersion().set(version);
               config.save();
            } catch (Exception e)
            {
               log.error("Update failed. Aborting! Try fixing the problem and start again", e);

               throw e;
            }
         }
      }

      public void passivate() throws Exception
      {
      }
   }
}
