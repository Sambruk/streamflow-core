/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.generic;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;

import java.lang.reflect.*;

/**
 * Generic mixin for simple event methods that create an entity and add it to a collection. They have to follow this pattern:
 * SomeType fooCreated(DomainEvent event, String id)
 * This will instantiate an EntityComposite with the "SomeType" type and id "id". The new entity is then returned from the method.
 */
@AppliesTo(EventEntityCreatedMixin.EventEntityCreatedAppliesTo.class)
public class EventEntityCreatedMixin
      implements InvocationHandler
{
   @Structure
   UnitOfWorkFactory uowf;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      // Create entity
      EntityComposite entity = (EntityComposite) uowf.currentUnitOfWork().newEntity( method.getReturnType(), (String) args[1] );

      return entity;
   }

   public static class EventEntityCreatedAppliesTo
         implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         return method.getParameterTypes().length == 2 &&
               method.getParameterTypes()[0].equals( DomainEvent.class ) && method.getName().startsWith( "created" ) &&
               !method.getReturnType().equals( Void.TYPE );
      }
   }
}