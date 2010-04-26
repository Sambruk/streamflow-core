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

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

/**
 * JAVADOC
 */
@Mixins(OrganizationsContext.Mixin.class)
public interface OrganizationsContext
   extends SubContexts<OrganizationContext>, IndexInteraction<LinksValue>, Interactions
{
   abstract class Mixin
      extends InteractionsMixin
      implements OrganizationsContext
   {
      public LinksValue index()
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         Query<OrganizationEntity> entityQuery = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID ).organizations().newQuery( uow );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         linksBuilder.addDescribables( entityQuery );

         return linksBuilder.newLinks();
      }

      public OrganizationContext context( String id )
      {
         context.set( module.unitOfWorkFactory().currentUnitOfWork().get( Organization.class, id ) );

         return subContext( OrganizationContext.class);
      }
   }
}