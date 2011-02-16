/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.jdesktop.application.ApplicationAction;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;

import javax.swing.*;

/**
 * Enable actions based on commands in a REST resource.
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
      Iterable<String> availableCommands = Iterables.map( new Function<LinkValue, String>()
      {
         public String map( LinkValue linkValue )
         {
            return linkValue.rel().get();
         }
      }, getClient().queryResource().commands().get());

      for (final Action action1 : action)
      {
         action1.setEnabled(Iterables.matchesAny( new Specification<String>()
         {
            public boolean satisfiedBy( String item )
            {
               String actionName = ((ApplicationAction) action1).getName().toLowerCase();
               return item.equals(actionName);
            }
         }, availableCommands));
      }
   }

   protected abstract CommandQueryClient getClient();
}
