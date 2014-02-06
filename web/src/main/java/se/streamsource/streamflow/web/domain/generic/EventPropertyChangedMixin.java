/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.StateHolder;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Generic mixin for simple event methods that update a property. They have to follow this pattern:
 * void changedFoo(DomainEvent event, SomeType newValue)
 * This will cause the property "foo" to be updated with the "newValue"
 */
@AppliesTo(EventPropertyChangedMixin.EventPropertyChangeAppliesTo.class)
public class EventPropertyChangedMixin
      implements InvocationHandler
{
   private static Map<Method, Method> methodMappings = new ConcurrentHashMap<Method, Method>();

   @State
   StateHolder state;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      Method propertyMethod = methodMappings.get( method );
      if (propertyMethod == null)
      {
         // Find property method
         String propName = Introspector.decapitalize( method.getName().substring( "changed".length() ) );
         propertyMethod = method.getDeclaringClass().getMethod( propName );
         methodMappings.put( method, propertyMethod );
      }

      // Update property
      state.getProperty( propertyMethod ).set( args[1] );

      return null;
   }

   public static class EventPropertyChangeAppliesTo
         implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         return method.getParameterTypes().length == 2 && method.getParameterTypes()[0].equals( DomainEvent.class ) && method.getName().startsWith( "changed" );
      }
   }
}
