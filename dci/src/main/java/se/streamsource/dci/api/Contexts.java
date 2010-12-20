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

package se.streamsource.dci.api;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.specification.Specification;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.iterable;

/**
 * Helper methods for working with contexts
 */
public class Contexts
{
   public static Iterable<Method> commands( Class contextClass, final InteractionConstraints constraints, final RoleMap roleMap )
   {
      if (constraints.isValid( contextClass, roleMap ))
         return filter( new Specification<Method>()
         {
            public boolean satisfiedBy( Method method )
            {
               if (!method.isSynthetic() && !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
                  return (method.getReturnType().equals( Void.TYPE ) && constraints.isValid( method, roleMap ));
               else
                  return false;
            }
         }, iterable( contextClass.getMethods() ) );
      else
         return Collections.emptyList();
   }

   public static Iterable<Method> queries( Class contextClass, final InteractionConstraints constraints, final RoleMap roleMap )
   {
      return filter( new Specification<Method>()
      {
         public boolean satisfiedBy( Method method )
         {
            if (!method.isSynthetic() && !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
               return (!method.getReturnType().equals( Void.TYPE ) && constraints.isValid( method, roleMap ));
            else
               return false;
         }
      }, iterable( contextClass.getMethods() ) );
   }
}
