package se.streamsource.streamflow.client.ui.workspace.cases;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
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
      conversations,
      submittedForms,
      attachments,
      caselog;   
   }
   
   private JList options;
   private List<String> selected = new ArrayList<String>();
   private CaseOutputConfigDTO config;
   
   @Structure
   ValueBuilderFactory vbf;
   
   public PdfPrintingDialog( @Service ApplicationContext context )
   {
      setLayout( new BorderLayout( ) );
      setActionMap( context.getActionMap( this ) );
      
      options = new SelectionList( Arrays.asList( contacts.name(), conversations.name(), submittedForms.name(), attachments.name(), caselog.name() ), 
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
                  }
               }
         }
      });
      
      add(options, BorderLayout.CENTER);
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
