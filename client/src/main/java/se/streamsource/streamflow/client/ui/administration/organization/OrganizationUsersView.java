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

package se.streamsource.streamflow.client.ui.administration.organization;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.LinksQueryListModel;
import se.streamsource.streamflow.client.ui.administration.SelectLinksDialog;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class OrganizationUsersView
      extends JPanel
{
   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   public JList participantList;

   private LinksListModel model;
   public SortedList<LinkValue> linkValues;

   public OrganizationUsersView( @Service ApplicationContext context, @Uses LinksListModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      model.refresh();
      
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      linkValues = new SortedList<LinkValue>(model.getEventList(), new LinkComparator());
      participantList = new JList( new EventListModel<LinkValue>( linkValues ) );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( participantList );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      participantList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenVisible( model, this );
   }

   @org.jdesktop.application.Action
   public void add() throws ResourceException
   {
      LinksQueryListModel dialogModel = obf.newObjectBuilder( LinksQueryListModel.class )
            .use( model.getClient(), "possibleusers" ).newInstance();
      dialogModel.refresh();
      SelectLinksDialog dialog = obf.newObjectBuilder( SelectLinksDialog.class )
            .use( dialogModel.getEventList() ).newInstance();
      dialogs.showOkCancelHelpDialog(
            WindowUtils.findWindow( this ),
            dialog,
            text( AdministrationResources.join_organization ) );

      if (dialog.getSelectedLinks() != null)
      {
         for (LinkValue linkValue : dialog.getSelectedLinks().links().get())
         {
            model.getClient().postLink( linkValue);
         }
         model.refresh();
      }
   }

   @org.jdesktop.application.Action
   public void remove() throws ResourceException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         for (int index : participantList.getSelectedIndices())
         {
            LinkValue user = linkValues.get( index );
            model.getClient().getClient( user.href().get() ).delete();
         }

         model.refresh();

         participantList.clearSelection();
      }
   }
}
