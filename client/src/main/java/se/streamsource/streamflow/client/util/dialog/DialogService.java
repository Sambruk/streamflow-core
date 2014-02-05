/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.util.StreamflowButton;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static se.streamsource.streamflow.client.util.dialog.DialogService.Orientation.*;

/**
 * JAVADOC
 */
public class DialogService
{
   @Service
   ApplicationContext context;
   
   public enum Orientation
   {
      right,
      left
   }

   public void showOkCancelHelpDialog(Component owner, JComponent main)
   {
      JXDialog dialog = createDialog(owner, main, left);
      dialog.setVisible(true);
   }

   public void showOkCancelHelpDialog(Component owner, JComponent main,
                                      String title)
   {
      JXDialog dialog = createDialog(owner, main, left );
      dialog.setTitle(title);
      dialog.setVisible(true);
   }

   public void showOkCancelHelpDialog(Component owner, JComponent main,
                                      String title, Orientation orientation)
   {
      JXDialog dialog = createDialog(owner, main, orientation);
      dialog.setTitle(title);
      dialog.setVisible(true);
   }

   private JXDialog createDialog(Component owner, JComponent main, Orientation orientation )
   {
      Window window = WindowUtils.findWindow(owner);
      JXDialog dialog;
      if (window instanceof Frame)
         dialog = new JXDialog((Frame) window, main);
      else
         dialog = new JXDialog((Dialog) window, main);

      dialog.setModal(true);

      dialog.pack();

      if (owner instanceof StreamflowButton)
      {
         Point location = new Point(owner.getLocationOnScreen());

         switch( valueOf( orientation.name() ) )
         {
            case left:
               location.translate( 0, owner.getHeight() );
               break;
            case right:
               location.translate( owner.getWidth() - dialog.getWidth(), owner.getHeight() );
               break;
         }
         //location.translate(0, owner.getHeight());
         dialog.setLocation(location);
      } else
      {
         dialog.setLocationRelativeTo(SwingUtilities.getAncestorOfClass(
                 Frame.class, owner));
      }
      return dialog;
   }

   public void showOkDialog(Component owner, JComponent main)
   {
      JXDialog dialog = createOkDialog(owner, main);
      dialog.setVisible(true);
   }

   private JXDialog createOkDialog(Component owner, JComponent main)
   {
      Window window = WindowUtils.findWindow(owner);
      JXDialog dialog;
      if (window instanceof Frame)
         dialog = new JXDialog((Frame) window, main);
      else
         dialog = new JXDialog((Dialog) window, main);
      final JXDialog finalDialog = dialog;

      Action ok = main.getActionMap().get(JXDialog.EXECUTE_ACTION_COMMAND);
      if (ok == null)
         ok = context.getActionMap().get("cancel");

      dialog.getContentPane().setLayout(new BorderLayout());
      StreamflowButton okButton = new StreamflowButton(ok);
      dialog.getContentPane().add(BorderLayout.SOUTH, createOKBar(okButton));
      dialog.getContentPane().add(BorderLayout.CENTER, main);
      dialog.setMinimumSize(new Dimension(300, 100));
      dialog.pack();
      dialog.setLocationRelativeTo(owner);
      dialog.setModal(true);

      okButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            finalDialog.setVisible(false);
            WindowUtils.findWindow(finalDialog).dispose();
         }
      });

      return dialog;
   }

   private JXDialog createButtonLessDialog(Component owner, JComponent main)
   {
      Window window = WindowUtils.findWindow(owner);
      JXDialog dialog;
      if (window instanceof Frame)
         dialog = new JXDialog((Frame) window, main);
      else
         dialog = new JXDialog((Dialog) window, main);
      final JXDialog finalDialog = dialog;

      dialog.getContentPane().setLayout(new BorderLayout());
      dialog.getContentPane().add(BorderLayout.CENTER, main);
      dialog.setMinimumSize(new Dimension(300, 100));
      dialog.pack();
      dialog.setLocationRelativeTo(owner);
      dialog.setModal(true);
      return dialog;
   }

   private JPanel createHelpOKCancelApplyBar(StreamflowButton help, StreamflowButton ok,
         StreamflowButton cancel, StreamflowButton apply)
   {
      ButtonBarBuilder2 builder = new ButtonBarBuilder2();
      builder.addButton(help);
      builder.addUnrelatedGap();
      builder.addGlue();
      builder.addButton(new StreamflowButton[]{ok, cancel, apply});
      return builder.getPanel();
   }

   private JPanel createOKBar(StreamflowButton ok)
   {
      ButtonBarBuilder2 builder = new ButtonBarBuilder2();
      builder.addUnrelatedGap();
      builder.addGlue();
      builder.addButton(new StreamflowButton[]{ok});
      return builder.getPanel();
   }

   private JPanel createButtonBar(StreamflowButton ok, StreamflowButton[] extras)
   {
      ButtonBarBuilder2 builder = new ButtonBarBuilder2();
      builder.addButton(extras);
      builder.addUnrelatedGap();
      builder.addGlue();
      builder.addButton(new StreamflowButton[]{ok});
      return builder.getPanel();
   }

   private JPanel createHelpOKCancelBar(StreamflowButton help, StreamflowButton ok, StreamflowButton cancel)
   {
      ButtonBarBuilder2 builder = new ButtonBarBuilder2();
      builder.addButton(help);
      builder.addUnrelatedGap();
      builder.addGlue();
      builder.addButton(new StreamflowButton[]{ok, cancel});
      return builder.getPanel();
   }

   public void showOkDialog(Component component, JComponent main, String title)
   {
      JXDialog dialog = createOkDialog(component, main);
      dialog.setTitle(title);
      dialog.setVisible(true);
   }

   public void showButtonLessDialog(Component component, JComponent main, String title)
   {
      JXDialog dialog = createButtonLessDialog(component, main);
      dialog.setTitle(title);
      dialog.setVisible(true);
   }


   public void showMessageDialog(Component component, String message, String title)
   {
      MessageLabel messageLabel = new MessageLabel(message);
      messageLabel.setActionMap(context.getActionMap(messageLabel));
      JXDialog dialog = createOkDialog(component, messageLabel);
      dialog.setTitle(title);
      dialog.setVisible(true);
   }

   public static class MessageLabel
           extends JLabel
   {
      private MessageLabel(String text)
      {
         super( text );
         setBorder( BorderFactory.createEmptyBorder( 5, 15, 5, 15 ) );

      }

      @org.jdesktop.application.Action
      public void execute()
      {

      }
   }
}
