/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * Templates for emails sent out by email Access Points.
 */
@Mixins(EmailTemplates.Mixin.class)
public interface EmailTemplates
{
   void changeTemplate(String key, String value);
   void changeSubject( String subject);
   void synchronizeTemplates();

   interface Data
   {
      Property<String> subject();

      @UseDefaults
      Property<Map<String, String>> emailTemplates();
   }

   interface Events
   {
      void changedSubject(@Optional DomainEvent event, String newSubjectTemplate);
      void changedTemplate(@Optional DomainEvent event, String key, String template);
   }

   class Mixin
      implements EmailTemplates, Events
   {
      @This
      Data data;

      public void changeSubject(String subject)
      {
         changedSubject(null, subject);
      }

      public void changedSubject(@Optional DomainEvent event, String newSubjectTemplate)
      {
         data.subject().set(newSubjectTemplate);
      }

      public void changeTemplate(String key, String value)
      {
         changedTemplate(null, key, value);
      }

      public void changedTemplate(@Optional DomainEvent event, String key, String template)
      {
         Map<String, String> templates = data.emailTemplates().get();
         templates.put(key, template);
         data.emailTemplates().set(templates);
      }

      public void synchronizeTemplates()
      {
         // Synchronize defaults for emails
         ResourceBundle bundle = ResourceBundle.getBundle(EmailTemplates.class.getName());
         for (String key : bundle.keySet())
         {
            if (data.emailTemplates().get().get(key) == null)
               changeTemplate(key, bundle.getString(key));
         }
      }
   }
}
