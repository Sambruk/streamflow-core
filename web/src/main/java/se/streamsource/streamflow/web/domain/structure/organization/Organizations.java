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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Organizations.Mixin.class)
public interface Organizations
{
   Organization createOrganization( String name );

   interface Data
   {
      @Optional
      Association<Organization> organization();

      Organization createdOrganization( @Optional DomainEvent event, String id );
   }

   abstract class Mixin
         implements Organizations, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      public Organization createOrganization(String name)
      {
         Organization ou = createdOrganization( null, idGen.generate( Identity.class ) );
//         Organization ou = createdOrganization( CREATE, "Organization" );

         // Change name
         ou.changeDescription( name );

         return ou;
      }

      public Organization createdOrganization( DomainEvent event, String id )
      {
         EntityBuilder<Organization> entityBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Organization.class, id );
         entityBuilder.instanceFor( OwningOrganization.class ).organization().set( entityBuilder.instance() );
         Organization organization = entityBuilder.newInstance();
         organization().set(organization);
         return organization;
      }
   }
}
