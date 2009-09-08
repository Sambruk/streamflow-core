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

package se.streamsource.streamflow.client.ui.task;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.CHECKBOX;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTAREA;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;

import javax.swing.*;
import java.util.Date;

/**
 * JAVADOC
 */
public class AddCommentDialog
        extends JPanel
{
    private StateBinder TaskBinder;
    private ValueBuilder<NewCommentCommand> commandBuilder;
    public NewCommentCommand command;

    public AddCommentDialog(@Service ApplicationContext appContext,
                            @Structure ValueBuilderFactory vbf,
                            @Uses EntityReference user
    )
    {
        setActionMap(appContext.getActionMap(this));

        setName(i18n.text(WorkspaceResources.add_comment_title));

        FormLayout layout = new FormLayout(
                "200dlu",
                "");                                      // add rows dynamically
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.setDefaultDialogBorder();

        TaskBinder = new StateBinder();
        TaskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        NewCommentCommand template = TaskBinder.bindingTemplate(NewCommentCommand.class);


        BindingFormBuilder bb = new BindingFormBuilder(builder, TaskBinder);
        bb.appendLine(WorkspaceResources.comment_public_label, CHECKBOX, template.isPublic());
        bb.appendLine(WorkspaceResources.comment_text_label, TEXTAREA, template.text());

        // Create command builder
        commandBuilder = vbf.newValueBuilder(NewCommentCommand.class);
        commandBuilder.prototype().commenter().set(user);

        TaskBinder.updateWith(commandBuilder.prototype());
    }

    @Action
    public void execute()
            throws Exception
    {
        // Create command instance
        commandBuilder.prototype().creationDate().set(new Date());
        command = commandBuilder.newInstance();

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

    public NewCommentCommand command()
    {
        return command;
    }
}