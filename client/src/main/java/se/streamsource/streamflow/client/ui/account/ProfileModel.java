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

package se.streamsource.streamflow.client.ui.account;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;

import java.util.*;

/**
 * JAVADOC
 */
public class ProfileModel
      extends Observable
   implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   private ContactDTO contact;

   public void changeMessageDeliveryType( String newDeliveryType )
         throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf
            .newValueBuilder( StringValue.class );
      builder.prototype().string().set( newDeliveryType );
      client.putCommand( "changemessagedeliverytype", builder.newInstance() );
   }

   public String getMessageDeliveryType()
   {
      return client.query( "messagedeliverytype", StringValue.class )
            .string().get();
   }

   // Contact Details

   public void refresh()
   {
      contact = client.query("index", ContactDTO.class ).<ContactDTO>buildWith().prototype();
      setChanged();
      notifyObservers();
   }

   public ContactDTO getContact()
   {
      return contact;
   }

   public ContactPhoneDTO getPhoneNumber()
   {
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneDTO phone = vbf.newValue( ContactPhoneDTO.class )
               .<ContactPhoneDTO>buildWith().prototype();
         contact.phoneNumbers().get().add( phone );
      }
      return contact.phoneNumbers().get().get( 0 );
   }

   public ContactEmailDTO getEmailAddress()
   {
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailDTO email = vbf.newValue( ContactEmailDTO.class )
               .<ContactEmailDTO>buildWith().prototype();
         contact.emailAddresses().get().add( email );
      }
      return contact.emailAddresses().get().get( 0 );
   }

   public void changeName( String newName )
   {
      ValueBuilder<StringValue> builder = vbf
            .newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );
      client.putCommand( "changename", builder.newInstance() );
   }

   public void changePhoneNumber( String newPhoneNumber )
   {
      ValueBuilder<ContactPhoneDTO> builder = vbf
            .newValueBuilder( ContactPhoneDTO.class );
      builder.prototype().phoneNumber().set( newPhoneNumber );
      client.putCommand( "changephonenumber", builder.newInstance() );
   }

   public void changeEmailAddress( String newEmailAddress )
   {
      ValueBuilder<ContactEmailDTO> builder = vbf
            .newValueBuilder( ContactEmailDTO.class );
      builder.prototype().emailAddress().set( newEmailAddress );
      client.putCommand( "changeemailaddress", builder.newInstance() );
   }
}
