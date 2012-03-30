package se.streamsource.streamflow.client.ui.administration.casesettings;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

/**
 * Shows case priority settings per case type.
 */
public class CasePrioritySettingView extends JPanel implements Observer, TransactionListener
{
   @Structure
   Module module;

   private CasePrioritySettingModel model;
   private final ApplicationContext context;

   private JCheckBox visible = new JCheckBox(  );
   private JCheckBox mandatory = new JCheckBox( );

   public CasePrioritySettingView(@Service ApplicationContext context, @Uses CasePrioritySettingModel model)
   {
      this.context = context;
      this.model = model;
      this.model.addObserver( this );

      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 70", "pref, pref" );
      setLayout( layout );
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.append( i18n.text( AdministrationResources.casepriority_visible ), visible );

      builder.append( i18n.text( AdministrationResources.casepriority_mandatory ), mandatory );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      new ActionBinder( am ).bind( "updateCasePrioritySetting", visible );
      new ActionBinder( am ).bind( "updateCasePrioritySetting", mandatory );

      new RefreshWhenShowing( this, model );
   }

   public void update(Observable o, Object arg)
   {
      FormValue prioritySettings = (FormValue) model.getIndex();

      visible.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "visible" ) ) );
      mandatory.setSelected( Boolean.parseBoolean( prioritySettings.form().get().get( "mandatory" ) ) );
   }

   @Action
   public void updateCasePrioritySetting()
   {
      model.changeCasePrioritySetting( visible.isSelected(), mandatory.isSelected() );
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      model.refresh();
   }
}
