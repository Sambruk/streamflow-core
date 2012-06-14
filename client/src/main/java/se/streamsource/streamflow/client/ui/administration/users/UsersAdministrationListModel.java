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

import org.qi4j.api.injection.scope.Uses;
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

import java.io.File;

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

}
