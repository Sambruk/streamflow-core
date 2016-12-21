/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.users;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specifications;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import java.io.File;

import static org.qi4j.api.specification.Specifications.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class UsersAdministrationListModel
      extends LinkValueListModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   public UsersAdministrationListModel()
   {
      relationModelMapping("user", UserAdministrationDetailModel.class);
   }


   public void importUsers( File f )
   {
      MediaType type = f.getName().endsWith( ".xls" )
            ? MediaType.APPLICATION_EXCEL
            : MediaType.TEXT_CSV;

      Representation representation = new FileRepresentation( f, type );

      client.postCommand( "importusers", representation );
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


   @Override
   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( 
            or( 
                  onEntities( client.getReference().getParentRef().getLastSegment() ), 
                  onEntities( client.getReference().getLastSegment() ),
                  onEntities( "users" ),
                  Specifications.and( 
                        Events.withNames( "createdUser", "changedDescription", "changedEnabled", "leftOrganization", "joinedOrganization" ), 
                        onEntities( linkValues )
                  )
             ), 
          transactions ))
         refresh();
   }
}
