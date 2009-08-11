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

package se.streamsource.streamflow.domain.contact;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Contacts.ContactsMixin.class)
public interface Contacts
{
    public void addContact(ContactValue contact);

    public void removeContact(int index);

    public void updateContact(int index, ContactValue newContact);

    interface ContactsState
    {
        @UseDefaults
        Property<List<ContactValue>> contacts();
    }

    class ContactsMixin
    implements Contacts
    {
        @This ContactsState state;

        public void addContact(ContactValue contact)
        {

        }
            

        public void removeContact(int index)
        {

        }

        public void updateContact(int index, ContactValue newContact)
        {

        }


    }
    
}