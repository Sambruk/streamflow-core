/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.dci.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map objects to role interfaces. An instance of InteractionContext
 * is created for each request, and is sent down in the composite chain
 * by InteractionMixin.subResource. Whenever an object is identified in the chain,
 * add it to the context.
 *
 * If an object has been added under a general role, then you can map it to a more specific
 * role by using the map() method.
 *
 * If objects are registered under their generic types, and a role lookup does not match anything, the
 * objects will be searched in the reverse insertion order, i.e. last registered object will be checked first.
 */
public class InteractionContext
{
   private Map<Class, Object> roles = new HashMap<Class, Object>( );
   private List<Object> objects = new ArrayList<Object>( );

   public InteractionContext()
   {
      objects.add( this );
   }

   public void playRoles(Object object, Class... roleClasses)
   {
      if (object == null)
         return;

      for (Class roleClass : roleClasses)
      {
         roles.put( roleClass, object );
      }

      objects.add( 0, object );
   }

   public <T> T role(Class<T> roleClass) throws IllegalArgumentException
   {
      Object object = roles.get( roleClass );

      if (object == null)
      {
         // If no explicit mapping has been made, see if
         // any other mapped objects could work
         for (Object possibleObject : objects)
         {
            if (roleClass.isInstance( possibleObject))
            {
               return roleClass.cast( possibleObject );
            }
         }
      }

      if (object == null)
      {
         throw new IllegalArgumentException("No object in context for role:"+roleClass.getSimpleName());
      }

      return roleClass.cast( object );
   }

   public void map(Class fromRoleClass, Class... toRoleClasses)
   {
      Object object = roles.get(fromRoleClass);
      if (object != null)
      {
         for (Class toRoleClass : toRoleClasses)
         {
            if (toRoleClass.isInstance( object))
               roles.put( toRoleClass, object );
            else
               throw new IllegalArgumentException(object +" does not implement role type "+toRoleClass.getName());
         }
      } else
      {
         throw new IllegalArgumentException(fromRoleClass.getName()+" has not been mapped");
      }
   }
}
