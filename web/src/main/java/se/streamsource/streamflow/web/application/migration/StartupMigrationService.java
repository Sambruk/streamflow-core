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

package se.streamsource.streamflow.web.application.migration;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.util.Function;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform migration of all data in the EntityStore
 */
@Mixins( StartupMigrationService.Mixin.class )
public interface StartupMigrationService
    extends Activatable, Configuration, ServiceComposite
{
    class Mixin
        implements Activatable
    {
        final Logger logger = LoggerFactory.getLogger( StartupMigrationService.class.getName() );
        @This
        Configuration<StartupMigrationConfiguration> config;

        @Structure
        Application application;

        @Service
        EntityStore entityStore;

        @Structure
        ModuleSPI module;

        public void activate()
            throws Exception
        {
            String lsv = config.configuration().lastStartupVersion().get();
            final int[] count = new int[]{ 0 };
            if( lsv != null && !lsv.equals( application.version() ) )
            {
                // Migrate all data eagerly
                logger.info( "Migrating data from version "+lsv+" to "+application.version() );

                // Do nothing - the EntityStore will do the migration on load
                entityStore.entityStates( module ).transferTo( Transforms.map( new Function<EntityState, EntityState>()
                {
                   public EntityState map( EntityState entityState )
                   {
                      count[ 0 ]++;

                      if( count[ 0 ] % 1000 == 0 )
                      {
                          logger.info( "Checked " + count[ 0 ] + " entities" );
                      }

                      return entityState;
                   }
                }, Outputs.<EntityState>noop() ));

                logger.info( "Migration finished. Checked " + count[ 0 ] + " entities" );
            }
            config.configuration().lastStartupVersion().set( application.version() );
            config.save();
        }

        public void passivate()
            throws Exception
        {
        }
    }
}
