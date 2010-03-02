/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.structure.organizations;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.domain.user.Password;
import se.streamsource.streamflow.domain.user.Username;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import static se.streamsource.streamflow.infrastructure.event.DomainEvent.CREATE;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;

/**
 * JAVADOC
 */
@Mixins(Organizations.Mixin.class)
public interface Organizations
{
   Organization createOrganization( String name );

   interface Data
   {
      Organization createdOrganization( DomainEvent event, String id );
   }

   abstract class Mixin
         implements Organizations, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idGen;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      public Organization createOrganization(String name)
      {
         Organization ou = createdOrganization( CREATE, idGen.generate( Identity.class ) );
//         Organization ou = createdOrganization( CREATE, "Organization" );

         // Change name
         ou.changeDescription( name );

         return ou;
      }

      public Organization createdOrganization( DomainEvent event, String id )
      {
         EntityBuilder<Organization> entityBuilder = uowf.currentUnitOfWork().newEntityBuilder( Organization.class, id );
         entityBuilder.instanceFor( OwningOrganization.class ).organization().set( entityBuilder.instance() );
         return entityBuilder.newInstance();
      }
   }
}
