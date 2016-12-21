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
package se.streamsource.streamflow.web.context.administration;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

/**
 * JAVADOC
 */
public class ParticipantsContext
      implements IndexContext<Iterable<Participant>>
{
   @Structure
   Module module;

   public Iterable<Participant> index()
   {
      return RoleMap.role( Participants.Data.class ).participants();
   }

   @ServiceAvailable( service = LdapImporterService.class, availability = false )
   public void addparticipant( EntityValue participantId )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Participant participant = uow.get( Participant.class, participantId.entity().get() );

      Participants participants = RoleMap.role( Participants.class );

      participants.addParticipant( participant );
   }

   public LinksValue possibleusers()
   {
      OwningOrganization org = RoleMap.role( OwningOrganization.class );
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Participants.Data participants = RoleMap.role( Participants.Data.class );

      Query<UserEntity> users = organization.findUsersByUsername( "*" ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
      users = users.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );

      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
      linksBuilder.command( "addparticipant" );

      for (UserEntity user : users)
      {
         if (!participants.participants().contains( user ))
         {
            String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
            linksBuilder.addDescribable( user, group );
         }
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possiblegroups()
   {
      OrganizationQueries org = RoleMap.role( OrganizationQueries.class );

      final GroupEntity group = RoleMap.role( GroupEntity.class );

      final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
      linksBuilder.command( "addparticipant" );

      org.visitOrganization( new OrganizationVisitor()
      {
         @Override
         public boolean visitGroup( Group grp )
         {
            if (!group.participants().contains( grp )
                  && !group.equals( grp ))
            {
               String grouping = "" + Character.toUpperCase( grp.getDescription().charAt( 0 ) );
               linksBuilder.addDescribable( grp, grouping );
            }

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Groups.class ) );

      return linksBuilder.newLinks();
   }
}
