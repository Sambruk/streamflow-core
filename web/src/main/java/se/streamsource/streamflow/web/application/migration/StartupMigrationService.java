/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.application.migration;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;

import java.util.logging.Logger;

/**
 * Perform migration of all data in the EntityStore
 */
@Mixins(StartupMigrationService.StartupMigrationMixin.class)
public interface StartupMigrationService
    extends Activatable, Configuration, ServiceComposite
{
    class StartupMigrationMixin
        implements Activatable
    {
        @This
        Configuration<StartupMigrationConfiguration> config;

        @Structure
        Application application;

        @Service
        EntityStore entityStore;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Module module;
        public Logger logger;

        public void activate() throws Exception
        {
            String lsv = config.configuration().lastStartupVersion().get();
            if (lsv != null && !lsv.equals(application.version()))
            {
                // Migrate all data eagerly
                logger = Logger.getLogger(StartupMigrationService.class.getName());
                logger.info("Migrating data to new version");
                final int[] count = new int[]{0};
                final Usecase usecase = UsecaseBuilder.newUsecase("Migrate data");
                final UnitOfWork[] uow = new UnitOfWork[]{uowf.newUnitOfWork(usecase)};

                entityStore.visitEntityStates(new EntityStore.EntityStateVisitor()
                {

                    public void visitEntityState(EntityState entityState)
                    {
                        try
                        {
                            // Do nothing - the EntityStore will do the migration on load
                            count[0]++;

                            uow[0].get(module.classLoader().loadClass(entityState.entityDescriptor().entityType().type().name()), entityState.identity().identity());

                            if (count[0] % 1000 == 0)
                            {
                                logger.info("Checked "+count[0]+" entities");
                                    uow[0].complete();
                                uow[0] = uowf.newUnitOfWork(usecase);
                            }
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, module);
                uow[0].complete();
                logger.info("Migration finished. Checked "+count[0]+" entities");
            }

            config.configuration().lastStartupVersion().set(application.version());
            config.save();
        }

        public void passivate() throws Exception
        {
        }
    }
}
