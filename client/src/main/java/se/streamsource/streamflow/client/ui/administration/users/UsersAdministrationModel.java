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

package se.streamsource.streamflow.client.ui.administration.users;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityValue;

import java.io.File;

public class UsersAdministrationModel
      implements Refreshable, TransactionListener
{
   @Structure
   ValueBuilderFactory vbf;

   private EventList<UserEntityValue> eventList = new BasicEventList<UserEntityValue>();

   private CommandQueryClient client;

   public UsersAdministrationModel( @Uses CommandQueryClient client ) throws ResourceException
   {
      this.client = client;
   }

   public EventList<UserEntityValue> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "index", LinksValue.class ).links().get(), eventList );
   }

   public void createUser( NewUserCommand userCommand )
   {
      try
      {
         client.postCommand( "createuser", userCommand );
      } catch (ResourceException e)
      {
         ErrorResources resources = ErrorResources.valueOf( e.getMessage() );
         throw new OperationException( resources, e );
      }
   }

   public void changeDisabled( UserEntityValue user )
   {
      client.getClient( user ).postCommand( "changedisabled" );
   }

   public void importUsers( File f )
   {
      MediaType type = f.getName().endsWith( ".xls" )
            ? MediaType.APPLICATION_EXCEL
            : MediaType.TEXT_CSV;

      Representation representation = new FileRepresentation( f, type );

      client.postCommand( "importusers", representation );
   }

   public void resetPassword( UserEntityValue user, String password )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( password );

      client.getClient( user ).putCommand( "resetpassword", builder.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames( "createdUser", "changedEnabled" ), transactions ))
         refresh();
   }
}
