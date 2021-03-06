/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.generic;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Generic mixin for simple event methods that remove an entity from a collection. They have to follow this pattern:
 * void removedFoo(DomainEvent event, SomeType entity)
 * This will remove the entity from the ManyAssociation called "foos"
 */
@AppliesTo(EventEntityRemovedMixin.EventEntityRemovedAppliesTo.class)
public class EventEntityRemovedMixin
      implements InvocationHandler
{
   private static Map<Method, Method> methodMappings = new ConcurrentHashMap();

   @State
   EntityStateHolder state;

   @This
   EntityComposite composite;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      Method manyAssociationMethod = methodMappings.get( method );
      if (manyAssociationMethod == null)
      {
         // Find ManyAssociation method
         String removedName = Introspector.decapitalize( method.getName().substring( "removed".length() ) ) + "s";
         manyAssociationMethod = composite.getClass().getInterfaces()[0].getMethod( removedName );
         methodMappings.put( method, manyAssociationMethod );
      }

      // Lookup the ManyAssociation
      ManyAssociation<Object> manyAssociation = state.getManyAssociation( manyAssociationMethod );

      // Remove entity from ManyAssociation
      manyAssociation.remove( args[1] );

      return null;
   }

   public static class EventEntityRemovedAppliesTo
         implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         return method.getParameterTypes().length == 2 &&
               method.getParameterTypes()[0].equals( DomainEvent.class ) && method.getName().startsWith( "removed" ) &&
               method.getReturnType().equals( Void.TYPE );
      }
   }
}