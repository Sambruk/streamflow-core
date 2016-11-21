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
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   Module module;

   Logger logger = LoggerFactory.getLogger( InteractionConstraintsService.class );

   private Map<Method, InteractionConstraintsBinding> methodsConstraints = new ConcurrentHashMap<Method, InteractionConstraintsBinding>();
   private Map<Class, InteractionConstraintsBinding> classConstraints = new ConcurrentHashMap<Class, InteractionConstraintsBinding>();

   public boolean isValid( Method method, RoleMap roleMap, ModuleSPI module )
   {
      return getConstraints( method, module ).isValid( roleMap );
   }

   public boolean isValid( Class resourceClass, RoleMap roleMap, ModuleSPI module )
   {
      return getConstraints( resourceClass, module ).isValid( roleMap );
   }

   private InteractionConstraintsBinding getConstraints( Method method, ModuleSPI module )
   {
      InteractionConstraintsBinding constraintBindings = methodsConstraints.get( method );
      if (constraintBindings == null)
      {
         constraintBindings = findConstraints( method, module );
         methodsConstraints.put( method, constraintBindings );
      }
      return constraintBindings;
   }

   private InteractionConstraintsBinding getConstraints( Class aClass, ModuleSPI module )
   {
      InteractionConstraintsBinding constraintBindings = classConstraints.get( aClass );
      if (constraintBindings == null)
      {
         constraintBindings = findConstraints( aClass, module );
         classConstraints.put( aClass, constraintBindings );
      }
      return constraintBindings;
   }

   private InteractionConstraintsBinding findConstraints( Method method, ModuleSPI module )
   {
      List<Binding> methodConstraintBindings = new ArrayList<Binding>();

      for (Annotation annotation : method.getAnnotations())
      {
         if (annotation.annotationType().equals(RequiresValid.class))
         {
            RequiresValid requiresValid = (RequiresValid) annotation;

            Class contextClass = method.getDeclaringClass();
            if (InteractionValidation.class.isAssignableFrom( contextClass))
            {
               InteractionValidation validation = null;
               if (TransientComposite.class.isAssignableFrom( contextClass ))
               {
                  validation = (InteractionValidation) module.transientBuilderFactory().newTransient( contextClass );
               } else
               {
                  validation = (InteractionValidation) module.objectBuilderFactory().newObject( contextClass );
               }
               methodConstraintBindings.add( new RequiresValidBinding( requiresValid, validation ) );
            }
         } else if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

            for (Class<? extends Constraint<?, ?>> aClass : constraints.value())
            {
               try
               {
                  Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) aClass.newInstance();
                  Class roleClass = (Class) ((ParameterizedType) aClass.getGenericInterfaces()[0]).getActualTypeArguments()[1];
                  ConstraintBinding constraintBinding = new ConstraintBinding( constraint, annotation, roleClass );
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
                  constraint = module.objectBuilderFactory().newObject(constraintClass);
               } catch (NoSuchObjectException e)
               {
                  constraint = constraintClass.newInstance();
               }

            } catch (Exception e)
            {
               continue; // Skip this constraint
            }
            InteractionConstraintBinding constraintBinding = new InteractionConstraintBinding( constraint, annotation );
            methodConstraintBindings.add( constraintBinding );

         }
      }

      if (methodConstraintBindings.isEmpty())
         methodConstraintBindings = null;

      return new InteractionConstraintsBinding( methodConstraintBindings );
   }

   private InteractionConstraintsBinding findConstraints( Class aClass, ModuleSPI module )
   {
      List<Binding> classConstraintBindings = new ArrayList<Binding>();

      for (Annotation annotation : aClass.getAnnotations())
      {
         if (annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null)
         {
            Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

            for (Class<? extends Constraint<?, ?>> constraintClass : constraints.value())
            {
               try
               {
                  Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) constraintClass.newInstance();
                  Class roleClass = (Class) ((ParameterizedType) constraint.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];
                  ConstraintBinding constraintBinding = new ConstraintBinding( constraint, annotation, roleClass );
                  classConstraintBindings.add( constraintBinding );
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
                  constraint = module.objectBuilderFactory().newObject(constraintClass);
               } catch (NoSuchObjectException e)
               {
                  constraint = constraintClass.newInstance();
               }

            } catch (Exception e)
            {
               continue; // Skip this constraint
            }
            InteractionConstraintBinding constraintBinding = new InteractionConstraintBinding( constraint, annotation );
            classConstraintBindings.add( constraintBinding );

         }
      }

      if (classConstraintBindings.isEmpty())
         classConstraintBindings = null;

      return new InteractionConstraintsBinding( classConstraintBindings );
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

   public class RequiresValidBinding
         implements Binding
   {
      RequiresValid annotation;
      private final InteractionValidation validation;

      public RequiresValidBinding( RequiresValid annotation, InteractionValidation validation )
      {
         this.validation = validation;
         this.annotation = annotation;
      }

      public boolean isValid( RoleMap roleMap )
      {
         try
         {
            return validation.isValid( annotation.value() );
         } catch (IllegalArgumentException e)
         {
            return false;
         } catch (Throwable e)
         {
            logger.warn( "Could not check validation constraint for '" + annotation.value() + "'", e );
            return false;
         }
      }
   }

   public class ConstraintBinding
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
         try
         {
            Object checkedObject = roleClass.equals( RoleMap.class ) ? roleMap : roleMap.get( roleClass );

            return constraint.isValid( annotation, checkedObject );
         } catch (IllegalArgumentException e)
         {
            return false;
         } catch (Throwable e)
         {
            logger.warn( "Could not check constraint " + constraint.getClass().getName(), e );
            return false;
         }
      }
   }

   public class InteractionConstraintBinding
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
         try
         {
            return constraint.isValid( annotation, roleMap );
         } catch (Throwable e)
         {
            logger.warn( "Could not check constraint " + constraint.getClass().getName(), e );
            return false;
         }
      }
   }
}