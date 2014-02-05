/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.InteractionConstraint;
import se.streamsource.dci.api.InteractionConstraintDeclaration;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InteractionConstraintDeclaration(FirstFieldInPage.FirstFieldInPageConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FirstFieldInPage
{
   boolean value() default true;

   class FirstFieldInPageConstraint
         implements InteractionConstraint<FirstFieldInPage>
   {

      @Structure
      Module module;

      public boolean isValid( FirstFieldInPage firstFieldInPage, RoleMap roleMap )
      {
         Fields.Data fields = roleMap.get( Fields.Data.class );
         Field field = roleMap.get( Field.class );

         if( firstFieldInPage.value() )
         {
            return fields.fields().toList().indexOf( field ) == 0;
         } else
         {
            return fields.fields().toList().indexOf( field ) != 0;
         }
      }
   }
}
