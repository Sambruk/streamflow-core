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

package se.streamsource.streamflow.client.ui.administration.groups;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class AddParticipantDialog
        extends JPanel
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    GroupModel groupModel;

    public EntityReferenceValue query;

    public AddParticipantDialog(@Service ApplicationContext context, @Structure ValueBuilderFactory vbf)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        JPanel participantForm = new JPanel();
        add(participantForm, BorderLayout.NORTH);
        FormLayout layout = new FormLayout(
                "200dlu", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, participantForm);
        builder.setDefaultDialogBorder();

        StateBinder settingsBinder = new StateBinder();
        settingsBinder.setResourceMap(context.getResourceMap(GroupsResources.class));
        query = vbf.newValueBuilder(EntityReferenceValue.class).prototype();

        BindingFormBuilder bb = new BindingFormBuilder(builder, settingsBinder);
        bb.appendLine(GroupsResources.name_label, TEXTFIELD, query.entity());
    }

    @Action
    public void execute()
    {
        try
        {
            groupModel.addParticipant(query);

            WindowUtils.findJDialog(this).dispose();

        } catch (Exception e)
        {
            // TODO
            e.printStackTrace();
        }
    }

    @Action
    public void close()
    {
        WindowUtils.findJDialog(this).dispose();
    }
}