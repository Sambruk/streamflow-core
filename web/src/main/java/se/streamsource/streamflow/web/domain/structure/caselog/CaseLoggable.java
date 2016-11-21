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
package se.streamsource.streamflow.web.domain.structure.caselog;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;

@Mixins(CaseLoggable.Mixin.class)
public interface CaseLoggable
{

   void createLog();
   
   interface Data
   {
      @Optional
      Association<CaseLog> caselog();
      
      void createdLog(@Optional DomainEvent event, String id);
   }
   
   abstract class Mixin implements CaseLoggable, Data
   {

      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;
      
      @This
      Data data;
      
      public void createdLog(DomainEvent event, String id)
      {
         CaseLogEntity caseLogEntity = module.unitOfWorkFactory().currentUnitOfWork().newEntity( CaseLogEntity.class, id );
         data.caselog().set( caseLogEntity );
      }

      public void createLog()
      {
         createdLog( null,  idGen.generate( Identity.class ));         
      }
      
   }
}
