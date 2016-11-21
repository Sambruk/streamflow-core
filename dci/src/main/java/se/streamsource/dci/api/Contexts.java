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
package se.streamsource.dci.api;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.specification.Specification;
import org.qi4j.spi.structure.ModuleSPI;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.iterable;

/**
 * Helper methods for working with contexts
 */
public class Contexts
{
   public static Iterable<Method> commands( Class contextClass, final InteractionConstraints constraints, final RoleMap roleMap, final ModuleSPI moduleInstance )
   {
      if (constraints.isValid( contextClass, roleMap, moduleInstance ))
         return filter( new Specification<Method>()
         {
            public boolean satisfiedBy( Method method )
            {
               if (!method.isSynthetic() && !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
                  return (method.getReturnType().equals( Void.TYPE ) && constraints.isValid( method, roleMap, moduleInstance ));
               else
                  return false;
            }
         }, iterable( contextClass.getMethods() ) );
      else
         return Collections.emptyList();
   }

   public static Iterable<Method> queries( Class contextClass, final InteractionConstraints constraints, final RoleMap roleMap, final ModuleSPI module )
   {
      return filter( new Specification<Method>()
      {
         public boolean satisfiedBy( Method method )
         {
            if (!method.isSynthetic() && !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
               return (!method.getReturnType().equals( Void.TYPE ) && constraints.isValid( method, roleMap, module ));
            else
               return false;
         }
      }, iterable( contextClass.getMethods() ) );
   }
}
