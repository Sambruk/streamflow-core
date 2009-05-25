/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.menu;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.workspace.TaskEntity;
import se.streamsource.streamflow.domain.contact.ContactRoleValue;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * JAVADOC
 */
public class NewTaskDialog
        extends JPanel
{
//   @Uses
//   NewTaskController controller;

    public NewTaskDialog(@Service final NewTaskModel model,
                         @Structure ObjectBuilderFactory obf,
                         @Structure ValueBuilderFactory vbf,
                         @Structure UnitOfWorkFactory uowf,
                         @Service ApplicationContext context
    )
    {
        setActionMap(context.getActionMap(this));

        TaskEntity taskEntity = model.task().get();

        ValueBuilder<ContactRoleValue> valueBuilder = vbf.newValueBuilder(ContactRoleValue.class);
        valueBuilder.prototype().contact().get().name().set("Rickard Öberg");
        valueBuilder.prototype().role().set("Medborgare");
        ContactRoleValue contactRole = valueBuilder.newInstance();

        taskEntity.addContact(contactRole);
/*
        taskEntity.description().set("A new task");
*/

/*
      final StateModel<ContactEntity.ContactState> stateModel = obf.newObjectBuilder(StateModel.class).use(ContactEntity.ContactState.class).newInstance();
      ContactEntity.ContactState template = stateModel.state();
      FormLayout layout = new FormLayout(
              "50dlu, 6dlu, default, 4dlu, left:pref", ""); // 5 columns; add rows later

      ResourceMap resources = context.getResourceMap(getClass());
      DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
      builder.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      builder.appendTitle(resources.getString("caller"));
      builder.nextLine();
      builder.append(resources.getString("name"), stateModel.bind(template.name()).to(new JTextField(50)));
      builder.nextLine();
      builder.append(resources.getString("phone"), stateModel.bind(template.phone()).to(new JTextField(50)));
      builder.nextLine();
      builder.append(resources.getString("address"), stateModel.bind(template.address()).to(new JTextArea(10,50)));
      builder.nextLine();
      builder.appendSeparator(resources.getString("sources"));
      builder.append("Eniro", new JButton(getActionMap().get("eniro")));

      stateModel.use(model.task());

      uowf.currentUnitOfWork().addStateChangeListener(new StateChangeListener()
      {
         public void notify(StateChange change)
         {
            stateModel.use(model.task());
         }
      });
*/
    }

    @Action
    void ok()
    {
//      controller.newTask();
        JOptionPane.showMessageDialog(this, "NYI");
    }
}
