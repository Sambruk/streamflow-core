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

package se.streamsource.streamflow.domain.contact;

import org.qi4j.api.common.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.value.*;
import org.qi4j.library.constraints.annotation.*;

import java.util.*;

/**
 * Contact information for either a person or a company
 */
@Mixins(ContactValue.Mixin.class)
public interface ContactValue
      extends ValueComposite
{
   @UseDefaults
   Property<String> name();

   @UseDefaults
   @Matches("([\\d]{12})?")
   Property<String> contactId();

   @UseDefaults
   Property<String> company();

   @UseDefaults
   Property<Boolean> isCompany();

   @UseDefaults
   Property<List<ContactPhoneValue>> phoneNumbers();

   @UseDefaults
   Property<List<ContactEmailValue>> emailAddresses();

   @UseDefaults
   Property<List<ContactAddressValue>> addresses();

   @UseDefaults
   Property<String> picture();

   @UseDefaults
   Property<String> note();

   ContactEmailValue defaultEmail();

   ContactPhoneValue defaultPhone();

   abstract class Mixin
      implements ContactValue
   {
      public ContactEmailValue defaultEmail()
      {
         ListIterator<ContactEmailValue> listIter = emailAddresses().get().listIterator();
         if (listIter.hasNext())
         {
            return listIter.next();
         } else
            return null;
      }

      public ContactPhoneValue defaultPhone()
      {
         ListIterator<ContactPhoneValue> listIter = phoneNumbers().get().listIterator();
         if (listIter.hasNext())
         {
            return listIter.next();
         } else
            return null;
      }
   }
}
