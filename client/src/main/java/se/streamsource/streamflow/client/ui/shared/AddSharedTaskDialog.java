/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.shared;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import static se.streamsource.streamflow.client.ui.shared.SharedInboxResources.*;

import javax.swing.JPanel;

/**
 * JAVADOC
 */
public class AddSharedTaskDialog
        extends JPanel
{
    @Structure
    UnitOfWorkFactory uowf;

    private StateBinder sharedTaskBinder;
    private ValueBuilder<NewSharedTaskCommand> commandBuilder;
    private SharedInboxModel inboxModel;

    public AddSharedTaskDialog(@Service ApplicationContext appContext,
                               @Structure ValueBuilderFactory vbf,
                               @Service SharedInboxModel inboxModel
    )
    {
        this.inboxModel = inboxModel;
        setActionMap(appContext.getActionMap(this));

        setName(appContext.getResourceMap(SharedInboxResources.class).getString(SharedInboxResources.add_shared_task_title.toString()));

        FormLayout layout = new FormLayout(
                "200dlu",
/*
                "right:max(40dlu;p), 4dlu, 200dlu, 7dlu, " // 1st major column
                        + "right:max(40dlu;p), 4dlu, 80dlu",        // 2nd major column
*/
                "");                                      // add rows dynamically
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.setDefaultDialogBorder();

        sharedTaskBinder = new StateBinder();
        sharedTaskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        NewSharedTaskCommand template = sharedTaskBinder.bindingTemplate(NewSharedTaskCommand.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, sharedTaskBinder);
        bb.appendLine(description_label, TEXTFIELD, template.description())
                .appendLine(SharedInboxResources.note_label, TEXTAREA, template.note());

        // Create command builder
        commandBuilder = vbf.newValueBuilder(NewSharedTaskCommand.class);

        sharedTaskBinder.updateWith(commandBuilder.prototype());
    }

    @Action
    public void execute()
            throws Exception
    {
        // Create command instance
        final NewSharedTaskCommand command = commandBuilder.newInstance();

        inboxModel.newTask(command);

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        uowf.currentUnitOfWork().discard();
        WindowUtils.findWindow(this).dispose();
    }

    public ValueBuilder<NewSharedTaskCommand> getCommandBuilder()
    {
        return commandBuilder;
    }
}
