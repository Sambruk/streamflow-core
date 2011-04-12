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

package se.streamsource.streamflow.client.ui.workspace.search;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.resource.user.profile.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * Save a search.
 */
public class SaveSearchDialog
        extends JPanel
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   public JTextField name;
   public JTextField query;

   PerspectiveValue search;

   public SaveSearchDialog(@Service ApplicationContext context)
   {
      super(new BorderLayout());

      setActionMap(context.getActionMap(this));
      getActionMap().put(JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel"));

      FormLayout layout = new FormLayout("40dlu, 5dlu, 120dlu:grow", "pref, pref");

      JPanel form = new JPanel(layout);
      form.setFocusable(false);
      DefaultFormBuilder builder = new DefaultFormBuilder(layout,
              form);

      name = new JTextField();
      query = new JTextField();

      builder.add(new JLabel(i18n.text(WorkspaceResources.name_label)));
      builder.nextColumn(2);
      builder.add(name);

      builder.nextLine();
      builder.add(new JLabel(i18n.text(WorkspaceResources.query_label)));
      builder.nextColumn(2);
      builder.add(query);

      add(form, BorderLayout.CENTER);
   }

   public PerspectiveValue search()
   {
      return search;
   }

   @Action
   public void execute()
   {
      if (!Strings.empty(name.getText()) && !Strings.empty(query.getText()))
      {
         ValueBuilder<PerspectiveValue> builder = vbf.newValueBuilder(PerspectiveValue.class);
         builder.prototype().name().set(name.getText());
         builder.prototype().query().set(query.getText());

         search = builder.newInstance();

         WindowUtils.findWindow(this).dispose();
      } else
      {
         dialogs.showOkDialog(WindowUtils.findWindow(this), new JLabel(i18n.text(WorkspaceResources.incomplete_data)));
      }
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow(this).dispose();
   }

   public void presetQuery(String query)
   {
      this.query.setText(query);
   }
}