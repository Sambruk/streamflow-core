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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Context;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationParticipationsQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.SubContexts;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(OrganizationUsersContext.Mixin.class)
public interface OrganizationUsersContext
   extends SubContexts<OrganizationUserContext>, Context
{
   public LinksValue users();

   public LinksValue possibleusers();

   public void join( EntityReferenceDTO userDTO );

   abstract class Mixin
      extends ContextMixin
      implements OrganizationUsersContext
   {
      public LinksValue users()
      {
         OrganizationParticipationsQueries participants = roleMap.get(OrganizationParticipationsQueries.class);

         QueryBuilder<User> builder = participants.users();
         Query<User> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

         return new LinksBuilder(module.valueBuilderFactory()).rel( "user" ).addDescribables( query ).newLinks();
      }

      public LinksValue possibleusers()
      {
         OrganizationParticipationsQueries participants = roleMap.get(OrganizationParticipationsQueries.class);

         Query<User> query = participants.possibleUsers();

         return new LinksBuilder(module.valueBuilderFactory()).command( "join" ).addDescribables( query ).newLinks();
      }

      public void join( EntityReferenceDTO userDTO )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         Organization org = roleMap.get( Organization.class );

         OrganizationParticipations user = uow.get( OrganizationParticipations.class, userDTO.entity().get().identity() );
         user.join( org );
      }

      public OrganizationUserContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( User.class, id ));
         return subContext( OrganizationUserContext.class );
      }
   }
}
