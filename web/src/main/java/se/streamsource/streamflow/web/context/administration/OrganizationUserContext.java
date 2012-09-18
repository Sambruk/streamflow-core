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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.InteractionValidation;
import se.streamsource.dci.api.RequiresValid;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class OrganizationUserContext
      implements IndexContext<LinksValue>, InteractionValidation
{
   @Structure
   Module module;

   public LinksValue index()
   {
      return new LinksBuilder( module.valueBuilderFactory() ).
            addLink( "Drafts", "drafts", "drafts", "workspace/user/drafts/cases", null ).
            addLink( "Inbox", "inbox", "inbox", "workspace/user/inbox/cases", null ).
            addLink( "Assignments", "assignments", "assignments", "workspace/user/assignments/cases", null ).
            newLinks();
   }

   @RequiresValid("LdapOffAdminAlways")
   public void resetpassword( @Name("password") String password )
   {
      UserAuthentication user = RoleMap.role( UserAuthentication.class );
      user.resetPassword( password );
   }


   @RequiresValid("AdminNever")
   @UserDisabled( false )
   public void setdisabled()
   {
      UserAuthentication user = RoleMap.role( UserAuthentication.class );
      UserAuthentication.Data userData = RoleMap.role( UserAuthentication.Data.class );

      user.changeEnabled( userData.disabled().get() );

   }

   @RequiresValid("AdminNever")
   @UserDisabled( true )
   public void setenabled()
   {
      UserAuthentication user = RoleMap.role( UserAuthentication.class );
      UserAuthentication.Data userData = RoleMap.role( UserAuthentication.Data.class );

      user.changeEnabled( userData.disabled().get() );
   }

   @RequiresValid("LdapOffAdminNever")
   @HasJoined( true )
   public void leave()
   {
      Organization org = role( Organization.class );
      OrganizationParticipations role = role( OrganizationParticipations.class );
      role.leave( org );
   }

   @RequiresValid("LdapOffAdminNever")
   @HasJoined( false )
   public void join()
   {
      Organization org = role( Organization.class );
      OrganizationParticipations role = role( OrganizationParticipations.class );
      role.join( org );
   }

   @RequiresValid("LdapOffAdminAlways")
   public void changemessagedeliverytype( @Name("messagedeliverytype") MessageRecipient.MessageDeliveryTypes newDeliveryType )
   {
      MessageRecipient recipient = role( MessageRecipient.class );
      recipient.changeMessageDeliveryType( newDeliveryType );
   }

   public String messagedeliverytype()
   {
      MessageRecipient.Data recipient = role( MessageRecipient.Data.class );
      return recipient.delivery().get().name();
   }

   public ContactDTO contact()
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      return contactable.getContact();
   }

   @RequiresValid("LdapOffAdminAlways")
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

   public boolean isValid( String name )
   {
      boolean isAdminUser = role( UserAuthentication.Data.class ).isAdministrator();
      ServiceReference ref = module.serviceFinder().findService( LdapImporterService.class );
      boolean isLdapOn =  ref != null && ref.isAvailable();

      if( "LdapOffAdminAlways".equals( name ))
      {
         if(isAdminUser)
         {
            return true;
         } else
         {
            return !isLdapOn;
         }
      } else if( "LdapOffAdminNever".equals( name ) )
      {
         if( isAdminUser )
         {
            return false;
         } else
         {
            return !isLdapOn;
         }

      } else if( "AdminNever".equals( name ) )
      {
         return !isAdminUser;
      } else
      {
         return false;
      }
   }
}
