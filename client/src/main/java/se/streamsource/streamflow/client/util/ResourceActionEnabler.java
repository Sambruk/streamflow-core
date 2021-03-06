/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.util;

import org.jdesktop.application.ApplicationAction;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;

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
      ResourceValue resource = getResource();

      // no resource --> noting to refresh
      if ( resource == null)
         return;

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

   protected abstract ResourceValue getResource();
}
