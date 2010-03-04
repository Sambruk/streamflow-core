/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.access.organizations.AccessPointsContext;
import se.streamsource.streamflow.web.context.access.projects.ProjectsContext;

/**
 * JAVADOC
 */
@Mixins(OrganizationContext.Mixin.class)
public interface OrganizationContext
   extends Context
{
   @SubContext
   ProjectsContext projects();

   @SubContext
   AccessPointsContext accesspoints();

   @SubContext
   ProxyUsersContext proxyusers();

   abstract class Mixin
      extends ContextMixin
      implements OrganizationContext
   {

      public AccessPointsContext accesspoints()
      {
         return subContext( AccessPointsContext.class );
      }

      public ProjectsContext projects( )
      {
         return subContext( ProjectsContext.class);
      }

      public ProxyUsersContext proxyusers()
      {
         return subContext( ProxyUsersContext.class );
      }
   }
}