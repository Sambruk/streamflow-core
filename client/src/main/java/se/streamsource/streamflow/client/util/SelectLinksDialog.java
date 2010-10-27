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
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SelectLinksDialog
      extends JPanel
{

   private ValueBuilderFactory vbf;

   private LinksValue selectedLinks;

   private JList list;

   public SelectLinksDialog( @Service ApplicationContext context,
                                         @Uses EventList<LinkValue> model,
                                         @Structure ValueBuilderFactory vbf )
   {
      super( new BorderLayout() );
      this.vbf = vbf;

      setActionMap( context.getActionMap( this ) );

      list = new JList( new EventListModel<LinkValue>(model) );
      list.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( list );
      add( scrollPane, BorderLayout.CENTER );
      setPreferredSize( new Dimension( 200, 300 ) );
   }


   @org.jdesktop.application.Action
   public void execute()
   {
      LinksBuilder linksBuilder = new LinksBuilder( vbf );

      for (Object value : list.getSelectedValues())
      {
         LinkValue linkValue = (LinkValue) value;
         linksBuilder.addLink( linkValue );
      }
      selectedLinks = linksBuilder.newLinks();

      WindowUtils.findWindow( this ).dispose();
   }

   @org.jdesktop.application.Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public LinksValue getSelectedLinks()
   {
      return selectedLinks;
   }
}
