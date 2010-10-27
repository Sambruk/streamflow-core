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

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class GroupedFilterListDialog
      extends JPanel
{
   public List<LinkValue> selected = new ArrayList<LinkValue>();
   public GroupedFilteredList itemList;

   public GroupedFilterListDialog( @Uses String caption,
                                   final @Uses EventList<TitledLinkValue> items,
                                   @Service ApplicationContext context,
                                   @Structure ObjectBuilderFactory obf )
   {
      super( new GridLayout( 1, 1 ) );

      setName( caption );
      setActionMap( context.getActionMap( this ) );

      itemList = new GroupedFilteredList();
      itemList.setEventList( items );

      add( itemList );
   }

   public EntityReference getSelectedReference()
   {
      return selected.size() == 0 ? null : EntityReference.parseEntityReference( selected.get( 0 ).id().get() );
   }

   public EntityReference[] getSelectedReferences()
   {
      EntityReference[] refs = new EntityReference[selected.size()];
      for (int i = 0; i < selected.size(); i++)
      {
         LinkValue linkValue = selected.get( i );
         refs[i] = EntityReference.parseEntityReference( linkValue.id().get() );
      }
      return refs;
   }

   public LinkValue getSelectedItem()
   {
      return selected.size() == 0 ? null : selected.get( 0 );
   }

   public List<LinkValue> getSelectedItems()
   {
      return selected.size() > 0 ? selected : new ArrayList();
   }

   @Action
   public void execute()
   {
      selected = new ArrayList<LinkValue>();
      for (Object o : itemList.getList().getSelectedValues())
      {
         selected.add( (LinkValue) o );
      }

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}