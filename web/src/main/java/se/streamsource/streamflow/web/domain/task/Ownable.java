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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Ownable.Mixin.class)
public interface Ownable
{
   void changeOwner( Owner owner );

   boolean isOwnedBy(Owner owner);

   interface Data
   {
      @Optional
      Association<Owner> owner();

      void changedOwner( DomainEvent event, Owner newOwner );
   }

   abstract class Mixin
         implements Ownable, Data
   {
      @This
      Data state;

      public void changeOwner( Owner owner )
      {
         changedOwner( DomainEvent.CREATE, owner );
      }

      public boolean isOwnedBy( Owner owner )
      {
         return owner.equals( owner().get() );
      }

      public void changedOwner( DomainEvent event, Owner newOwner )
      {
         state.owner().set( newOwner );
      }
   }
}
