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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.dialog.SelectLinksDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/**
 * JAVADOC
 */
public class VisibilityRuleValuesView
      extends JPanel
   implements TransactionListener
{
   private JXList elementList;

   private VisibilityRuleValuesModel model;

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public VisibilityRuleValuesView( @Service ApplicationContext context,
                                    @Uses VisibilityRuleValuesModel model )
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("4dlu, 4dlu, 4dlu, 4dlu"));

      JScrollPane scrollPanel = new JScrollPane();
      ActionMap am = context.getActionMap( this );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( am.get( "remove" ) ) );
      toolbar.add( new StreamflowButton( am.get( "rename" ) ) );

      model.refresh();
      elementList = new JXList( new EventListModel<String>(model.getEventList()) );
      elementList.setCellRenderer( new DefaultListCellRenderer()
      {
         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            if ("".equals( o ))
            {
               Component cell = super.getListCellRendererComponent( jList, i18n.text( WorkspaceResources.name_label ), i, b, b1);
               cell.setForeground( Color.GRAY );
               return cell;
            }
            return super.getListCellRendererComponent( jList, o, i, b, b1 );    //To change body of overridden methods use File | Settings | File Templates.
         }
      });

      scrollPanel.setViewportView( elementList );

      add( scrollPanel, BorderLayout.CENTER );
      add( toolbar, BorderLayout.SOUTH );

      elementList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );
      elementList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "rename" ) ) );
      elementList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

   }

   @org.jdesktop.application.Action
   public Task add()
   {
      EventList<LinkValue> linkValues = model.possiblePredefinedRuleValues();
      if( linkValues.isEmpty() )
      {
         final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

         dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.name_label ));

         if ( !Strings.empty( dialog.name() ) )
         {
            if ( dialog.name().contains( "[" ) || dialog.name().contains( "]" ))
            {
               dialogs.showOkDialog( this, new JLabel( i18n.text( AdministrationResources.no_such_character )), i18n.text( AdministrationResources.illegal_name ) );
               return null;
            } else
            {
               return new CommandTask()
               {
                  @Override
                  public void command()
                        throws Exception
                  {
                     model.addElement( dialog.name() );
                  }
               };
            }
         }
         else return null;
      } else
      {
         final SelectLinksDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinksDialog.class)
               .use(linkValues).newInstance();

         dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.name_label ));

         if ( dialog.getSelectedLinks() != null )
         {

               return new CommandTask()
               {
                  @Override
                  public void command()
                        throws Exception
                  {
                     model.addElements( dialog.getSelectedLinks() );
                  }
               };
         }
         else return null;
      }
   }


   @org.jdesktop.application.Action
   public Task remove( )
   {
      final int index = elementList.getSelectedIndex();

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( elementList.getStringAt( index ));

      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (index != -1 && dialog.isConfirmed() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.removeElement( index );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task rename()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);

      dialogs.showOkCancelHelpDialog( this, dialog , i18n.text( AdministrationResources.rename ));

      if ( !Strings.empty( dialog.name() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeElementName( dialog.name(), elementList.getSelectedIndex() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}