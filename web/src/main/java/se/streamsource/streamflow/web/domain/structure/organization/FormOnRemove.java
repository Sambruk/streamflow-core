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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.form.Form;

/**
 * Holds the reference to a form that has to be filled in and submitted prior
 * to be able to remove any case within the organization
 */
@Mixins(FormOnRemove.Mixin.class)
public interface FormOnRemove
{
   void changeFormOnRemove( @Optional Form form );

   interface Data
   {
      @Optional
      Association<Form> formOnRemove();
   }

   interface Events
   {
      void changedFormOnRemove( @Optional DomainEvent event, @Optional Form form );
   }

   abstract class Mixin
      implements FormOnRemove, Events
   {
      @This
      Data data;

      public void changeFormOnRemove( Form form )
      {

         changedFormOnRemove( null, form );
      }

      public void changedFormOnRemove( DomainEvent event, Form form )
      {

         data.formOnRemove().set( form );
      }
   }
}
