/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.infrastructure.ui;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.client.StreamFlowApplication;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JAVADOC
 */
public class DialogService
{
    @Structure
    UnitOfWorkFactory uowf;

    @Service
    StreamFlowApplication application;

    public void showOkCancelHelpDialog(Component component, JComponent main)
    {
        Window window = WindowUtils.findWindow(component);
        JXDialog dialog;
        if (window instanceof Frame)
            dialog = new JXDialog((Frame) window, main);
        else
            dialog = new JXDialog((Dialog) window, main);
        final JXDialog finalDialog = dialog;

        dialog.setModal(true);

/*
      Action ok = main.getActionMap().get("ok");
      Action cancel = main.getActionMap().get("cancel");
      Action help = main.getActionMap().get("help");
*/

/*
      dialog.setTitle(title);
      dialog.getContentPane().setLayout(new BorderLayout());
      dialog.getContentPane().add(BorderLayout.SOUTH, createHelpOKCancelBar(new JButton(help),
              new JButton(ok),
              new JButton(cancel)));
      dialog.getContentPane().add(BorderLayout.CENTER, main);
*/
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.windowForComponent(component));
        dialog.setVisible(true);
    }

    public void showOkDialog(Component component, JComponent main)
    {
        Window window = WindowUtils.findWindow(component);
        JXDialog dialog;
        if (window instanceof Frame)
            dialog = new JXDialog((Frame) window, main);
        else
            dialog = new JXDialog((Dialog) window, main);
        final JXDialog finalDialog = dialog;

        Action ok = main.getActionMap().get(JXDialog.EXECUTE_ACTION_COMMAND);
        if (ok == null)
            ok = application.getContext().getActionMap().get(JXDialog.EXECUTE_ACTION_COMMAND);

        dialog.getContentPane().setLayout(new BorderLayout());
        JButton okButton = new JButton(ok);
        dialog.getContentPane().add(BorderLayout.SOUTH, createOKBar(okButton));
        dialog.getContentPane().add(BorderLayout.CENTER, main);
        dialog.setMinimumSize(new Dimension(300, 200));
        dialog.pack();
        dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                finalDialog.setVisible(false);
            }
        });
    }

    private JPanel createHelpOKCancelApplyBar(
            JButton help, JButton ok, JButton cancel, JButton apply)
    {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addButton(help);
        builder.addUnrelatedGap();
        builder.addGlue();
        builder.addButton(new JButton[]{ok, cancel, apply});
        return builder.getPanel();
    }

    private JPanel createOKBar(JButton ok)
    {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addUnrelatedGap();
        builder.addGlue();
        builder.addButton(ok);
        return builder.getPanel();
    }

    private JPanel createHelpOKCancelBar(
            JButton help, JButton ok, JButton cancel)
    {
        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
        builder.addButton(help);
        builder.addUnrelatedGap();
        builder.addGlue();
        builder.addButton(new JButton[]{ok, cancel});
        return builder.getPanel();
    }
}
