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

package se.streamsource.streamflow.web.configuration;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.entitystore.jdbm.*;
import org.qi4j.library.rdf.repository.*;
import se.streamsource.streamflow.infrastructure.configuration.*;

import java.io.*;

/**
 * Initial configurations for all services
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
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Service configuration" ) );
         String jdbmPath = new File( config.dataDirectory(), "data/streamflow.data" ).getAbsolutePath();
         try
         {
            uow.get( JdbmConfiguration.class, "data" );
         } catch (NoSuchEntityException e)
         {
            EntityBuilder<JdbmConfiguration> builder = uow.newEntityBuilder( JdbmConfiguration.class, "data" );
            builder.instance().file().set( jdbmPath );
            builder.newInstance();
         }

         try
         {
            uow.get( NativeConfiguration.class, "rdf-repository" );
         } catch (NoSuchEntityException e)
         {
            String rdfPath = new File( config.dataDirectory(), "rdf-repository" ).getAbsolutePath();
            EntityBuilder<NativeConfiguration> builder = uow.newEntityBuilder( NativeConfiguration.class, "rdf-repository" );
            builder.instance().dataDirectory().set( rdfPath );
            builder.instance().tripleIndexes().set( "spoc,cspo,ospc" );
            builder.newInstance();
         }

         uow.complete();
      }

      public void passivate() throws Exception
      {
      }
   }
}
