/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.generic;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic mixin for simple command methods that create an entity and add it to a collection. They have to follow this pattern:
 * SomeType createFoo(<args>)
 * This will generated an id for "SomeType" (so SomeType needs to extend EntityComposite!) and then call the
 * event method "createdFoo(DomainEvent.CREATE, id)"
 * followed by "addedFoo(DomainEvent.CREATE, entity)".
 * The new entity is then returned from the method.
 */
@AppliesTo(CommandEntityCreateMixin.CommandEntityCreateAppliesTo.class)
public class CommandEntityCreateMixin
      implements InvocationHandler
{
   private static Map<Method, Method> createdMappings = new ConcurrentHashMap();
   private static Map<Method, Method> addedMappings = new ConcurrentHashMap();

   @Service
   IdentityGenerator idGen;

   @State
   EntityStateHolder state;

   @Structure
   UnitOfWorkFactory uowf;

   @This
   EntityComposite composite;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {

      //Method createdMethod = createdMappings.get(method);
      //Method addedMethod = null;
      Method createdMethod;
      Method addedMethod;
      //if (createdMethod == null)
      //{
      // createFoo -> createdFoo
      String name = method.getName().substring( "create".length() );
      {
         String createdName = "created" + name;
         Class[] parameterTypes = new Class[]{DomainEvent.class, String.class};
         createdMethod = composite.getClass().getMethod( createdName, parameterTypes );
         //createdMappings.put(method, createdMethod);
      }

      // createFoo -> addedFoo
      {
         String addedName = "added" + name;
         Class[] parameterTypes = new Class[]{DomainEvent.class, method.getReturnType()};
         addedMethod = composite.getClass().getMethod( addedName, parameterTypes );
         //addedMappings.put(method, addedMethod);
      }
      /*} else
      {
          addedMethod = addedMappings.get(method);
      }*/

      // Generate id
      String id = idGen.generate( (Class<? extends Identity>) method.getReturnType() );

      // Create entity
      Object entity = createdMethod.invoke( composite, DomainEvent.CREATE, id );

      // Add entity to collection
      addedMethod.invoke( composite, DomainEvent.CREATE, entity );

      return entity;
   }

   public static class CommandEntityCreateAppliesTo
         implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         return method.getName().startsWith( "create" ) &&
               method.getReturnType().isInterface() &&
               !Property.class.isAssignableFrom( method.getReturnType()) &&
               !Association.class.isAssignableFrom( method.getReturnType());
      }
   }
}