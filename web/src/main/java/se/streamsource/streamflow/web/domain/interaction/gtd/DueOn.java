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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Date;

/**
 * Due date management for cases.
 */
@Mixins(DueOn.Mixin.class)
public interface DueOn
{
   void dueOn( @Future Date dueDate );

   interface Data
   {
      @Optional
      Property<Date> dueOn();


      void changedDueOn( @Optional DomainEvent event, Date dueDate );
   }

   abstract class Mixin
         implements DueOn, Data
   {
      public void dueOn( Date dueDate )
      {
         changedDueOn( null, dueDate );
      }

      public void changedDueOn( @Optional DomainEvent event, Date dueDate )
      {
         dueOn().set( dueDate );
      }
   }
}