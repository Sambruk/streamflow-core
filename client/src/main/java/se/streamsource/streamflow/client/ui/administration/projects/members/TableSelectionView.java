/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.projects.members;

import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class TableSelectionView
      extends JPanel
{

   public String searchText()
   {
      return nameField.getText();
   }

   protected AbstractTableSelectionModel model;
   private JTextField nameField;

   public TableSelectionView( @Structure ValueBuilderFactory vbf,
                              @Uses AbstractTableSelectionModel model,
                              @Uses String searchLineString )
   {
      super( new BorderLayout() );
      this.model = model;

      model.setModel( vbf.newValueBuilder( LinksValue.class ).newInstance() );
      nameField = new JTextField();
      nameField.setColumns( 10 );
      JPanel searchLine = new JPanel( new BorderLayout() );
      searchLine.add( new JLabel( searchLineString ), BorderLayout.CENTER );
      searchLine.add( nameField, BorderLayout.LINE_END );
      add( searchLine, BorderLayout.NORTH );

      JXTable projectsTable = new JXTable( getModel() );
      projectsTable.getColumn( 0 ).setMaxWidth( 40 );
      projectsTable.getColumn( 0 ).setResizable( false );
      JScrollPane projectsScrollPane = new JScrollPane( projectsTable );
      add( projectsScrollPane );
   }

   public JTextField getSearchInputField()
   {
      return nameField;
   }

   public AbstractTableSelectionModel getModel()
   {
      return model;
   }
}