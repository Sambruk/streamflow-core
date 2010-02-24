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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * Model for a contact of a task
 */
public class TaskContactModel
{
   @Uses
   private ContactValue contact;

   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;


   public ContactValue getContact()
   {
      return contact;
   }

   public ContactPhoneValue getPhoneNumber()
   {
      return contact.phoneNumbers().get().get( 0 );
   }

   public ContactAddressValue getAddress()
   {
      return contact.addresses().get().get( 0 );
   }

   public ContactEmailValue getEmailAddress()
   {
      return contact.emailAddresses().get().get( 0 );
   }

   public void changeName( String newName ) throws ResourceException
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newName );
      client.putCommand( "changename", builder.newInstance() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newNote );
      client.putCommand( "changenote", builder.newInstance() );
   }

   public void changeContactId( String newId ) throws ResourceException
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newId );
      client.putCommand("changecontactid", builder.newInstance() );
   }

   public void changeCompany( String newCompany ) throws ResourceException
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newCompany );
      client.putCommand( "changecompany", builder.newInstance() );
   }

   public void changePhoneNumber( String newPhoneNumber ) throws ResourceException
   {
      ValueBuilder<ContactPhoneValue> builder = vbf.newValueBuilder( ContactPhoneValue.class );
      builder.prototype().phoneNumber().set( newPhoneNumber );
      client.putCommand( "changephonenumber", builder.newInstance() );
   }

   public void changeAddress( String newAddress ) throws ResourceException
   {
      ValueBuilder<ContactAddressValue> builder = vbf.newValueBuilder( ContactAddressValue.class );
      builder.prototype().address().set( newAddress );
      client.putCommand( "changeaddress", builder.newInstance() );
   }

   public void changeEmailAddress( String newEmailAddress ) throws ResourceException
   {
      ValueBuilder<ContactEmailValue> builder = vbf.newValueBuilder( ContactEmailValue.class );
      builder.prototype().emailAddress().set( newEmailAddress );
      client.putCommand( "changeemailaddress", builder.newInstance() );
   }

}