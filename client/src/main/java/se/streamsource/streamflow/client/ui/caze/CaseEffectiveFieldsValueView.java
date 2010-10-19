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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.DateFunctions;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends JScrollPane
      implements TransactionListener, ListEventListener<EffectiveFieldDTO>
{
   private CaseEffectiveFieldsValueModel model;

   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) );

   private JPanel forms = new JPanel();

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      forms.setLayout( new BoxLayout( forms, BoxLayout.Y_AXIS ) );

      model = obf.newObjectBuilder( CaseEffectiveFieldsValueModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );

      model.getEventList().addListEventListener( this );

      setViewportView( forms );

      new RefreshWhenVisible( this, model );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, Events.withNames( "submittedForm" ) ))
      {
         model.refresh();
      }
   }

   public void listChanged( ListEvent<EffectiveFieldDTO> listEvent )
   {
      EventList<EffectiveFieldDTO> eventList = model.getEventList();
      eventList.getReadWriteLock().readLock().lock();
      try
      {
         forms.removeAll();
         Set<String> formNames = new LinkedHashSet<String>();
         for (EffectiveFieldDTO effectiveFieldDTO : eventList)
         {
            formNames.add( effectiveFieldDTO.formName().get() );
         }

         for (final String formName : formNames)
         {
            JPanel formPanel = new JPanel();
            FormLayout formLayout = new FormLayout("70dlu, 5dlu, 150dlu:grow","");
            DefaultFormBuilder builder = new DefaultFormBuilder( formLayout, formPanel );

            for (EffectiveFieldDTO effectiveFieldDTO : eventList)
            {
               if (effectiveFieldDTO.formName().get().equals(formName))
               {
                  String value = effectiveFieldDTO.fieldValue().get();
                  if (effectiveFieldDTO.fieldType().get().equals( DateFieldValue.class.getName() ))
                  {
                     value = formatter.format( DateFunctions.fromString( value ) );
                  } else if (effectiveFieldDTO.fieldType().get().equals( TextAreaFieldValue.class.getName() ))
                  {
                     value = "<html>"+value.replace( "\n", "<br/>" )+"</html>";
                  }
                  builder.append( new JLabel(effectiveFieldDTO.fieldName().get()+":", SwingConstants.RIGHT), 1 );
                  JLabel jLabel = new JLabel( value );
                  jLabel.setToolTipText( effectiveFieldDTO.submitter().get()+", "+formatter.format( effectiveFieldDTO.submissionDate().get() ) );
                  jLabel.setBorder( BorderFactory.createEtchedBorder());
                  builder.append( jLabel );
                  builder.nextLine();
               }
            }

            formPanel.setBorder( BorderFactory.createTitledBorder(formName ));
            
            forms.add( formPanel );
         }
         revalidate();
         repaint(  );
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }finally
      {
         eventList.getReadWriteLock().readLock().unlock();
      }
   }
}