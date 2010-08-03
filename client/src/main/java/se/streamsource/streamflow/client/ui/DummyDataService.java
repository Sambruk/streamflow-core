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

package se.streamsource.streamflow.client.ui;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Uniform;
import org.restlet.resource.ClientResource;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.Individual;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
@Mixins(DummyDataService.Mixin.class)
public interface DummyDataService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      final Logger logger = LoggerFactory.getLogger( getClass().getName() );

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      IndividualRepository individualRepository;

      @Service
      Uniform client;

      @Structure
      Application app;

      public void activate() throws Exception
      {
         if (!app.mode().equals( Application.Mode.development ))
            return;

         try
         {
            UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Create account" ) );

            Individual individual = individualRepository.individual();

            ValueBuilder<AccountSettingsValue> builder = vbf.newValueBuilder( AccountSettingsValue.class );
            builder.prototype().name().set( "Test server administrator" );
            builder.prototype().server().set( "http://localhost:8040/streamflow" );
            builder.prototype().userName().set( "administrator" );
            builder.prototype().password().set( "administrator" );

            final Account account = individual.newAccount(builder.newInstance());


            builder.prototype().name().set( "Test server someuser" );
            builder.prototype().userName().set("someuser");
            builder.prototype().password().set("someuser");
            individual.newAccount( builder.newInstance() );

            CommandQueryClient server = account.server( client );
            ClientResource version = new ClientResource(server.getReference().clone().addSegment( "static" ).addSegment( "version.html" ));
            version.setNext( server.getClient() );

            String response = account.version( client );
            System.out.println( response );
            CommandQueryClient user = account.server( client ).getSubClient( "users" ).getSubClient( "administrator" );
            System.out.println( user.getSubClient( "workspace").getSubClient( "user" ).getSubClient( "drafts" ).query("cases", LinksValue.class ).links().get().size() );

            uow.complete();

         } catch (Exception e)
         {
            logger.warn( "Could not create dummy account" );
            e.printStackTrace();
         }
      }

      public void passivate() throws Exception
      {
      }

   }

}