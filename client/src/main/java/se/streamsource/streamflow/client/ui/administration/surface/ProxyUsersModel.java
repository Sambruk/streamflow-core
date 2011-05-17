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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.NewProxyUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserListDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

public class ProxyUsersModel
      implements Refreshable, TransactionListener
{
   @Structure
   ValueBuilderFactory vbf;

   private EventList<ProxyUserDTO> eventList = new BasicEventList<ProxyUserDTO>();

   @Uses
   private CommandQueryClient client;

   public EventList<ProxyUserDTO> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      final ProxyUserListDTO proxyUsers = client.query( "index", ProxyUserListDTO.class );
      EventListSynch.synchronize( proxyUsers.users().get(), eventList );
   }

   public void createProxyUser( NewProxyUserDTO proxyUserDTO)
   {
      try
      {
         client.postCommand( "createproxyuser", proxyUserDTO);
      } catch (ResourceException e)
      {
         ErrorResources resources = ErrorResources.valueOf(e.getMessage());
         throw new OperationException( resources, e );
      }
   }

   public void changeEnabled( ProxyUserDTO proxyUser, boolean enabled )
   {
      Form form = new Form();
      form.set("enabled", Boolean.toString(enabled));
      client.getSubClient( proxyUser.username().get() ).postCommand( "changeenabled", form.getWebRepresentation() );
   }

   public void resetPassword( ProxyUserDTO proxyUser, String password )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( password );

      client.getSubClient( proxyUser.username().get() ).postCommand( "resetpassword", builder.newInstance() );
   }

   public void remove( ProxyUserDTO proxyUser )
   {
      client.getSubClient( proxyUser.username().get() ).delete();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      refresh();
   }
}