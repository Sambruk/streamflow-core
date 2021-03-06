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
package se.streamsource.streamflow.web.domain.structure.external;

import org.joda.time.DateTime;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.external.ShadowCaseDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.external.ShadowCaseEntity;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import java.util.Date;

import static org.qi4j.api.util.Iterables.*;

/**
 * Role of ShadowCases.
 */
@Mixins( ShadowCases.Mixin.class)
public interface ShadowCases
   extends ShadowCasesQueries
{
   ShadowCase createCase( ShadowCaseDTO shadowCaseIn );

   void addCase( ShadowCase caze );

   void removeCase( ShadowCase caze );

   interface Data
   {
      ManyAssociation<ShadowCase> cases();

      ShadowCase createdCase( @Optional DomainEvent event, String id, String systemName, String customer, String externalId, DateTime creationDate );

      void addedCase( @Optional DomainEvent event, ShadowCase caze );

      void removedCase( @Optional DomainEvent event, ShadowCase caze );
   }

   abstract class Mixin
      implements ShadowCases, Data
   {
      @Service
      IdentityGenerator idgen;

      @This
      Data data;

      @Structure
      Module module;

      public ShadowCase createCase( final ShadowCaseDTO shadowCaseIn )
      {
         // make sure we dont create same case again - key systemName-externalId
         if( count( filter( new Specification<ShadowCase>()
         {
            public boolean satisfiedBy( ShadowCase item )
            {
               return ((ShadowCase.Data) item).systemName().get().equalsIgnoreCase( shadowCaseIn.systemName().get() )
                     && ((ShadowCase.Data) item).externalId().get().equals( shadowCaseIn.externalId().get() );
            }
         }, data.cases() ) ) > 0 )
         {
            throw new IllegalArgumentException( "Case with identifier " + shadowCaseIn.systemName().get() + "-" + shadowCaseIn.externalId().get() + " already exists." );
         }

         String id = idgen.generate( Identity.class );

         ShadowCase caze = createdCase( null, id, shadowCaseIn.systemName().get().toLowerCase(), shadowCaseIn.externalId().get(), shadowCaseIn.contactId().get(), shadowCaseIn.creationDate().get() );
         caze.changeDescription( shadowCaseIn.description().get() );
         addCase( caze );

         return caze;
      }

      public ShadowCase createdCase( @Optional DomainEvent event, String id, String systemName, String externalId, String customer, DateTime creationDate )
      {
         EntityBuilder<ShadowCaseEntity> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( ShadowCaseEntity.class, id );
         builder.instanceFor( ShadowCase.Data.class ).systemName().set( systemName );
         builder.instanceFor( ShadowCase.Data.class ).externalId().set( externalId );
         builder.instanceFor( ShadowCase.Data.class ).contactId().set( customer );
         builder.instanceFor( ShadowCase.Data.class ).creationDate().set( creationDate );
         builder.instanceFor( CreatedOn.class ).createdOn().set( new Date() );

         return builder.newInstance();
      }

      public void addCase( ShadowCase caze )
      {
         if( !data.cases().contains( caze ) )
         {
            data.addedCase( null, caze );
         }
      }

      public void removeCase( ShadowCase caze )
      {
         if( data.cases().contains( caze ) )
         {
            data.removedCase( null, caze );

            caze.removeEntity();
         }
      }
   }
}
