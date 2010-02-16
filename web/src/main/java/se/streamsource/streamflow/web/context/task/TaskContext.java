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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContext;

/**
 * JAVADOC
 */
@Mixins(TaskContext.Mixin.class)
public interface TaskContext
   extends Context, TaskActionsContext
{
   @SubContext
   TaskGeneralContext general();

   @SubContext
   TaskContactsContext contacts();

   @SubContext
   TaskFormsContext forms();

   abstract class Mixin
      extends ContextMixin
      implements TaskContext
   {
      public TaskGeneralContext general()
      {
         return subContext( TaskGeneralContext.class );
      }

      public TaskContactsContext contacts()
      {
         return subContext( TaskContactsContext.class );
      }

      public TaskFormsContext forms()
      {
         return subContext( TaskFormsContext.class );
      }
   }
}
