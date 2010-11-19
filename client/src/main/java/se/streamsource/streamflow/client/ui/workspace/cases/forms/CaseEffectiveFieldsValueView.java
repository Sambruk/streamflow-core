/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends CaseSubmittedFormAbstractView
      implements ListEventListener<EffectiveFieldDTO>
{
   private CaseEffectiveFieldsValueModel model;
   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) );

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context,
                                        @Uses CommandQueryClient client,
                                        @Structure ObjectBuilderFactory obf )
   {
      model = obf.newObjectBuilder( CaseEffectiveFieldsValueModel.class ).use( client ).newInstance();
      model.getEventList().addListEventListener( this );

      setActionMap( context.getActionMap( this ) );

      new RefreshWhenVisible( this, model );
   }

   public void listChanged( ListEvent<EffectiveFieldDTO> listEvent )
   {
      panel().removeAll();
      final Map<String, DefaultFormBuilder> formBuilder = new LinkedHashMap<String, DefaultFormBuilder>();

      safeRead( model.getEventList(), new EventCallback<EffectiveFieldDTO>()
      {
         public void iterate( EffectiveFieldDTO effectiveFieldDTO )
         {
            String formName = effectiveFieldDTO.formName().get();
            if ( !formBuilder.containsKey( formName ))
            {
               JPanel formPanel = new JPanel();
               formPanel.setBorder( BorderFactory.createTitledBorder( formName ));
               formBuilder.put( formName, builder( formPanel ) );
            }

            JLabel label = new JLabel( effectiveFieldDTO.fieldName().get() + ":", SwingConstants.RIGHT );

            JComponent component = getComponent( effectiveFieldDTO.fieldValue().get(), effectiveFieldDTO.fieldType().get() );
            component.setToolTipText( effectiveFieldDTO.submitter().get()+", "+formatter.format( effectiveFieldDTO.submissionDate().get() ) );

            DefaultFormBuilder builder = formBuilder.get( formName );
            builder.append( label, component );
            builder.nextLine();
         }
      });

      for (DefaultFormBuilder builder : formBuilder.values())
      {
         panel().add( builder.getPanel() );
      }
      revalidate();
      repaint(  );
   }

   @Override
   protected FormAttachmentDownload getModel()
   {
      return model;
   }

   @Action
   public Task open( ActionEvent event )
   {
      return openAttachment( event );
   }

}