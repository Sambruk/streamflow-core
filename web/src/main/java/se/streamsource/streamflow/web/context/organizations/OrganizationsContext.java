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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Numbers;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;

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
      @Structure
      UnitOfWorkFactory uowf;

      public LinksValue index()
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         OrganizationsQueries organizations = uow
               .get( OrganizationsQueries.class, OrganizationsEntity.ORGANIZATIONS_ID );

         return new LinksBuilder(module.valueBuilderFactory()).addDescribables( organizations.organizations().newQuery( uow )).newLinks();
      }

      public OrganizationContext context( String id )
      {
         OrganizationEntity organization = uowf.currentUnitOfWork().get( OrganizationEntity.class, id );
         organization.dirty().set(organization.dirty().get()+1  );
         context.set( organization );
         return subContext( OrganizationContext.class );
      }
   }
}
