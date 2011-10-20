package se.streamsource.streamflow.web.management;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.migration.assembly.MigrationRule;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public interface UpdateService
      extends ServiceComposite, Configuration<UpdateConfiguration>
{
   class Mixin
         implements Activatable
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
         if (!app.version().equals(lastVersion))
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
