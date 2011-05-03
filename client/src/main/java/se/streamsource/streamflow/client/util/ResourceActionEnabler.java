/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.util;

import org.jdesktop.application.*;
import org.qi4j.api.specification.*;
import org.qi4j.api.util.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.*;

import javax.swing.Action;

/**
 * Enable actions based on commands and queries in a REST resource.
 */
public abstract class ResourceActionEnabler
      implements Refreshable
{
   private Action[] action;

   public ResourceActionEnabler(Action... action )
   {
      this.action = action;
      for (Action action1 : action)
      {
         action1.setEnabled( false );
      }
   }

   public void refresh()
   {
      ResourceValue resource = getClient().queryResource();
      Iterable<LinkValue> commandsAndQueries = Iterables.flatten(resource.commands().get(), resource.queries().get());

      Iterable<String> availableCommandsAndQueries = Iterables.map( new Function<LinkValue, String>()
      {
         public String map( LinkValue linkValue )
         {
            return linkValue.rel().get();
         }
      }, commandsAndQueries);

      for (final Action action1 : action)
      {
         action1.setEnabled(Iterables.matchesAny( new Specification<String>()
         {
            public boolean satisfiedBy( String item )
            {
               String actionName = ((ApplicationAction) action1).getName().toLowerCase();
               return item.equals(actionName);
            }
         }, availableCommandsAndQueries));
      }
   }

   protected abstract CommandQueryClient getClient();
}
