/*
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.util;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static org.qi4j.api.util.Iterables.matchesAny;

/**
 * Register components here that should be visible/invisble, enabled/disabled depending on available commands.
 */
public class RefreshComponents
   implements Observer
{
   Map<Specification<LinkValue>, Component> visibles = new HashMap<Specification<LinkValue>, Component>(  );
   Map<Specification<LinkValue>, Component> enableds = new HashMap<Specification<LinkValue>, Component>(  );

   public RefreshComponents visibleOn(String command, Component... components)
   {
      for (Component component : components)
      {
         visibles.put( Links.withRel( command ), component );
      }
      return this;
   }

   public RefreshComponents enabledOn(String command, Component... components)
   {
      for (Component component : components)
      {
         enableds.put( Links.withRel( command ), component );
      }
      return this;
   }

   public void update( Observable o, Object arg )
   {
      if (arg instanceof ResourceValue)
         refresh((ResourceValue)arg);
   }

   public void refresh(ResourceValue resourceValue)
   {
      for (Map.Entry<Specification<LinkValue>, Component> en : visibles.entrySet())
      {
         Component value = en.getValue();

         for (Component component : Components.components( Specifications.<Component>TRUE(), value ))
         {
            component.setVisible( resourceValue != null && matchesAny( en.getKey(), resourceValue.commands().get() ) );
         }
      }

      for (Map.Entry<Specification<LinkValue>, Component> en : enableds.entrySet())
      {
         Component value = en.getValue();

         for (Component component : Components.components( Specifications.<Component>TRUE(), value ))
         {
            component.setEnabled( resourceValue != null && matchesAny( en.getKey(), resourceValue.commands().get() ) );
         }

      }
   }

}