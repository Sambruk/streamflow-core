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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.form.Form;

/**
 * Holds the reference to a form that has to be filled in and submitted prior
 * to be able to close a case that has a certain case type.
 */
@Mixins(FormOnClose.Mixin.class)
public interface FormOnClose
{
   void changeFormOnClose( @Optional Form form);

   interface Data
   {
      @Optional
      Association<Form> formOnClose();
   }

   interface Events
   {
      void changedFormOnClose( @Optional DomainEvent event, @Optional Form form );
   }

   abstract class Mixin
      implements FormOnClose, Events
   {
      @This
      Data data;

      public void changeFormOnClose( Form form )
      {

         changedFormOnClose( null, form );
      }

      public void changedFormOnClose( DomainEvent event, Form form )
      {

         data.formOnClose().set( form );
      }
   }
}
