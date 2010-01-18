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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Invocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
@AppliesTo(MethodConstraintsConcern.Filter.class)
public class MethodConstraintsConcern
   extends GenericConcern
{
   @Invocation Method method;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      for (Annotation annotation : method.getAnnotations())
      {
         if (annotation.getClass().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            Constraints constraints = annotation.getClass().getAnnotation( Constraints.class );
            for (Class<? extends Constraint<?, ?>> aClass : constraints.value())
            {
               Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) aClass.newInstance();
               if (!constraint.isValid( annotation, proxy ))
                  return null;
            }
         }
      }

      return next.invoke( proxy, method, args );
   }

   public static class Filter
      implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         for (Annotation annotation : method.getAnnotations())
         {
            if (annotation.getClass().getAnnotation( ConstraintDeclaration.class ) != null)
            {
               return true;
            }
         }
         return false;
      }
   }
}
