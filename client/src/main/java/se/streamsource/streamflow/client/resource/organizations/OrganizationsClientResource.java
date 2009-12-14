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

package se.streamsource.streamflow.client.resource.organizations;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.ResetPasswordCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;

/**
 * JAVADOC
 */
public class OrganizationsClientResource
      extends CommandQueryClientResource
{
   public OrganizationsClientResource( @Uses Context context, @Uses Reference reference )
   {
      super( context, reference );
   }

   public OrganizationClientResource organization( String orgid ) throws ResourceException
   {
      return getSubResource( orgid, OrganizationClientResource.class );
   }

   public UserEntityListDTO users() throws ResourceException
   {
      return query( "users", UserEntityListDTO.class );
   }

   public void createUser( String username, String password ) throws ResourceException
   {
      ValueBuilder<NewUserCommand> builder = vbf.newValueBuilder( NewUserCommand.class );
      builder.prototype().username().set( username );
      builder.prototype().password().set( password );

      postCommand( "createUser", builder.newInstance() );
   }

   public void changeDisabled( UserEntityDTO user ) throws ResourceException
   {
      postCommand( "changeDisabled", user );
   }


   public void importUsers( Representation representation ) throws ResourceException
   {
      postCommand( "importUsers", representation );
   }

   public ListValue organizations() throws ResourceException
   {
      return query( "organizations", ListValue.class );
   }

   public void resetPassword( EntityReference userentity, String password ) throws ResourceException
   {
      ValueBuilder<ResetPasswordCommand> builder = vbf.newValueBuilder( ResetPasswordCommand.class );
      builder.prototype().entity().set( userentity );
      builder.prototype().password().set( password );

      putCommand( "resetPassword", builder.newInstance() );
   }
}