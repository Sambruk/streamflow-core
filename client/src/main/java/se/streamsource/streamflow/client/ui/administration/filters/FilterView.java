/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.filters;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static se.streamsource.dci.value.link.Links.withRel;

import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.data.Form;

import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.BindingFormBuilder2;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.client.util.i18n;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TODO
 */
public class FilterView
      extends JPanel
      implements Refreshable
{
   private FilterModel model;
   private Module module;

   private ActionBinder actions;
   private ValueBinder values;
   protected JTextField nameField;
   protected JCheckBox enabled;
   protected JRadioButton all;
   protected JRadioButton any;
   protected DefaultFormBuilder formBuilder;
   protected RulesView rulesView;
   protected ActionsView actionsView;
   protected ButtonGroup matching;

   public FilterView(@Service ApplicationContext context, @Uses final FilterModel model, @Structure Module module)
   {
      this.model = model;
      this.module = module;

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      FormLayout layout = new FormLayout("70dlu, 2dlu, 200:grow", "pref, pref, pref, pref, 20dlu, pref, pref");

      setLayout(layout);
      formBuilder = new DefaultFormBuilder(layout, this);

      actions = new ActionBinder(am);
      values = module.objectBuilderFactory().newObject(ValueBinder.class);
      BindingFormBuilder2 binding = new BindingFormBuilder2(formBuilder, actions, values, context.getResourceMap(getClass()));
      nameField = new JTextField();
      binding.appendWithLabel(AdministrationResources.name_label, nameField, "name", "update");
      enabled = new JCheckBox();
      binding.appendWithLabel(AdministrationResources.enabled, enabled, "enabled", "update");
      all = new JRadioButton();
      all.setActionCommand("all");
      binding.appendWithLabel(AdministrationResources.all, all, "matching", "update");
      any = new JRadioButton();
      any.setActionCommand("any");
      binding.appendWithLabel(AdministrationResources.any, any, "matching", "update");
      matching = new ButtonGroup();
      matching.add(any);
      matching.add(all);

      formBuilder.nextLine();

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      model.refresh();

      if (rulesView == null)
      {
         formBuilder.appendSeparator(i18n.text(AdministrationResources.filter_rules));
         rulesView = module.objectBuilderFactory().newObjectBuilder(RulesView.class).use(model.newResourceModel(first(filter(withRel("rules"), model.getResourceValue().resources().get())))).newInstance();
         formBuilder.append(rulesView, 3);

         formBuilder.appendSeparator(i18n.text(AdministrationResources.filter_actions));
         actionsView = module.objectBuilderFactory().newObjectBuilder(ActionsView.class).use(model.newResourceModel(first(filter(withRel("actions"), model.getResourceValue().resources().get())))).newInstance();
         formBuilder.append(actionsView, 3);

         revalidate();
      }


      values.update(model.getIndex());
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task update()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            Form form = model.getIndex();
            form.set("name", nameField.getText());
            form.set("enabled", enabled.isSelected() + "");
            form.set("matching", matching.getSelection().getActionCommand());
            model.update(form);
         }
      };
   }
}
