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

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.text.MessageFormat;

/**
 * A general confirmation dialog
 */
public class ConfirmationDialog
      extends JPanel
{
   boolean confirm;
   private JLabel msgLabel;

   public ConfirmationDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      JPanel dialog = new JPanel( new BorderLayout() );
      dialog.add( msgLabel = new JLabel( i18n.text( StreamflowResources.proceed_label ) ), BorderLayout.CENTER );
      add( dialog, BorderLayout.NORTH );
   }

   public boolean isConfirmed()
   {
      return confirm;
   }

   public void setRemovalMessage( String description )
   {
      msgLabel.setText( new MessageFormat( i18n.text( StreamflowResources.removal_confirmation )).format( new Object[]{description} ));
   }

   @org.jdesktop.application.Action
   public void execute()
   {
      confirm = true;

      WindowUtils.findWindow( this ).dispose();
   }

   @org.jdesktop.application.Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
