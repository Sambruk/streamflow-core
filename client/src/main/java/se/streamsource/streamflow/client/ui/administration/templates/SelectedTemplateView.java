/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.administration.templates;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.util.DialogService;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.RemovableLabel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.resource.organization.SelectedTemplateValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;


public class SelectedTemplateView extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   protected ObjectBuilder<SelectLinkDialog> templateDialog;

   private StateBinder selectedTemplateBinder;

   JButton templateButton;
   RemovableLabel selectedTemplate = new RemovableLabel();

   private SelectedTemplateModel model;

   public SelectedTemplateView( @Service ApplicationContext appContext,
                                @Uses CommandQueryClient client,
                                @Structure ObjectBuilderFactory obf )
   {
      this.model = obf.newObjectBuilder( SelectedTemplateModel.class ).use( client.getClient( "../template/" ) ).newInstance();
      model.addObserver( this );

      selectedTemplateBinder = obf.newObject( StateBinder.class );
      selectedTemplateBinder.addObserver( this );
      selectedTemplateBinder.addConverter( new StateBinder.Converter()
      {
         public Object toComponent( Object value )
         {
            if (value instanceof LinkValue)
            {
               return ((LinkValue) value).text().get();
            } else
               return value;
         }

         public Object fromComponent( Object value )
         {
            return value;
         }
      } );
      selectedTemplateBinder.setResourceMap( appContext.getResourceMap( getClass() ) );
      SelectedTemplateValue template = selectedTemplateBinder
            .bindingTemplate( SelectedTemplateValue.class );

      selectedTemplate.setFont( selectedTemplate.getFont().deriveFont(
            Font.BOLD ) );

      FormLayout layout = new FormLayout( "60dlu, 5dlu, 150:grow", "pref, 2dlu, default:grow" );

      JPanel panel = new JPanel( layout );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            panel );
      builder.setBorder( Borders.createEmptyBorder( Sizes.DLUY8,
            Sizes.DLUX4, Sizes.DLUY2, Sizes.DLUX8 ) );

      CellConstraints cc = new CellConstraints();

      setActionMap( appContext.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( appContext.getActionMap(
            SelectedTemplateView.class, this ) );

      ActionMap am = getActionMap();

      // Select template
      javax.swing.Action templateAction = am.get( "template" );
      templateButton = new JButton( templateAction );

      templateButton.registerKeyboardAction( templateAction, (KeyStroke) templateAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      templateButton.setHorizontalAlignment( SwingConstants.LEFT );

      builder.add( templateButton, cc.xy( 1, 3, CellConstraints.FILL, CellConstraints.TOP ) );

      builder.add( selectedTemplateBinder.bind( selectedTemplate, template.selectedTemplate() ),
            new CellConstraints( 3, 3, 1, 1, CellConstraints.LEFT, CellConstraints.TOP, new Insets( 3, 0, 0, 0 ) ) );

      add( panel, BorderLayout.CENTER );

      selectedTemplateBinder.updateWith( model.getSelectedTemplateValue() );

      new RefreshWhenVisible( this, model );
   }


   @Action
   public Task template()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            SelectLinkDialog dialog = templateDialog.use(
                  i18n.text( WorkspaceResources.choose_template ),
                  model.getPossibleTemplates() ).newInstance();

            dialogs.showOkCancelHelpDialog( templateButton, dialog );

            if (dialog.getSelected() != null)
            {
               model.setTemplate( dialog.getSelected() );
            }
         }
      };

   }


   public void update( Observable o, Object arg )
   {

      if (o == selectedTemplateBinder)
      {
         final Property property = (Property) arg;
         if (property.qualifiedName().name().equals( "selectedTemplate" ))
         {
            new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.removeTemplate();
               }
            }.execute();
         }
      } else
      {
         selectedTemplateBinder.updateWith( model.getSelectedTemplateValue() );
      }
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames(
            "selectedTemplateAdded", "selectedTemplateRemoved" ), transactions ))
      {
         model.refresh();
      }
   }
}
