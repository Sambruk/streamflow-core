package se.streamsource.streamflow.client.ui.administration.casepriorities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
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
public class CasePriorityView
   extends JPanel implements Refreshable
{
   private ValueBinder values;
   private ActionBinder actions;
   private DefaultFormBuilder formBuilder;
   
   private JTextField name;
   //private JTextField color;
   private StreamflowJXColorSelectionButton color;
   
   private CasePriorityModel model;
   
   private Module module;
   
   public CasePriorityView( @Service ApplicationContext context, @Uses CasePriorityModel model, @Structure Module module )
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

      binding.appendWithLabel( AdministrationResources.name_label, name = new JTextField(), "name", "change");

      binding.appendWithLabel( AdministrationResources.name_show_color, color = new StreamflowJXColorSelectionButton(), "color", "change");
      color.setPreferredSize( new Dimension( 190,25 ) );
      

      new RefreshWhenShowing( this, this );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task change()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            ValueBuilder<CasePriorityValue> builder = module.valueBuilderFactory().newValueBuilder( CasePriorityValue.class );
            builder.prototype().name().set( name.getText() );
            builder.prototype().color().set( "" + color.getBackground().getRGB() );
            model.change( builder.newInstance() );
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
      if (matches( withNames( "changedPriority" ), transactions ))
      {
         refresh();
      }
   }
   
   
}
