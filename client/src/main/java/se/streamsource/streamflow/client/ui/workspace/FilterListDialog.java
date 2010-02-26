/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.EventList;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.FilteredList;
import se.streamsource.streamflow.infrastructure.application.LinkValue;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * JAVADOC
 */
public class FilterListDialog
      extends JPanel
{
   Dimension dialogSize = new Dimension( 600, 300 );

   public LinkValue selected;
   public FilteredList itemList;

   public FilterListDialog( @Uses String caption,
                                      final @Uses EventList<LinkValue> items,
                                      @Service ApplicationContext context,
                                      @Structure ObjectBuilderFactory obf )
   {
      super( new GridLayout(1, 1) );

      setName( caption );
      setActionMap( context.getActionMap( this ) );

      itemList = new FilteredList();
      itemList.setEventList( items );

      add( itemList );
   }

   public EntityReference getSelected()
   {
      return selected == null ? null : EntityReference.parseEntityReference( selected.id().get());
   }

   @Action
   public void execute()
   {
      selected = (LinkValue) itemList.getList().getSelectedValue();

      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}