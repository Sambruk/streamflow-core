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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

import javax.swing.*;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * JAVADOC
 */
public class CaseSubmittedFormView
      extends CaseSubmittedFormAbstractView
      implements ListEventListener<FieldDTO>
{
   private CaseSubmittedFormModel model;

   public CaseSubmittedFormView(@Service ApplicationContext context, @Uses CommandQueryClient client,
                                @Structure ObjectBuilderFactory obf, @Uses Integer index, @Structure  ValueBuilderFactory vbf)
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( index );
      model = obf.newObjectBuilder( CaseSubmittedFormModel.class ).use( client, builder.newInstance() ).newInstance();
      model.getEventList().addListEventListener( this );

      setActionMap( context.getActionMap( this ) );

      new RefreshWhenShowing( this, model );
   }

   public void listChanged( ListEvent<FieldDTO> listEvent )
   {
      panel().removeAll();
      final DefaultFormBuilder builder = builder( panel() );

      safeRead( model.getEventList(), new EventCallback<FieldDTO>() {
         public void iterate( FieldDTO field )
         {
            JLabel label = new JLabel( field.field().get(), SwingConstants.LEFT);
            label.setFont(  label. getFont().deriveFont( Font.BOLD ) );
            JComponent component = getComponent( field.value().get(), field.fieldType().get() );

            builder.append( label );
            builder.nextLine();
            builder.append( component );
            builder.nextLine();
         }
      });
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