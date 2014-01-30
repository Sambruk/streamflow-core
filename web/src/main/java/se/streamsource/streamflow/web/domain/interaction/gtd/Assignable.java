/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import java.util.Date;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.MethodConstraintsConcern;

/**
 * JAVADOC
 */
@Concerns({MethodConstraintsConcern.class})
@Mixins(Assignable.Mixin.class)
public interface Assignable
{
   void assignTo( Assignee assignee );

   void unassign();

   boolean isAssigned();

   interface Data
   {
      @Optional
      Association<Assignee> assignedTo();

      @Optional
      Property<Date> assignedOn();
   }

   interface Events
   {
      void assignedTo( @Optional DomainEvent event, Assignee assignee );

      void unassigned( @Optional DomainEvent event );
   }

   public abstract class Mixin
         implements Assignable, Events
   {
      @This
      Data data;

      public void assignTo( Assignee assignee )
      {
         if (data.assignedTo().get() == null || !assignee.equals( data.assignedTo().get() ))
         {
            assignedTo( null, assignee );
         }
      }

      public void unassign()
      {
         if (data.assignedTo().get() != null)
            unassigned( null );
      }

      public boolean isAssigned()
      {
         return data.assignedTo().get() != null;
      }

      public void assignedTo( @Optional DomainEvent event, Assignee assignee )
      {
         data.assignedTo().set( assignee );
         data.assignedOn().set( event.on().get() );
      }

      public void unassigned( @Optional DomainEvent event )
      {
         data.assignedTo().set( null );
         data.assignedOn().set( null );
      }
   }
}
