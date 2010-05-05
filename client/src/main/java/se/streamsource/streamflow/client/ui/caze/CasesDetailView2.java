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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.ForEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class CasesDetailView2
      extends JPanel
{
   private CaseDetailView current = null;

   @Structure
   ObjectBuilderFactory obf;

   private TransactionVisitor subscriber;

   private CardLayout layout = new CardLayout();

   public CasesDetailView2( @Uses CaseDetailView current, @Service EventSource events )
   {
      setLayout(layout );
      setBorder(BorderFactory.createEmptyBorder());

      add( new JLabel( i18n.text( WorkspaceResources.choose_case ), JLabel.CENTER), "blank" );
      add( current, "detail" );

      layout.show( this, "blank" );

      setPreferredSize( new Dimension( getWidth(), 500 ) );

      this.current = current;

      subscriber = new ForEvents( new EventQuery().withNames( "deletedEntity" ), new EventVisitor()
      {
         public boolean visit( DomainEvent event )
         {
            layout.show( CasesDetailView2.this, "blank");

            return false;
         }
      } );

      events.registerListener( subscriber );
   }

   public void show( final CaseModel aCase )
   {
      current.setCaseModel( aCase );

      layout.show( this, "detail" );
   }

   @Override
   public boolean requestFocusInWindow()
   {
      return current.requestFocusInWindow();
   }
}