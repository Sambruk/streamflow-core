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

package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.object.*;
import org.qi4j.api.property.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.resource.user.*;

import java.io.*;

/**
 * Entity representing a client-side account
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

      @Service
      ResponseHandler handler;

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
         server( client ).getSubClient( "account" ).postCommand( "changepassword", changePassword );

         AccountSettingsValue settings = state.settings().get().<AccountSettingsValue>buildWith().prototype();
         settings.password().set( changePassword.newPassword().get() );

         updateSettings( settings );
      }

      // AccountConnection
      public CommandQueryClient server( Uniform client )
      {
         AccountSettingsValue settings = accountSettings();
         Reference serverRef = new Reference( settings.server().get() );
         serverRef.setPath( "/streamflow/" );

         AuthenticationFilter filter = new AuthenticationFilter( uowf, account, client );

         return obf.newObjectBuilder( CommandQueryClientFactory.class ).use( filter, handler ).newInstance().newClient( serverRef );
      }

      public CommandQueryClient user( Uniform client )
      {
         return server( client ).getSubClient( "users" ).getSubClient( accountSettings().userName().get() );
      }

      public String version(Uniform client) throws ResourceException, IOException
      {
         CommandQueryClient server = server( client );
         Representation in = server.getClient( "/streamflow/static/" ).queryRepresentation( "version.html", null);

         String version = in.getText();
         return version;
      }
   }

   class AuthenticationFilter
      implements Uniform
   {
      private UnitOfWorkFactory uowf;
      private AccountSettings account;
      private final Uniform next;

      public AuthenticationFilter( UnitOfWorkFactory uowf, AccountSettings account, Uniform next )
      {
         this.uowf = uowf;
         this.account = account;
         this.next = next;
      }

      public void handle( Request request, Response response )
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

         next.handle( request, response );
      }
   }
}
