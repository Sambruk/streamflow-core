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

package se.streamsource.streamflow.client.ui.workspace.cases;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.workspace.cases.info.CaseInfoModel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.resource.caze.CaseValue;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;


/**
 * JAVADOC
 */
public class SubCasesView
   extends JPanel
   implements TransactionListener
{
   private JList subCaseList;
   private JButton createSubCase;
   private CaseInfoModel model;
   private JButton caseButton = new JButton();
   private JButton parentCaseButton = new JButton();
   private EventList<LinkValue> eventList = new BasicEventList<LinkValue>(  );
   private JLabel parentLabel;
   private JLabel subcasesLabel;
   private JScrollPane subCaseListScroll;

   public SubCasesView(@Uses CommandQueryClient client, @Service ApplicationContext context, @Structure ObjectBuilderFactory obf)
   {
      setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

      setPreferredSize( new Dimension(150,0) );
      setMaximumSize( new Dimension(150,1000) );

      subCaseList = new JList(new EventListModel<LinkValue>(eventList));
      subCaseList.setCellRenderer( new LinkListCellRenderer(){
         @Override
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
         {
            JLabel component = (JLabel) super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

            LinkValue link = (LinkValue) value;
            Icon icon = i18n.icon( CaseResources.valueOf( "case_status_"+link.classes().get().toLowerCase()+"_icon" ),
                     i18n.ICON_16 );

            component.setIcon( icon );

            if (component.getText().equals(""))
            {
               component.setText( "<Empty description>" );
               component.setForeground( Color.lightGray );
            }

            return component;
         }
      });

      setActionMap(context.getActionMap(this));

      parentLabel = new JLabel("Parent");
      parentLabel.setForeground( Color.GRAY );
      parentLabel.setLabelFor( parentCaseButton );

      add(parentLabel);
      add(parentCaseButton);

      createSubCase = new JButton(getActionMap().get( "createSubCase" ));

      JLabel caseLabel = new JLabel( "Case", JLabel.RIGHT );
      caseLabel.setForeground( Color.GRAY );
      caseLabel.setLabelFor( caseButton );
      add( caseLabel );
      add(caseButton);

      subcasesLabel = new JLabel( "Subcases", JLabel.RIGHT );
      subcasesLabel.setForeground( Color.GRAY );
      add( subcasesLabel );
      subCaseListScroll = new JScrollPane( subCaseList );
      add( subCaseListScroll );
      add( createSubCase);
   }

   public void setModel(CaseInfoModel model)
   {
      this.model = model;
      model.addObserver( new Observer()
      {
         public void update( Observable o, Object arg )
         {
            CaseValue caseValue = (CaseValue) arg;
            if (caseValue.subcases().get().links().get().isEmpty())
            {
               subcasesLabel.setVisible( false );
               subCaseListScroll.setVisible( false );
            } else
            {
               EventListSynch.synchronize( caseValue.subcases().get().links().get(), eventList );
               subcasesLabel.setVisible( true );
               subCaseListScroll.setVisible( true );
            }

            caseButton.setText( caseValue.caseId().get() );

            if (caseValue.parentCase().get() != null)
            {
               parentCaseButton.setText( caseValue.parentCase().get().text().get() );
            } else
            {
               parentLabel.setVisible( false );
               parentCaseButton.setVisible( false );

            }

            if (caseValue.subcases().get().links().get().isEmpty() && caseValue.parentCase().get() == null)
            {
               JSplitPane parent = (JSplitPane) getParent();
               parent.setDividerLocation( 0.0 );
            } else
            {
               JSplitPane parent = (JSplitPane) getParent();
               parent.resetToPreferredSizes();
            }
         }
      } );
   }

   @Action
   public Task createSubCase()
   {
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.createSubCase();
         }
      };
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "createdSubCase", "removedSubCase", "changedStatus", "changedDescription" ), transactions ))
         model.refresh();
   }

   public JList getList()
   {
      return subCaseList;
   }

   public JButton getCaseButton()
   {
      return caseButton;
   }

   public JButton getParentCaseButton()
   {
      return parentCaseButton;
   }

   public CaseInfoModel getModel()
   {
      return model;
   }
}
