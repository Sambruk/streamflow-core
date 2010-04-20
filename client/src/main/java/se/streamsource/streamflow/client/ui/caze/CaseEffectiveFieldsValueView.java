/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.ToolTipTableCellRenderer;
import se.streamsource.streamflow.domain.contact.ContactValue;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends JPanel
{
   public ValueBuilder<ContactValue> valueBuilder;
   private JXTable effectiveValueTable;
   public RefreshWhenVisible refresher;

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );

      effectiveValueTable = new JXTable();
      effectiveValueTable.setDefaultRenderer( Object.class, new ToolTipTableCellRenderer() );

      JScrollPane submittedFormsScollPane = new JScrollPane();
      submittedFormsScollPane.setViewportView( effectiveValueTable );

      add( submittedFormsScollPane, BorderLayout.CENTER );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }


   public void setModel( CaseEffectiveFieldsValueModel model )
   {
      effectiveValueTable.setModel( model );

      refresher.setRefreshable( model );
   }
}