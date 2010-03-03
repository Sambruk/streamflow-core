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

package se.streamsource.streamflow.web.context;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.access.AccessContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationsContext;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.context.users.UsersContext;

/**
 * JAVADOC
 */
@Mixins(RootContext.Mixin.class)
public interface RootContext
   extends Context
{
   /**
    * Users context. Here is where you access all users, and methods to create users.
    */
   @SubContext
   UsersContext users();

   /**
    * Here is where you access all tasks, including search
    */
   @SubContext
   TasksContext tasks();

   @SubContext
   OrganizationsContext organizations();

   @SubContext
   AccessContext access();

   abstract class Mixin
      extends ContextMixin
      implements RootContext
   {
      public UsersContext users()
      {
         return subContext( UsersContext.class );
      }

      public TasksContext tasks()
      {
         return subContext( TasksContext.class );
      }

      public OrganizationsContext organizations()
      {
         return subContext( OrganizationsContext.class );
      }

      public AccessContext access()
      {
         return subContext( AccessContext.class );
      }
   }
}
