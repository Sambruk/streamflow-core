/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

public class CaseLabelsView
      extends JPanel
      implements ListEventListener, TransactionListener
{
   @Service
   private DialogService dialogs;

   @Uses
   private ObjectBuilder<CaseLabelsDialog> labelSelectionDialog;

   private CaseLabelsModel model;

   private boolean useBorders;

   public CaseLabelsView(@Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      setActionMap( context.getActionMap(this ));

      model = obf.newObjectBuilder( CaseLabelsModel.class ).use( client ).newInstance();
      model.getLabels().addListEventListener( this );

      setLayout( new FlowLayout( FlowLayout.LEFT ) );
      //setBorder( BorderFactory.createLineBorder( Color.BLUE, 1));
      new RefreshWhenVisible( this, model );
   }

   public CaseLabelsModel getModel()
   {
      return model;
   }

   public void setEnabled( boolean enabled )
   {
      for (Component component : getComponents())
      {
         component.setEnabled( enabled );
      }
   }

   public void listChanged( ListEvent listEvent )
   {
      removeAll();

      for (int i = 0; i < model.getLabels().size(); i++)
      {
         LinkValue linkValue = model.getLabels().get( i );
         RemovableLabel label = new RemovableLabel( linkValue, useBorders );
         label.addActionListener( getActionMap().get("remove" ));

         add( label );
      }

      revalidate();
      repaint();
   }

   @Action
   public Task addLabel()
   {
      final CaseLabelsDialog dialog = labelSelectionDialog.use(
            model.getPossibleLabels() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog );

      if (dialog.getSelectedLabels() != null)
      {
         return new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               for (LinkValue listItemValue : dialog.getSelectedLabels())
               {
                  model.addLabel( listItemValue );
               }
            }
         };
      } else
         return null;
   }


   @Action
   public Task remove( final ActionEvent e )
   {
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            Component component = ((Component) e.getSource());
            RemovableLabel label = (RemovableLabel) component.getParent();
            model.removeLabel( label.link() );
         }
      };
   }

   public void useBorders( boolean useBorders )
   {
      this.useBorders = useBorders;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      model.notifyTransactions(transactions);
   }
}
