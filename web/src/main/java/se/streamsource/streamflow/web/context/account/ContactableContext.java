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
package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

/**
 * JAVADOC
 */
public class ContactableContext
      implements IndexContext<ContactDTO>
{
   @Structure
   Module module;

   public ContactDTO index()
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();
      return contact;
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