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

package se.streamsource.streamflow.client.ui.status;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.search.Searchable;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.LoggerCategories;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class StatusBarView
      extends JXStatusBar
{
   public JXFindBar searchField;

   public StatusBarView( final @Service ApplicationContext context )
   {
      final ResourceMap resources = context.getResourceMap( StatusResources.class );

      searchField = new JXFindBar()
      {
         JComboBox box = new JComboBox();

         /*
                     @Override
                     protected void initComponents()
                     {
                         super.initComponents();
                         searchField = (JTextField) box.getEditor().getEditorComponent();
                     }

         */
         @Override
         protected void build()
         {
            setLayout( new FlowLayout( SwingConstants.LEADING ) );
            add( searchField );
            add( findNext );
            add( findPrevious );

//                AutoCompleteDecorator.decorate(searchField, searchable.);
         }

         @Override
         public void setSearchable( Searchable searchable )
         {
            super.setSearchable( searchable );

            if (searchField != null)
               searchField.setText( "" );
         }
      };
      JXStatusBar.Constraint c1 = new JXStatusBar.Constraint();
      c1.setFixedWidth( 600 );
//        add(searchField, c1);     // Fixed width of 400 with no inserts

      final JLabel statusLabel = new JLabel();
      statusLabel.setOpaque( true );
      JXStatusBar.Constraint c2 = new JXStatusBar.Constraint();
      c2.setFixedWidth( 200 );
      add( statusLabel, JXStatusBar.Constraint.ResizeBehavior.FILL );
      JXStatusBar.Constraint c3 = new JXStatusBar.Constraint(
            JXStatusBar.Constraint.ResizeBehavior.FILL ); // Fill with no inserts
      final JProgressBar pbar = new JProgressBar();
      add( pbar, JXStatusBar.Constraint.ResizeBehavior.FIXED );            // Fill with no inserts - will use remaining space

      Logger.getLogger( LoggerCategories.STATUS ).addHandler( new Handler()
      {
         public void publish( LogRecord record )
         {
            String status = record.getMessage();

            String localizedStatus = resources.getString( status );
            if (localizedStatus != null)
               status = localizedStatus;

            final String text = status;

            statusLabel.setText( text );
/*
                statusLabel.repaint();
                StatusBarView.this.repaint();
                StatusBarView.this.revalidate();
*/
         }

         public void flush()
         {
         }

         public void close() throws SecurityException
         {
         }
      } );

      Logger.getLogger( LoggerCategories.PROGRESS ).addHandler( new StatusLogHandler( pbar ) );

      Logger.getLogger( LoggerCategories.STATUS ).info( StatusResources.ready.name() );

      context.getTaskMonitor().addPropertyChangeListener( new PropertyChangeListener()
      {
         public void propertyChange( PropertyChangeEvent evt )
         {
            if (context.getTaskMonitor().getForegroundTask() == null)
            {
               Logger.getLogger( LoggerCategories.STATUS ).info( StatusResources.ready.name() );
               Logger.getLogger( LoggerCategories.PROGRESS ).info( LoggerCategories.DONE );
            } else
            {
               Logger.getLogger( LoggerCategories.STATUS ).info( StatusResources.loading.name() );
               Logger.getLogger( LoggerCategories.PROGRESS ).info( "loading" );
            }
         }
      } );
   }

   public JXFindBar getSearchField()
   {
      return searchField;
   }

   private static class StatusLogHandler extends Handler
   {
      private final JProgressBar pbar;

      public StatusLogHandler( JProgressBar pbar )
      {
         this.pbar = pbar;
      }

      public void publish( LogRecord record )
      {
         JXFrame frame = (JXFrame) ((SingleFrameApplication) Application.getInstance()).getMainFrame();

         String[] message = record.getMessage().split( "/" );
         if (message.length == 1)
         {
            if (message[0].equals( LoggerCategories.DONE ))
            {
               pbar.setIndeterminate( false );
               frame.setWaiting( false );
            } else
            {
               pbar.setIndeterminate( true );
               frame.setWaiting( true );
            }
         } else
         {
            int max = Integer.parseInt( message[1] );
            int current = Integer.parseInt( message[0] );
            pbar.setMaximum( max );
            pbar.setValue( current );
         }
      }

      public void flush()
      {
      }

      public void close() throws SecurityException
      {
      }
   }
}
