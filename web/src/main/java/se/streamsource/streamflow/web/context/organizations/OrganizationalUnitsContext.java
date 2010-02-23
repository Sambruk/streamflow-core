/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.Value;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitsContext.Mixin.class)
public interface OrganizationalUnitsContext
   extends SubContexts<OrganizationalUnitContext>, IndexContext<LinksValue>, Context
{
   public void createorganizationalunit( StringDTO value );

   abstract class Mixin
      extends ContextMixin
      implements OrganizationalUnitsContext
   {

      @Structure
      Module module;

      public LinksValue index()
      {
         OrganizationalUnits.Data ous = context.role(OrganizationalUnits.Data.class);
         return new LinksBuilder(module.valueBuilderFactory()).rel( "organizationalunit" ).addDescribables( ous.organizationalUnits() ).newLinks();
      }

      public void createorganizationalunit( StringDTO value )
      {
         OrganizationalUnits ous = context.role(OrganizationalUnits.class);

         ous.createOrganizationalUnit( value.string().get() );
      }

      public OrganizationalUnitContext context( String id )
      {
         context.playRoles(module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationalUnit.class, id ));
         return subContext( OrganizationalUnitContext.class );
      }
   }
}
