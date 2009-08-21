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

package se.streamsource.streamflow.web.infrastructure.configuration;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

/**
 * Configure all services
 */
@Mixins(ServiceConfiguration.ServiceConfigurationMixin.class)
public interface ServiceConfiguration
        extends ServiceComposite, Activatable
{
    class ServiceConfigurationMixin
            implements Activatable
    {
        @Service
        FileConfiguration config;

        @Structure
        UnitOfWorkFactory uowf;

        public void activate() throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Service configuration"));
            String jdbmPath = new File(config.dataDirectory(), "JdbmEntityStoreService/jdbm.data").getAbsolutePath();
            try
            {
                uow.get(JdbmConfiguration.class, "JdbmEntityStoreService");
            } catch (NoSuchEntityException e)
            {
                EntityBuilder<JdbmConfiguration> builder = uow.newEntityBuilder(JdbmConfiguration.class, "JdbmEntityStoreService");
                builder.instance().file().set(jdbmPath);
                builder.newInstance();
            }

            try
            {
                uow.get(NativeConfiguration.class, "rdf-repository");
            } catch (NoSuchEntityException e)
            {
                String rdfPath = new File(config.dataDirectory(), "rdf-repository").getAbsolutePath();
                NativeConfiguration nativeConfiguration = uow.newEntity(NativeConfiguration.class, "rdf-repository");
                nativeConfiguration.dataDirectory().set(rdfPath);
                nativeConfiguration.tripleIndexes().set("spoc,cspo,ospc");
            }

            uow.complete();
        }

        public void passivate() throws Exception
        {
        }
    }
}
