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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.ToolTipTableCellRenderer;
import se.streamsource.streamflow.domain.contact.ContactValue;

import com.jgoodies.forms.factories.Borders;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends JPanel
   implements TransactionListener
{
   public ValueBuilder<ContactValue> valueBuilder;
   private JXTable effectiveValueTable;
   public RefreshWhenVisible refresher;
   private CaseEffectiveFieldsValueModel model;

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );

      model = obf.newObjectBuilder( CaseEffectiveFieldsValueModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );
      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      effectiveValueTable = new JXTable( model );
      effectiveValueTable.setDefaultRenderer( Object.class, new ToolTipTableCellRenderer() );

      JScrollPane effectiveFields = new JScrollPane();

      effectiveFields.setViewportView( effectiveValueTable );

      add( effectiveFields, BorderLayout.CENTER );

      refresher = new RefreshWhenVisible( this, model );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches(transactions, Events.withNames( "submittedForm" )))
      {
         model.refresh();
      }
   }
}