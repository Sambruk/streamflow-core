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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Base mixin for Roles. Provides access to the Context of the UoW associated
 * with the Entity of this Role.
 */
public abstract class RoleMixin
{
   @Structure
   UnitOfWorkFactory uowf;

   protected Context context()
   {
      return null; // TODO uowf.currentUnitOfWork().metaInfo().get(Context.class);
   }
}
