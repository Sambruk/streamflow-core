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

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormsContext;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.DeleteContext;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;

/**
 * JAVADOC
 */
@Mixins(TaskTypeContext.Mixin.class)
public interface TaskTypeContext
   extends DescribableContext, DeleteContext, Context
{
   @SubContext
   FormsContext forms();

   @SubContext
   SelectedFormsContext selectedforms();

   @SubContext
   SelectedLabelsContext selectedlabels();

   abstract class Mixin
      extends ContextMixin
      implements TaskTypeContext
   {
      public void delete()
      {
         TaskTypes taskTypes = context.role(TaskTypes.class);

         TaskType taskType = context.role(TaskType.class);

         taskTypes.removeTaskType( taskType );
      }

      public FormsContext forms()
      {
         return subContext( FormsContext.class );
      }

      public SelectedFormsContext selectedforms()
      {
         return subContext( SelectedFormsContext.class );
      }

      public SelectedLabelsContext selectedlabels()
      {
         return subContext( SelectedLabelsContext.class );
      }
   }
}
