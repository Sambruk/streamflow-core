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

package se.streamsource.streamflow.web.context;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ServicesContext.Mixin.class)
public interface ServicesContext
   extends Context
{
   ContactList contactlookup(ContactValue template);

   abstract class Mixin
      extends ContextMixin
      implements ServicesContext
   {
      @Service
      StreamflowContactLookupService contactLookup;

      public ContactList contactlookup( ContactValue template )
      {
         return contactLookup.lookup( template );
      }
   }
}