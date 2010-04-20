/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.forms;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;

/**
 * JAVADOC
 */
@Mixins(RequiredFormsContext.Mixin.class)
public interface RequiredFormsContext
   extends Interactions, IndexInteraction<LinksValue>
{
   // commands
   void createformdraft( EntityReferenceDTO form );


   abstract class Mixin
      extends InteractionsMixin
      implements RequiredFormsContext
   {
      public LinksValue index()
      {
         TypedTask.Data typedTask = context.get( TypedTask.Data.class );

         SelectedForms.Data forms = (SelectedForms.Data) typedTask.taskType().get();

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         builder.command( "createformdraft" );

         builder.addDescribables( forms.selectedForms() );

         return builder.newLinks();
      }

      public void createformdraft( EntityReferenceDTO formReference )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         Form form = uow.get( Form.class, formReference.entity().get().identity() );

         Task task = context.get( Task.class );
         task.createFormSubmission( form );
      }
   }
}