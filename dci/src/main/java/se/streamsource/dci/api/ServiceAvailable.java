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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate interaction methods with ServiceAvailable. They will only be valid
 * if a service with the given type is available.
 */
@InteractionConstraintDeclaration(ServiceAvailable.ServiceAvailableConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceAvailable
{
   Class service();
   boolean availability() default true;

   public class ServiceAvailableConstraint
         implements InteractionConstraint<ServiceAvailable>
   {
      @Structure
      Module module;

      public boolean isValid( ServiceAvailable serviceAvailable, RoleMap roleMap )
      {
         ServiceReference ref = module.serviceFinder().findService( serviceAvailable.service() );
         return serviceAvailable.availability() ? ref != null && ref.isAvailable() : ref == null || !ref.isAvailable();
      }
   }
}