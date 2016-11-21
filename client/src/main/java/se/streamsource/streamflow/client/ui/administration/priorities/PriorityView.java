/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.priorities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.BindingFormBuilder2;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.StreamflowJXColorSelectionButton;
import se.streamsource.streamflow.client.util.ValueBinder;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * Case priority view.
 */
public class PriorityView
   extends JPanel implements Refreshable
{
   private ValueBinder values;
   private ActionBinder actions;
   private DefaultFormBuilder formBuilder;
   
   private JTextField name;
   //private JTextField color;
   private StreamflowJXColorSelectionButton color;
   
   private PriorityModel model;
   
   private Module module;
   
   public PriorityView( @Service ApplicationContext context, @Uses PriorityModel model, @Structure Module module )
   {
      this.model = model;
      this.module = module;

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      FormLayout layout = new FormLayout("70dlu, 2dlu, 200", "pref, pref");

      setLayout(layout);
      formBuilder = new DefaultFormBuilder(layout, this);

      actions = new ActionBinder(am);
      values = module.objectBuilderFactory().newObject(ValueBinder.class);
      BindingFormBuilder2 binding = new BindingFormBuilder2(formBuilder, actions, values, context.getResourceMap(getClass()));

      binding.appendWithLabel( AdministrationResources.name_label, name = new JTextField(), "text", "changeDescription");

      binding.appendWithLabel( AdministrationResources.name_show_color, color = new StreamflowJXColorSelectionButton(), "color", "changeColor");
      color.setPreferredSize( new Dimension( 190,25 ) );
      

      new RefreshWhenShowing( this, this );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeColor()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeColor( "" + color.getBackground().getRGB() );
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task changeDescription()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.changeDescription( name.getText() );
         }
      };
   }
   
   public void refresh()
   {
      model.refresh();
      values.update( model.getIndex() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames( "changedColor", "changedDescription" ), transactions ))
      {
         refresh();
      }
   }
   
   
}
