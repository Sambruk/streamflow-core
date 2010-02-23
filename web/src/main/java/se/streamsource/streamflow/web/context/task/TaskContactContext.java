/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.task.Contacts;
import se.streamsource.streamflow.dci.infrastructure.web.context.Context;
import se.streamsource.streamflow.dci.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.dci.infrastructure.web.context.DeleteContext;

/**
 * JAVADOC
 */
@Mixins(TaskContactContext.Mixin.class)
public interface TaskContactContext
   extends DeleteContext, Context
{
   public void changename( StringDTO name );
   public void changenote( StringDTO note );
   public void changecontactid( StringDTO contactId );
   public void changecompany( StringDTO company );
   public void changephonenumber( ContactPhoneValue phoneValue );
   public void changeaddress( ContactAddressValue addressValue );
   public void changeemailaddress( ContactEmailValue emailValue );

   abstract class Mixin
      extends ContextMixin
      implements TaskContactContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public void delete()
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);

         contacts.deleteContact( index );
      }

      public void changename( StringDTO name )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();
         builder.prototype().name().set( name.string().get() );
         contacts.updateContact( index, builder.newInstance() );
      }

      public void changenote( StringDTO note )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();
         builder.prototype().note().set( note.string().get() );
         contacts.updateContact( index, builder.newInstance() );
      }

      public void changecontactid( StringDTO contactId )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();
         builder.prototype().contactId().set( contactId.string().get() );
         contacts.updateContact( index, builder.newInstance() );
      }

      public void changecompany( StringDTO company )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();
         builder.prototype().company().set( company.string().get() );
         contacts.updateContact( index, builder.newInstance() );
      }

      public void changephonenumber( ContactPhoneValue phoneValue )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();

         // Create an empty phone value if it doesnt exist already
         if (contact.phoneNumbers().get().isEmpty())
         {
            ContactPhoneValue phone = vbf.newValue( ContactPhoneValue.class ).<ContactPhoneValue>buildWith().prototype();
            phone.phoneNumber().set( phoneValue.phoneNumber().get() );
            builder.prototype().phoneNumbers().get().add( phone );
         } else
         {
            builder.prototype().phoneNumbers().get().get( 0 ).phoneNumber().set( phoneValue.phoneNumber().get() );
         }

         contacts.updateContact( index, builder.newInstance() );
      }

      public void changeaddress( ContactAddressValue addressValue )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();

         // Create an empty address value if it doesnt exist already
         if (contact.addresses().get().isEmpty())
         {
            ContactAddressValue address = vbf.newValue( ContactAddressValue.class ).<ContactAddressValue>buildWith().prototype();
            address.address().set( addressValue.address().get() );
            builder.prototype().addresses().get().add( address );
         } else
         {
            builder.prototype().addresses().get().get( 0 ).address().set( addressValue.address().get() );
         }

         contacts.updateContact( index, builder.newInstance() );
      }

      public void changeemailaddress( ContactEmailValue emailValue )
      {
         Contacts contacts = context.role(Contacts.class);
         Integer index = context.role(Integer.class);
         ContactValue contact = context.role(ContactValue.class);

         ValueBuilder<ContactValue> builder = contact.buildWith();

         // Create an empty email value if it doesnt exist already
         if (contact.emailAddresses().get().isEmpty())
         {
            ContactEmailValue email = vbf.newValue( ContactEmailValue.class ).<ContactEmailValue>buildWith().prototype();
            email.emailAddress().set( emailValue.emailAddress().get() );
            builder.prototype().emailAddresses().get().add( email );
         } else
         {
            builder.prototype().emailAddresses().get().get( 0 ).emailAddress().set( emailValue.emailAddress().get() );
         }


         contacts.updateContact( index, builder.newInstance() );
      }
   }
}
