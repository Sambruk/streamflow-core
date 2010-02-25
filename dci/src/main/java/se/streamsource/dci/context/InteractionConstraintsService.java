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

import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Constraints;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JAVADOC
 */
public class InteractionConstraintsService
{
   private Map<Method, List<InteractionConstraint>> methodsConstraints = new ConcurrentHashMap<Method, List<InteractionConstraint>>( );

   public boolean isValid(Method method, InteractionContext context)
   {
      for (InteractionConstraint constraint : getConstraints( method ))
      {
         Object role = context.role( constraint.roleClass );
         if (!constraint.constraint.isValid( constraint.annotation, role ))
            return false;

      }

      return true;
   }

   public boolean hasConstraints(Method method)
   {
      return !getConstraints( method ).isEmpty();
   }

   private List<InteractionConstraint> getConstraints( Method method )
   {
      List<InteractionConstraint> constraints = methodsConstraints.get( method );
      if (constraints == null)
      {
         constraints = findConstraints( method );
         methodsConstraints.put( method, constraints );
      }
      return constraints;
   }

   private List<InteractionConstraint> findConstraints( Method method)
   {
      List<InteractionConstraint> methodConstraints = new ArrayList<InteractionConstraint>( );

      for (Annotation annotation : method.getAnnotations())
      {
         if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

            for (Class<? extends Constraint<?, ?>> aClass : constraints.value())
            {
               try
               {
                  Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) aClass.newInstance();
                  Class roleClass = (Class) ((ParameterizedType)aClass.getGenericInterfaces()[0]).getActualTypeArguments()[1];
                  InteractionConstraint interactionConstraint = new InteractionConstraint(constraint, annotation, roleClass);
                  methodConstraints.add(interactionConstraint);
               } catch (InstantiationException e)
               {
                  e.printStackTrace();
               } catch (IllegalAccessException e)
               {
                  e.printStackTrace();
               }
            }
         }
      }

      return methodConstraints;
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