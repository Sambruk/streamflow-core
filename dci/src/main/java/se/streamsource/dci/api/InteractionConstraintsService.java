/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.object.ObjectBuilderFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JAVADOC
 */
public class InteractionConstraintsService
   implements InteractionConstraints
{
   @Structure
   ObjectBuilderFactory obf;

   private Map<Method, InteractionConstraintsBinding> methodsConstraints = new ConcurrentHashMap<Method, InteractionConstraintsBinding>( );

   public boolean isValid(Method method, RoleMap roleMap )
   {
      return getConstraints( method ).isValid( roleMap );
   }

   public boolean hasConstraints(Method method)
   {
      return !getConstraints( method ).isConstrained();
   }

   private InteractionConstraintsBinding getConstraints( Method method )
   {
      InteractionConstraintsBinding constraintBindings = methodsConstraints.get( method );
      if (constraintBindings == null)
      {
         constraintBindings = findConstraints( method );
         methodsConstraints.put( method, constraintBindings );
      }
      return constraintBindings;
   }

   private InteractionConstraintsBinding findConstraints( Method method)
   {
      List<Binding> methodConstraintBindings = new ArrayList<Binding>( );

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
                  ConstraintBinding constraintBinding = new ConstraintBinding(constraint, annotation, roleClass);
                  methodConstraintBindings.add( constraintBinding );
               } catch (InstantiationException e)
               {
                  e.printStackTrace();
               } catch (IllegalAccessException e)
               {
                  e.printStackTrace();
               }
            }
         } else if (annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ) != null)
         {
            Class<? extends InteractionConstraint> constraintClass = annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ).value();
            InteractionConstraint<Annotation> constraint = null;
            try
            {
               try
               {
                  constraint = obf.newObject( constraintClass );
               } catch (NoSuchObjectException e)
               {
                  constraint = constraintClass.newInstance();
               }

            } catch (Exception e)
            {
               continue; // Skip this constraint
            }
            InteractionConstraintBinding constraintBinding = new InteractionConstraintBinding(constraint, annotation);
            methodConstraintBindings.add( constraintBinding );

         }
      }

      if (methodConstraintBindings.isEmpty())
         methodConstraintBindings = null;

      return new InteractionConstraintsBinding( methodConstraintBindings);
   }

   interface Binding
   {
      boolean isValid( RoleMap roleMap );
   }

   public static class InteractionConstraintsBinding
   {
      List<Binding> bindings;

      public InteractionConstraintsBinding( List<Binding> bindings )
      {
         this.bindings = bindings;
      }

      boolean isConstrained()
      {
         return bindings != null;
      }

      public boolean isValid( RoleMap roleMap )
      {
         if (bindings != null)
            for (Binding constraintBinding : bindings)
            {
               if (!constraintBinding.isValid( roleMap ))
                  return false;
            }

         return true;
      }
   }

   public static class ConstraintBinding
         implements Binding
   {
      Constraint<Annotation, Object> constraint;
      Annotation annotation;
      Class roleClass;

      public ConstraintBinding( Constraint<Annotation, Object> constraint, Annotation annotation, Class roleClass )
      {
         this.constraint = constraint;
         this.annotation = annotation;
         this.roleClass = roleClass;
      }

      public boolean isValid( RoleMap roleMap )
      {
         return constraint.isValid( annotation, roleMap.get(roleClass) );
      }
   }

   public static class InteractionConstraintBinding
      implements Binding
   {
      InteractionConstraint<Annotation> constraint;
      Annotation annotation;

      public InteractionConstraintBinding( InteractionConstraint<Annotation> constraint, Annotation annotation )
      {
         this.constraint = constraint;
         this.annotation = annotation;
      }

      public boolean isValid( RoleMap roleMap )
      {
         return constraint.isValid( annotation, roleMap );
      }
   }
}