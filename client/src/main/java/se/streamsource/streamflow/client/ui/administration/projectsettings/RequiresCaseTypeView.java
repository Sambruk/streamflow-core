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
package se.streamsource.streamflow.client.ui.administration.projectsettings;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.administration.DueOnNotificationSettingsDTO;
import se.streamsource.streamflow.api.administration.RequiresCaseTypeDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.ActionBinder;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static se.streamsource.dci.value.link.Links.withRel;

/**
 * JAVADOC
 */
public class RequiresCaseTypeView
      extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private RequiresCaseTypeModel model;

   private JCheckBox requiresCaseType = new JCheckBox("Aktiv");

   private DefaultFormBuilder builder;

   public RequiresCaseTypeView(@Service ApplicationContext context, @Uses RequiresCaseTypeModel model)
   {
      this.model = model;
      model.addObserver( this );

      FormLayout layout = new FormLayout( "150dlu, 2dlu, 50, 200", "pref" );
      setLayout(layout);
      setMaximumSize( new Dimension( Short.MAX_VALUE, 50 ) );
      builder = new DefaultFormBuilder( layout, this);
      builder.add( new JLabel(i18n.text(AdministrationResources.change_requires_case_type)), new CellConstraints(1,1));
      builder.add( requiresCaseType, new CellConstraints(3,1,2,1));

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      new ActionBinder(am).bind("changeRequiresCaseType", requiresCaseType);

      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      RequiresCaseTypeDTO settings = (RequiresCaseTypeDTO) model.getIndex();
      requiresCaseType.setSelected( settings.requiresCaseType().get() );
   }

   @org.jdesktop.application.Action
   public void changeRequiresCaseType()
   {
      model.changeRequiresCaseType(requiresCaseType.isSelected());
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "changedRequiresCaseType"), transactions ))
      {
         model.refresh();
      }
   }
}
