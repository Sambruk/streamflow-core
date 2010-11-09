package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The panel responsible for viewing gui components for attaching a file from a form.
 */
public class AttachmentFieldPanel
      extends AbstractFieldPanel
{
   private JButton attach;
   private JLabel attachment = new JLabel();

   public AttachmentFieldPanel( @Uses FieldSubmissionValue field,
                                @Service ApplicationContext context )
   {
      super( field );

      FormLayout formLayout = new FormLayout(
            "60dlu, 2dlu, 150dlu",
            "20dlu" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, this );

      ApplicationActionMap am = context.getActionMap( this );
      attach = new JButton( am.get( "attach" ) );

      formBuilder.add( attach, "1,1,1,1" );
      formBuilder.add( attachment, "3,1,1,1" );
   }

   @Override
   public String getValue()
   {
      return attachment.getText();
   }

   @Override
   public void setValue( String newValue )
   {
      attachment.setText( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      attach.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled( false );

            if (fileChooser.showDialog( AttachmentFieldPanel.this, i18n.text( WorkspaceResources.create_attachment ) ) == JFileChooser.APPROVE_OPTION)
            {
               binding.updateProperty( fileChooser.getSelectedFile() );
            }

         }
      });
   }
}
