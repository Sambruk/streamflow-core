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

package se.streamsource.streamflow.web.resource.organizations.groups;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.Groups;
import se.streamsource.streamflow.web.resource.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Path("groups")
@Mixins(GroupsResource.GroupsMixin.class)
public interface GroupsResource
{
   GroupResource group( Group group );

   class GroupsMixin
         implements GroupsResource
   {
      @Uses
      TransientBuilder<GroupResource> groupResource;

      public GroupResource group( Group group )
      {
         return groupResource.use( group ).newInstance();
      }

      public Iterable<GroupResource> group( Groups.Data groups )
      {
         List<GroupResource> groupList = new ArrayList<GroupResource>();
         for (Group group : groups.groups())
         {
            groupList.add( groupResource.use( group ).newInstance() );
         }
         return groupList;
      }
   }
}
