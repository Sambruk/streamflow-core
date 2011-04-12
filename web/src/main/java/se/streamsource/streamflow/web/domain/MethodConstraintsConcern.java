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

package se.streamsource.streamflow.web.domain;

import org.qi4j.api.*;
import org.qi4j.api.common.*;
import org.qi4j.api.composite.*;
import org.qi4j.api.concern.*;
import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Check method constraints. A method constraint is an 
 */
@AppliesTo(MethodConstraintsConcern.Filter.class)
public class MethodConstraintsConcern
   extends GenericConcern
{
   Constraint<Annotation, Object> constraint;
   private Annotation annotation;

   private @This
   Composite composite;
   private @Structure
   Qi4j api;

   public MethodConstraintsConcern( @Invocation Method method ) throws IllegalAccessException, InstantiationException
   {
      for (Annotation annotation : method.getAnnotations())
      {
         if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            this.annotation = annotation;
            Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );
            for (Class<? extends Constraint<?, ?>> aClass : constraints.value())
            {
               constraint = (Constraint<Annotation, Object>) aClass.newInstance();
            }
         }
      }
   }

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      if (!constraint.isValid( annotation, api.dereference( composite ) ))
         throw new ConstraintViolationException(api.dereference( composite ), method, Collections.singleton( new ConstraintViolation(annotation.annotationType().getSimpleName(), annotation, api.dereference( composite )) ));

      return next.invoke( proxy, method, args );
   }

   public static class Filter
      implements AppliesToFilter
   {
      public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
      {
         for (Annotation annotation : method.getAnnotations())
         {
            if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
            {
               return true;
            }
         }
         return false;
      }
   }
}
