/**
 *
 * Copyright 2009-2012 Streamsource AB
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
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.api.administration.UserEntityDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import java.io.File;

public class UsersAdministrationModel
      implements Refreshable, TransactionListener
{
   @Structure
   Module module;

   private EventList<UserEntityDTO> eventList = new BasicEventList<UserEntityDTO>();

   private CommandQueryClient client;

   public UsersAdministrationModel( @Uses CommandQueryClient client ) throws ResourceException
   {
      this.client = client;
   }

   public EventList<UserEntityDTO> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "index", LinksValue.class ).links().get(), eventList );
   }

   public void createUser( NewUserDTO userDTO)
   {
      try
      {
         client.postCommand( "createuser", userDTO);
      } catch (ResourceException e)
      {
         ErrorResources resources = ErrorResources.valueOf( e.getMessage() );
         throw new OperationException( resources, e );
      }
   }

   public void changeDisabled( UserEntityDTO user )
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

   public void resetPassword( UserEntityDTO user, String password )
   {
      Form form = new Form();
      form.set("password", password);
      client.getClient( user ).putCommand( "resetpassword", form );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "createdUser", "changedEnabled" ), transactions ))
         refresh();
   }
}
