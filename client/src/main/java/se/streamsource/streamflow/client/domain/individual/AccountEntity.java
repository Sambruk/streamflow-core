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

package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins({AccountEntity.Mixin.class})
public interface AccountEntity
      extends Account, EntityComposite
{
   interface Data
         extends Describable
   {
      // Settings

      Property<AccountSettingsValue> settings();

   }

   class Mixin
         implements AccountSettings, AccountConnection
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      ObjectBuilderFactory obf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Account account;

      @This
      Data state;

      @This
      Describable description;

      @Service
      IndividualRepository repo;

      // AccountSettings

      public AccountSettingsValue accountSettings()
      {
         return state.settings().get();
      }

      public void updateSettings( AccountSettingsValue newAccountSettings )
      {
         state.settings().set( newAccountSettings );
         description.changeDescription( newAccountSettings.name().get() );
      }

      public void changePassword( Uniform client, ChangePasswordCommand changePassword ) throws ResourceException
      {
         user( client ).postCommand( "changePassword", changePassword );

         AccountSettingsValue settings = state.settings().get().<AccountSettingsValue>buildWith().prototype();
         settings.password().set( changePassword.newPassword().get() );

         updateSettings( settings );
      }

      // AccountConnection

      public CommandQueryClient server( Uniform client )
      {
         AccountSettingsValue settings = accountSettings();
         Reference serverRef = new Reference( settings.server().get() );
         serverRef.addSegment( "streamflow" ).addSegment( "v1" ).addSegment( "" );

         AuthenticationFilter filter = new AuthenticationFilter( uowf, account );
         filter.setNext( (Restlet) client );

         CommandQueryClient cqc = obf.newObjectBuilder( CommandQueryClient.class ).use( filter, serverRef ).newInstance();

         return cqc;
      }

      public CommandQueryClient user( Uniform client )
      {
         return server( client ).getSubClient( "users" ).getSubClient( accountSettings().userName().get() );
      }

      public String version(Uniform client) throws ResourceException, IOException
      {
         CommandQueryClient server = server( client );
         ClientResource version = new ClientResource( server.getReference().clone().addSegment( "static" ).addSegment( "version.html" ));
         version.setNext( server.getClient() );

         String response = version.get().getText();
         return response;
      }
   }

   class AuthenticationFilter extends Filter
   {
      private UnitOfWorkFactory uowf;
      private AccountSettings account;

      public AuthenticationFilter( UnitOfWorkFactory uowf, AccountSettings account )
      {
         this.uowf = uowf;
         this.account = account;
      }

      @Override
      protected int beforeHandle( Request request, Response response )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         AccountSettingsValue settings;
         if (uow == null)
         {
            uow = uowf.newUnitOfWork();
            settings = uow.get( account ).accountSettings();
            uow.discard();
         } else
         {
            settings = uow.get( account ).accountSettings();
         }

         request.setChallengeResponse( new ChallengeResponse( ChallengeScheme.HTTP_BASIC, settings.userName().get(), settings.password().get() ) );

         return super.beforeHandle( request, response );
      }
   }
}
