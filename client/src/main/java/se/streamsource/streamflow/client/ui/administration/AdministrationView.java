/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.TreeList;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.TabbedResourceView;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import static java.util.Arrays.asList;
import static org.qi4j.api.specification.Specifications.*;
import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class AdministrationView
      extends JPanel implements TransactionListener
{
   @Service
   StreamflowApplication application;

   JSplitPane mainView = new JSplitPane();
   JPanel detailView;

   CardLayout viewSwitch = new CardLayout();
   private ApplicationActionMap am;
   private AdministrationTreeView adminTreeView;

   private AdministrationModel model;

   public AdministrationView( @Service ApplicationContext context,
                              @Uses final AdministrationModel model,
                              @Structure final Module module)
   {
      am = context.getActionMap( this );
      setActionMap( am );
      this.model = model;
      this.adminTreeView = module.objectBuilderFactory().newObjectBuilder(AdministrationTreeView.class).use( this.model ).newInstance();

      setLayout( viewSwitch );
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      detailView = new JPanel( new BorderLayout() );

      add( mainView, "main" );
      add( detailView, "detail" );

      viewSwitch.show( this, "main" );

      setMinimumSize( new Dimension( 800, 600 ) );
      setPreferredSize( getMinimumSize() );

      mainView.setBorder(BorderFactory.createEmptyBorder());
      mainView.setOneTouchExpandable( true );

      mainView.setLeftComponent( adminTreeView );
      adminTreeView.setMinimumSize( new Dimension( 200, 400 ) );
      mainView.setRightComponent( new JPanel() );

      mainView.setDividerLocation( 200 );
      mainView.setResizeWeight( 0 );
      adminTreeView.getTree().addTreeSelectionListener( new TreeSelectionListener()
      {
         public void valueChanged( TreeSelectionEvent e )
         {
            final TreePath path = e.getNewLeadSelectionPath();
            if (path != null)
            {
               Object node = path.getLastPathComponent();

               final LinkValue link = (LinkValue) ((TreeList.Node)node).getElement();

               // if node is disabled clear right component and jump out
               if( matchesAny( new Specification<String>()
               {
                  public boolean satisfiedBy( String item )
                  {
                     return item.equals( "disabled" );
                  }
               }, asList( link.classes().get().split( " " ) )) )
               {
                  mainView.setRightComponent( new JPanel() );
                  return;
               }

               Object linkedModel = model.newResourceModel(link);

               JComponent view = module.objectBuilderFactory().newObjectBuilder(TabbedResourceView.class).use( linkedModel ).newInstance();

               mainView.setRightComponent( view );
            } 
         }
      } );
   }

   public void show( JComponent view )
   {
      detailView.removeAll();
      detailView.add( view, BorderLayout.CENTER );
      detailView.add( new StreamflowButton( am.get( "done" ) ), BorderLayout.SOUTH );
      detailView.revalidate();
      detailView.repaint();
      viewSwitch.show( this, "detail" );
   }

   @org.jdesktop.application.Action
   public void done()
   {
      viewSwitch.show( this, "main" );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if( matches( and( Events.withUsecases( "delete" ), Events.withNames( "revokedRole" ) ), transactions ) )
      {
         DomainEvent event = first( filter( and( withUsecases( "delete" ), withNames( "revokedRole" ) ), events( transactions ) ) );

         if( matches(  Events.paramIs( "param1", application.currentUserId() ), transactions )
               || model.isParticipantInGroup( EventParameters.getParameter( event, "param1" ), application.currentUserId()))
         {
            adminTreeView.getTree().setSelectionPath( null );
            model.notifyTransactions( transactions );

            mainView.setRightComponent( new JPanel() );
         }
      }
   }
}
