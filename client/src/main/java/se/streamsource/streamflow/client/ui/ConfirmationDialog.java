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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * A general confirmation dialog
 */
public class ConfirmationDialog
      extends JPanel
{
   boolean confirm;

   public ConfirmationDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      JPanel dialog = new JPanel( new BorderLayout() );
      dialog.add( new JLabel( i18n.text( StreamflowResources.proceed_label ) ), BorderLayout.CENTER );
      add( dialog, BorderLayout.NORTH );
   }

   public boolean isConfirmed()
   {
      return confirm;
   }

   @org.jdesktop.application.Action
   public void execute()
   {
      confirm = true;

      WindowUtils.findWindow( this ).dispose();
   }

   @org.jdesktop.application.Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
