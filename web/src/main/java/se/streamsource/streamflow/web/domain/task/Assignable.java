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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * JAVADOC
 */
@Concerns(Assignable.AssignOnStatusChangeConcern.class)
@Mixins(Assignable.Mixin.class)
public interface Assignable
{
   void assignTo( Assignee assignee );

   void unassign();

   interface Data
   {
      @Optional
      Association<Assignee> assignedTo();

      @Optional
      Property<Date> assignedOn();


      void assignedTo( DomainEvent event, Assignee assignee );


      void unassigned( DomainEvent event );
   }

   public abstract class Mixin
         implements Assignable, Data
   {
      public void assignTo( Assignee assignee )
      {
         if (!assignee.equals( assignedTo().get() ))
         {
            assignedTo( DomainEvent.CREATE, assignee );
         }
      }

      public void unassign()
      {
         if (assignedTo().get() != null)
            unassigned( DomainEvent.CREATE );
      }

      public void assignedTo( DomainEvent event, Assignee assignee )
      {
         assignedTo().set( assignee );
         assignedOn().set( event.on().get() );
      }

      public void unassigned( DomainEvent event )
      {
         assignedTo().set( null );
         assignedOn().set( null );
      }
   }

   abstract class AssignOnStatusChangeConcern
         extends ConcernOf<TaskStatus>
         implements TaskStatus
   {
      @This
      Data state;

      @This
      Assignable assignable;

      public void complete()
      {
         next.complete();
      }

      public void drop()
      {
         next.drop();
      }
   }

}
