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

package se.streamsource.dci.qi4j;

import org.qi4j.bootstrap.*;
import org.qi4j.runtime.injection.*;
import org.qi4j.runtime.injection.provider.*;
import org.qi4j.runtime.model.*;
import se.streamsource.dci.api.*;

/**
 * Injection factory for @Role annotation. Looks up role in current RoleMap.
 */
public class RoleInjectionProviderFactory
   implements InjectionProviderFactory
{
   public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
   {
      return new RoleInjectionProvider(dependencyModel.rawInjectionType());
   }

   static class RoleInjectionProvider
      implements InjectionProvider
   {
      private Class<?> roleType;

      public RoleInjectionProvider( Class<?> roleType )
      {
         this.roleType = roleType;
      }

      public Object provideInjection( InjectionContext context ) throws InjectionProviderException
      {
         try
         {
            return RoleMap.role( roleType );
         } catch (IllegalArgumentException e)
         {
            return null;
         }
      }
   }
}
