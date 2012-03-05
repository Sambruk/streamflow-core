/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.cases;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.client.util.popup.SelectionList;
import se.streamsource.streamflow.client.util.popup.ValueToLabelConverter;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static se.streamsource.streamflow.client.ui.workspace.cases.PdfPrintingDialog.CasePrintingOptions.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * A dialog to choose case pdf printing options.
 */
public class PdfPrintingDialog
   extends JPanel
{
   public enum CasePrintingOptions
   {
      contacts,
      submittedForms,
      conversations,
      attachments,
      caselog;   
   }
   
   private JList options;
   private List<String> selected = new ArrayList<String>(
         Arrays.asList( contacts.name(), submittedForms.name(), conversations.name(), attachments.name() )
   );
   private CaseOutputConfigDTO config;
   private JScrollPane pane = new JScrollPane( );
   
   @Structure
   ValueBuilderFactory vbf;
   
   public PdfPrintingDialog( @Service ApplicationContext context )
   {
      setLayout( new BorderLayout( ) );
      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));
      
      options = new SelectionList( Arrays.asList( contacts.name(), submittedForms.name(), conversations.name(), attachments.name(), caselog.name() ),
         selected,
         new ValueToLabelConverter()
         {
            public String convert(String value)
            {
               return text( valueOf( value.toString() ) );
            }
         },
         new ListSelectionListener(){

            public void valueChanged( ListSelectionEvent event )
            {
               if (!event.getValueIsAdjusting())
               {
                  String selectedValue = (String) ((JList) event.getSource()).getSelectedValue();
                  if (selectedValue != null)
                  {
                     if(selected.contains( selectedValue ))
                        selected.remove( selectedValue );
                     else
                        selected.add( selectedValue );

                     ((SelectionList)event.getSource()).clearSelection();
                  }
               }
         }
      });
      pane.setViewportView( options );
      
      add( pane, BorderLayout.CENTER );
      setPreferredSize( new Dimension( 100,110 ) );
   }

   @Action
   public void execute()
   {
      ValueBuilder<CaseOutputConfigDTO> builder = vbf.newValueBuilder( CaseOutputConfigDTO.class );

      for( String s : selected )
      {
         switch( CasePrintingOptions.valueOf( s ) )
         {
            case contacts :
               builder.prototype().contacts().set( true );
               break;
            case conversations :
               builder.prototype().conversations().set( true );
               break;
            case submittedForms :
               builder.prototype().submittedForms().set( true );
               break;
            case attachments :
               builder.prototype().attachments().set( true );
               break;
            case caselog :
               builder.prototype().caselog().set( true );
               break;
         }
      }
      
      config = builder.newInstance();
      
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {

      WindowUtils.findWindow( this ).dispose();
   }
   
   public CaseOutputConfigDTO getCaseOutputConfig()
   {
      return config;   
   }
}
