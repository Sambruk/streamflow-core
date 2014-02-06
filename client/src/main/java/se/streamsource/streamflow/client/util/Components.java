/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.specification.Specification;

/**
 * JAVADOC
 */
public final class Components
{
   public static Iterable<Component> components(Specification<Component> specification, final Component root)
   {
      List<Component> list = new ArrayList<Component>(  );

      add(specification, root, list);

      return list;
   }

   private static void add(Specification<Component> specification, final Component component, List<Component> list)
   {
      if (specification.satisfiedBy( component ))
         list.add(component);

      if (component instanceof Container)
      {
         Container container = (Container) component;
         for (Component childComponent : container.getComponents())
         {
            add(specification, childComponent, list);
         }
      }
   }

   public static Specification<Component> isVisible()
   {
      return new Specification<Component>()
      {
         public boolean satisfiedBy( Component component )
         {
            return component.isVisible();
         }
      };
   }

   public static Specification<Component> isShowing()
   {
      return new Specification<Component>()
      {
         public boolean satisfiedBy( Component component )
         {
            return component.isShowing();
         }
      };
   }
}
