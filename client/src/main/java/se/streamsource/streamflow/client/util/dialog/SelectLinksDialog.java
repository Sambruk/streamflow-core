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

package se.streamsource.streamflow.client.util.dialog;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;

import javax.swing.*;
import java.awt.*;

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
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

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
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public LinksValue getSelectedLinks()
   {
      return selectedLinks;
   }
}
