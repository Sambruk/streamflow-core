/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.dci.api;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.spi.structure.ModuleSPI;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Add this concern to all interaction methods that use constraints
 */
@AppliesTo(InteractionConstraintsConcern.HasInteractionConstraints.class)
public class InteractionConstraintsConcern
   extends GenericConcern
{
   @Service
   InteractionConstraints interactionConstraints;

   @Service
   ModuleSPI module;

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (!interactionConstraints.isValid(proxy.getClass(), RoleMap.current(), module))
         throw new IllegalStateException("Not allowed to invoke interaction "+method.getName());

      if (!interactionConstraints.isValid(method, RoleMap.current(), module))
         throw new IllegalStateException("Not allowed to invoke interaction "+method.getName());

      return next.invoke(proxy, method, args);
   }

   public static class HasInteractionConstraints
      implements AppliesToFilter
   {
      public boolean appliesTo(Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass)
      {
         for (Annotation annotation : method.getAnnotations())
         {
            if (annotation.annotationType().equals(RequiresValid.class) ||
                  annotation.annotationType().getAnnotation(ConstraintDeclaration.class) != null ||
                  annotation.annotationType().getAnnotation(InteractionConstraintDeclaration.class) != null)
               return true;
         }
         return false;
      }
   }
}
