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
package se.streamsource.streamflow.web.domain.structure.task;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.task.DoubleSignatureTaskEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

@Mixins(DoubleSignatureTasks.Mixin.class)
public interface DoubleSignatureTasks
{

   DoubleSignatureTask createTask( Case caze, SubmittedFormValue submittedForm, @Optional FormDraft formDraft );


   interface Data {
      
      @Aggregated
      @Queryable(false)
      ManyAssociation<DoubleSignatureTask> doubleSignatureTasks();

      DoubleSignatureTask createdTask( @Optional DomainEvent event, String id );
   }

   abstract class Mixin
      implements DoubleSignatureTasks, Data
   {
      @Structure
      Module module;

      @Service
      IdentityGenerator idGenerator;

      @This
      Data data;

      public DoubleSignatureTask createTask( Case caze, SubmittedFormValue submittedFormValue, FormDraft formDraft )
      {
         DoubleSignatureTask task = createdTask( null, idGenerator.generate( DoubleSignatureTaskEntity.class ) );
         task.updateCase( caze );
         task.updateSubmittedForm( submittedFormValue );
         task.updateFormDraft( formDraft );

         return task;
      }

      public DoubleSignatureTask createdTask( DomainEvent event, String id )
      {
         EntityBuilder<DoubleSignatureTask> submissionEntityBuilder = module.unitOfWorkFactory().currentUnitOfWork()
               .newEntityBuilder( DoubleSignatureTask.class, id );

         DoubleSignatureTask task = submissionEntityBuilder.newInstance();
         data.doubleSignatureTasks().add( task );

         return task;
      }
   }
}
