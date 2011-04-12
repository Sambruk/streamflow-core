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

package se.streamsource.streamflow.client.ui.administration.resolutions;

import ca.odell.glazedlists.swing.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.util.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class SelectedResolutionsView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<SelectLinkDialog> labelsDialogs;

   public JList list;

   private SelectedResolutionsModel model;

   public SelectedResolutionsView( @Service ApplicationContext context,
                                   @Uses final CommandQueryClient client,
                                   @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( SelectedResolutionsModel.class ).use( client ).newInstance();
      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      list = new JList( new EventListModel<LinkValue>( model.getList() ) );

      list.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( list ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = labelsDialogs.use( model.getPossible() ).newInstance();
      dialog.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_resolution_title ) );

      if (dialog.getSelectedLinks() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.add( dialog.getSelectedLinks() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final Iterable<LinkValue> selected = (Iterable) Iterables.iterable( list.getSelectedValues() );

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            for (LinkValue linkValue : selected)
            {
               model.remove( linkValue );
            }
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}