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

package se.streamsource.streamflow.web.infrastructure.web.context;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.util.Classes;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Check interaction constraints.
 */
@AppliesTo(InteractionConstraintsConcern.Filter.class)
public class InteractionConstraintsConcern
   extends GenericConcern
{
   List<InteractionConstraint> constraints = new ArrayList<InteractionConstraint>( );

   public InteractionConstraintsConcern( @Invocation Method method ) throws IllegalAccessException, InstantiationException
   {
      for (Annotation annotation : method.getAnnotations())
      {
         if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

            for (Class<? extends Constraint<?, ?>> aClass : constraints.value())
            {
               Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) aClass.newInstance();
               Class roleClass = (Class) ((ParameterizedType)aClass.getGenericInterfaces()[0]).getActualTypeArguments()[1];
               InteractionConstraint interactionConstraint = new InteractionConstraint(constraint, annotation, roleClass);
               this.constraints.add(interactionConstraint);
            }
         }
      }
   }

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      for (InteractionConstraint constraint : constraints)
      {
         Object role = ((Context)proxy).context().role( constraint.roleClass );
         if (!constraint.constraint.isValid( constraint.annotation, role ))
            throw new ConstraintViolationException((Composite) proxy, method, Collections.singleton( new ConstraintViolation(constraint.annotation.annotationType().getSimpleName(), constraint.annotation, proxy) ));

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
            if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
            {
               return true;
            }
         }
         return false;
      }
   }

   public static class InteractionConstraint
   {
      Constraint<Annotation, Object> constraint;
      Annotation annotation;
      Class roleClass;

      public InteractionConstraint( Constraint<Annotation, Object> constraint, Annotation annotation, Class roleClass )
      {
         this.constraint = constraint;
         this.annotation = annotation;
         this.roleClass = roleClass;
      }
   }
}