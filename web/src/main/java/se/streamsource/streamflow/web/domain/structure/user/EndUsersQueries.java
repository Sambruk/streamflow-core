/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

/**
 * Queries for fetching end users
 */
@Mixins( EndUsersQueries.Mixin.class)
public interface EndUsersQueries
{
   Iterable<EndUser> endusers();

   abstract class Mixin
      implements EndUsersQueries
   {
      @Structure
      Module module;

      @This
      UserAuthentication.Data user;

      public Iterable<EndUser> endusers()
      {
         Query<EndUser> query = module.queryBuilderFactory().newQueryBuilder( EndUser.class )
                     .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         return Iterables.filter(new Specification<EndUser>()
         {
            public boolean satisfiedBy( EndUser endUser )
            {
               return ((Identity)endUser).identity().get().startsWith( user.userName().get()+"/" );
            }
         }, query);
      }
   }
}
