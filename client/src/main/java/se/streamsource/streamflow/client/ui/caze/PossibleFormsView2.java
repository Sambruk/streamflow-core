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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.util.WindowUtils;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.entity.EntityReference;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class PossibleFormsView2 extends JPanel
{
   private PossibleFormsModel model;
   private JXList forms;

   public PossibleFormsView2()
   {
      super.setLayout( new BorderLayout() );

      forms = new JXList();
      forms.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      forms.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "openFormWizard" );
      final javax.swing.Action openFormWizard = new AbstractAction()
      {
         public void actionPerformed( ActionEvent e )
         {
            PossibleFormsView2.this.openFormWizard();
         }
      };
      forms.getActionMap().put( "openFormWizard", openFormWizard );

      forms.addMouseListener( new MouseAdapter()
      {
         public void mouseClicked( MouseEvent me )
         {
            Object obj = forms.getSelectedValue();
            if (obj == null) return;
            if (me.getClickCount() == 2)
            {
               openFormWizard.actionPerformed( new ActionEvent( this,
                     ActionEvent.ACTION_PERFORMED,
                     "listItem" ) );
               me.consume();
            }
         }
      } );

      add( new JScrollPane( forms ), BorderLayout.CENTER );
   }

   public void setFormsModel( PossibleFormsModel modelForm )
   {
      this.model = modelForm;
      forms.setModel( new EventListModel( model.getForms() ) );
      forms.setCellRenderer( new LinkListCellRenderer() );
   }


   @Action
   public void openFormWizard()
   {

      final LinkValue link = (LinkValue) forms.getSelectedValue();
      FormSubmissionModel model = this.model.getFormSubmitModel( link.id().get() );

      Wizard wizard = WizardPage.createWizard( model.getTitle(), model.getPages(), new WizardPage.WizardResultProducer()
      {

         public Object finish( Map map ) throws WizardException
         {
            PossibleFormsView2.this.model.submit( EntityReference.parseEntityReference( link.id().get() ) );
            return null;
         }

         public boolean cancel( Map map )
         {
            PossibleFormsView2.this.model.discard( EntityReference.parseEntityReference( link.id().get() ) );
            return true;
         }
      } );
      Point onScreen = WindowUtils.findWindow( this ).getLocationOnScreen();
      WizardDisplayer.showWizard( wizard, new Rectangle( onScreen, new Dimension( 800, 600 ) ) );
   }
}
