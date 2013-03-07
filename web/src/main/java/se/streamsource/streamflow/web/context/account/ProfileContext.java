/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.interaction.profile.UserProfileDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.domain.interaction.profile.MarkReadTimeout;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

/**
 * JAVADOC
 */
public class ProfileContext
   implements IndexContext<UserProfileDTO>
{
   @Service
   SystemDefaultsService systemDefaults;

   @Structure
   Module module;

   @Structure
   private Qi4jSPI spi;

   @Uses MessageRecipient recipient;
   @Uses MessageRecipient.Data recipientData;
   @Uses
   MarkReadTimeout markReadTimeout;

   public void changemessagedeliverytype( @Name("messagedeliverytype") MessageRecipient.MessageDeliveryTypes newDeliveryType )
   {
      recipient.changeMessageDeliveryType( newDeliveryType );
   }

   public void changemarkreadtimeout( @Name("markreadtimeoutsec") String timeoutsec )
   {
      markReadTimeout.changeTimeout( new Long( timeoutsec ) );
   }

   public UserProfileDTO index()
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      final ContactDTO contact = contactable.getContact();

      MarkReadTimeout.Data markReadTimeoutData = RoleMap.role( MarkReadTimeout.Data.class );


      ValueBuilder<UserProfileDTO> builder = module.valueBuilderFactory().newValueBuilder( UserProfileDTO.class );
      final ValueDescriptor descriptor = spi.getValueDescriptor( builder.newInstance() );
      builder.withState( new StateHolder()
      {

         public <T> Property<T> getProperty( QualifiedName name )
         {
            return null;
         }

         public <T> Property<T> getProperty( java.lang.reflect.Method propertyMethod )
         {
            return null;
         }

         public <ThrowableType extends Throwable> void visitProperties( StateVisitor<ThrowableType> visitor )
               throws ThrowableType
         {
            for (PropertyDescriptor propertyDescriptor : descriptor.state().properties())
            {
               Property property = contact.state().getProperty( propertyDescriptor.qualifiedName() );
               if( property != null )
                  visitor.visitProperty( propertyDescriptor.qualifiedName(), property.get() );
            }
         }
      } );



      Long systemDefault = systemDefaults.config().configuration().defaultMarkReadTimeout().get();
      Long timeout = systemDefault.compareTo( markReadTimeoutData.timeout().get() ) > 0 ? systemDefault : markReadTimeoutData.timeout().get();

      builder.prototype().markReadTimeout().set( timeout );
      builder.prototype().messageDeliveryType().set( recipientData.delivery().get().name() );

      return builder.newInstance();
   }

   public void update(@Optional @Name("name") String name,
                      @Optional @Name("contactId") String contactId,
                      @Optional @Name("company") String company,
                      @Optional @Name("iscompany") Boolean isCompany,
                      @Optional @Name("phone") String phone,
                      @Optional @Name("email") String email,
                      @Optional @Name("address") String address,
                      @Optional @Name("zipCode") String zip,
                      @Optional @Name("city") String city,
                      @Optional @Name("region") String region,
                      @Optional @Name("country") String country,
                      @Optional @Name("note") String note)
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ContactBuilder builder = new ContactBuilder(contact, module.valueBuilderFactory());

      if (name != null)
         builder.name(name );
      if (contactId != null)
         builder.contactId(contactId);
      if (company != null)
         builder.company(company);
      if (isCompany != null)
         builder.isCompany(isCompany);
      if (phone != null)
         builder.phoneNumber(phone);
      if (email != null)
         builder.email(email);
      if (address != null)
         builder.address(address);
      if (zip != null)
         builder.zipCode(zip);
      if (city != null)
         builder.city(city);
      if (region != null)
         builder.region(region);
      if (country != null)
         builder.country(country);
      if (note != null)
         builder.note(note);

      contactable.updateContact( builder.newInstance() );
   }
}
