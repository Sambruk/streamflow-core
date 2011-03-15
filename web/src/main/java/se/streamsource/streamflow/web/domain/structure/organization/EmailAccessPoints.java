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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.List;

/**
 * TODO
 */
@Mixins(EmailAccessPoints.Mixin.class)
public interface EmailAccessPoints
{
   void addEmailAccessPoint(EmailAccessPointValue emailAccessPoint);
   void removeEmailAccessPoint(String email);

   AccessPoint getAccessPoint(String email) throws IllegalArgumentException;

   interface Data
   {
      @UseDefaults
      Property<List<EmailAccessPointValue>> emailAccessPoints();

      void addedEmailAccessPoint(@Optional DomainEvent event, EmailAccessPointValue emailAccessPoint);
      void removedEmailAccessPoint(@Optional DomainEvent event, String email);
   }

   abstract class Mixin
      implements EmailAccessPoints, Data
   {
      @Structure
      Module module;

      public void addEmailAccessPoint(EmailAccessPointValue emailAccessPoint)
      {
         // See first if this is a replacement of existing setting
         for (EmailAccessPointValue emailAccessPointValue : emailAccessPoints().get())
         {
            if (emailAccessPoint.email().get().equals(emailAccessPoint.email().get()))
            {
               removedEmailAccessPoint(null, emailAccessPoint.email().get());
               break;
            }
         }

         // Add it
         addedEmailAccessPoint(null, emailAccessPoint);
      }

      public void removeEmailAccessPoint(String email)
      {
         removedEmailAccessPoint(null, email);
      }

      public AccessPoint getAccessPoint(String email) throws IllegalArgumentException
      {
         for (EmailAccessPointValue emailAccessPointValue : emailAccessPoints().get())
         {
            if (emailAccessPointValue.email().get().equals(email))
            {
               return module.unitOfWorkFactory().currentUnitOfWork().get(AccessPoint.class, emailAccessPointValue.accessPoint().get().identity());
            }
         }

         // None found for this email address
         throw new IllegalArgumentException("No AccessPoint registered for email address:"+email);
      }

      public void addedEmailAccessPoint(@Optional DomainEvent event, EmailAccessPointValue emailAccessPoint)
      {
         List<EmailAccessPointValue> list = emailAccessPoints().get();
         list.add(emailAccessPoint);
         emailAccessPoints().set(list);
      }

      public void removedEmailAccessPoint(@Optional DomainEvent event, String email)
      {
         List<EmailAccessPointValue> list = emailAccessPoints().get();
         for (EmailAccessPointValue emailAccessPointValue : list)
         {
            if (emailAccessPointValue.email().get().equals(email))
            {
               list.remove(emailAccessPointValue);
               break;
            }
         }
         emailAccessPoints().set(list);
      }
   }
}
