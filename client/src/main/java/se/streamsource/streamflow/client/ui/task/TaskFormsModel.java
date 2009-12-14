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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * JAVADOC
 */
public class TaskFormsModel
      implements Refreshable, EventListener
{
   @Uses
   private TaskSubmittedFormsModel submittedForms;

   @Uses
   private TaskEffectiveFieldsValueModel effectiveValues;

   public TaskSubmittedFormsModel submittedForms()
   {
      return submittedForms;
   }

   public TaskEffectiveFieldsValueModel effectiveValues()
   {
      return effectiveValues;
   }

   public void refresh() throws OperationException
   {
      submittedForms.refresh();
      effectiveValues.refresh();
   }

   public void notifyEvent( DomainEvent event )
   {
      submittedForms.notifyEvent( event );
      effectiveValues.notifyEvent( event );
   }
}
