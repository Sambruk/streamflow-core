/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
@Mixins(IndividualRepositoryService.Mixin.class)
public interface IndividualRepositoryService
      extends IndividualRepository, ServiceComposite, Activatable
{
   class Mixin
         implements IndividualRepository, Activatable
   {
      final Logger logger = LoggerFactory.getLogger( IndividualRepository.class.getName() );

      @Structure
      Module module;

      public Individual individual()
      {
         UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
         return unitOfWork.get( Individual.class, "1" );
      }

      public void activate() throws Exception
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();

         try
         {
            uow.get( Individual.class, "1" );
         } catch (NoSuchEntityException e)
         {
            // Create Individual
            uow.newEntity( Individual.class, "1" );

            logger.info( "Created invidual" );
         }

         uow.complete();
      }

      public void passivate() throws Exception
      {
      }
   }
}
