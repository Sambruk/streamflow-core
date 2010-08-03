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

package se.streamsource.streamflow.client;

import org.jdesktop.application.Action;
import org.jdesktop.application.ProxyActions;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.ModuleSPI;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.client.assembler.StreamflowClientAssembler;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.administration.AccountResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.administration.ProfileDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.helper.ForEvents;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * Controller for the application
 */
@ProxyActions({"cut", "copy", "paste",
      "createDraft", "complete", "assign", "drop", "forward", // Case related proxy actions
      "find", "selectTree", "selectTable", "selectDetails"})
public class StreamflowApplication
      extends SingleFrameApplication
{
   public static ValueType DOMAIN_EVENT_TYPE;

   final Logger logger = LoggerFactory.getLogger( getClass().getName() );
   final Logger streamflowLogger = LoggerFactory.getLogger( LoggerCategories.STREAMFLOW );

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ModuleSPI module;

   @Service
   DialogService dialogs;

   @Service
   IndividualRepository individualRepo;

   @Service
   JavaHelp javaHelp;

   AccountsModel accountsModel;

   JLabel label;

   private AccountSelector accountSelector;
   WorkspaceWindow workspaceWindow;

   OverviewWindow overviewWindow;

   DebugWindow debugWindow;

   AdministrationWindow administrationWindow;
   private ForEvents subscriber;
   public ApplicationSPI app;

   public StreamflowApplication()
   {
      super();

      getContext().getResourceManager().setApplicationBundleNames( Arrays.asList( "se.streamsource.streamflow.client.resources.StreamflowApplication" ) );
   }

   public void init( @Uses final AccountsModel accountsModel,
                     @Structure final ObjectBuilderFactory obf,
                     @Uses final AccountSelector accountSelector,
                     @Service EventSource source
   ) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
   {
      DOMAIN_EVENT_TYPE = module.valueDescriptor( DomainEvent.class.getName() ).valueType();

//      NotificationGlassPane.install();

      try
      {
         //Check for Mac OS - and load if we are on Mac
         getClass().getClassLoader().loadClass( "com.apple.eawt.Application" );
         MacOsUIExtension osUIExtension = new MacOsUIExtension( this );
         osUIExtension.attachMacUIExtension();
         osUIExtension.convertAccelerators();
      } catch (Throwable e)
      {
         //Do nothing
      }


      // General UI settings
      String toolTipDismissDelay = i18n.text( StreamflowResources.tooltip_delay_dismiss );
      String toolTipInitialDelay = i18n.text( StreamflowResources.tooltip_delay_initial );
      String toolTipReshowDelay = i18n.text( StreamflowResources.tooltip_delay_reshow );
      if (toolTipInitialDelay != null && !toolTipInitialDelay.trim().equals( "" ))
      {
         ToolTipManager.sharedInstance().setInitialDelay( Integer.parseInt( toolTipInitialDelay ) );
      }
      if (toolTipDismissDelay != null && !toolTipDismissDelay.trim().equals( "" ))
      {
         ToolTipManager.sharedInstance().setDismissDelay( Integer.parseInt( toolTipDismissDelay ) );
      }
      if (toolTipReshowDelay != null && !toolTipReshowDelay.trim().equals( "" ))
      {
         ToolTipManager.sharedInstance().setReshowDelay( Integer.parseInt( toolTipReshowDelay ) );
      }

      getContext().getActionMap().get( "myProfile" ).setEnabled( false );


      this.accountSelector = accountSelector;
      this.workspaceWindow = obf.newObjectBuilder( WorkspaceWindow.class ).use( accountSelector ).newInstance();
      this.overviewWindow = obf.newObjectBuilder( OverviewWindow.class ).use( accountSelector ).newInstance();
      this.administrationWindow = obf.newObjectBuilder( AdministrationWindow.class ).use( accountSelector ).newInstance();
      this.debugWindow = obf.newObjectBuilder( DebugWindow.class ).newInstance();
      setMainFrame( workspaceWindow.getFrame() );

      this.accountsModel = accountsModel;

      subscriber = new ForEvents( AllEventsSpecification.INSTANCE, new EventVisitor()
      {
         public boolean visit( DomainEvent event )
         {
            accountsModel.notifyEvent( event );

            return true;
         }
      } );
      source.registerListener( subscriber );

      showWorkspaceWindow();

      // Auto-select first account if only one available
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            if (accountsModel.getAccounts().size() == 1)
            {
               accountSelector.setSelectedIndex( 0 );
            }
         }
      } );

      accountSelector.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            StreamflowApplication.this.getContext().getActionMap().get( "myProfile" ).setEnabled( !accountSelector.getSelectionModel().isSelectionEmpty() );
         }
      } );
   }

   @Override
   protected void startup()
   {
      try
      {
         Client client = new Client( Protocol.HTTP );
         client.start();
         // Make it slower to get it more realistic
         Restlet restlet = new Filter( client.getContext(), client )
         {
            @Override
            protected int beforeHandle( Request request, Response response )
            {
               return super.beforeHandle( request, response );
            }

            @Override
            protected void afterHandle( Request request, Response response )
            {
               super.afterHandle( request, response );
            }
         };

         Energy4Java is = new Energy4Java();
         app = is.newApplication( new StreamflowClientAssembler( this,
               org.jdesktop.application.Application.getInstance().getContext(),
               restlet ) );

         logger.info( "Starting in " + app.mode() + " mode" );

         app.activate();
      } catch (Throwable e)
      {
         JXErrorPane.showDialog( getMainFrame(), new ErrorInfo( i18n.text( StreamflowResources.startup_error ), e.getMessage(), null, "#error", e, java.util.logging.Level.SEVERE, Collections.<String, String>emptyMap() ) );
         shutdown();
      }

      streamflowLogger.info( "Startup done" );

   }

   // Menu actions

   @Uses
   private ObjectBuilder<AccountsDialog> accountsDialog;

   @Action
   public void manageAccounts()
   {
      AccountsDialog dialog = accountsDialog.use( accountsModel ).newInstance();
      dialogs.showOkDialog( getMainFrame(), dialog, text( AccountResources.account_title ) );
   }

   @Action
   public void selectAccount()
   {
      accountSelector.clearSelection();
      if (administrationWindow.getFrame().isVisible())
      {
         administrationWindow.getFrame().setVisible( false );
      }
   }

   @Uses
   private ObjectBuilder<ProfileDialog> profileDialogs;

   @Action
   public void myProfile()
   {
      ProfileDialog profile = profileDialogs.use( accountSelector.getSelectedAccount() ).newInstance();
      dialogs.showOkDialog( getMainFrame(), profile, text( AccountResources.profile_title ) );
   }

   public AccountsModel accountsModel()
   {
      return accountsModel;
   }

   public String getSelectedUser()
   {
      return accountSelector.isSelectionEmpty() ? null : accountSelector.getSelectedAccount().settings().userName().get();
   }

   public AccountSelector getAccountSelector()
   {
      return accountSelector;
   }

   // Controller actions -------------------------------------------

   // Menu actions
   // Account menu

   @Action
   public void showWorkspaceWindow()
   {
      if (!workspaceWindow.getFrame().isVisible())
      {
         show( workspaceWindow );
      }
      workspaceWindow.getFrame().toFront();
   }

   @Action
   public void showOverviewWindow() throws Exception
   {
      if (!overviewWindow.getFrame().isVisible())
      {
         show( overviewWindow );
      }
      overviewWindow.getFrame().toFront();
   }

   @Action
   public void showAdministrationWindow() throws Exception
   {
      if (!administrationWindow.getFrame().isVisible())
         show( administrationWindow );
      administrationWindow.getFrame().toFront();
   }

   @Action
   public void showDebugWindow() throws Exception
   {
      if (!debugWindow.getFrame().isVisible())
         show( debugWindow );
      debugWindow.getFrame().toFront();
   }

   @Action
   public void close( ActionEvent e )
   {
      WindowUtils.findWindow( (Component) e.getSource() ).dispose();
   }

   @Action
   public void cancel( ActionEvent e )
   {
      WindowUtils.findWindow( (Component) e.getSource() ).dispose();
   }

   @Action
   public void showAbout()
   {
      dialogs.showOkDialog( getMainFrame(), new AboutDialog() );
   }

   @Action
   public void showHelp( ActionEvent event )
   {
      // Turn off java help for 1.0 release
      // javaHelp.init();
   }

   @Override
   public void exit( EventObject eventObject )
   {
      super.exit( eventObject );
   }

   @Override
   protected void shutdown()
   {
      try
      {
         if (app != null)
            app.passivate();
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      super.shutdown();
   }

   @Override
   protected void show( JComponent jComponent )
   {
      super.show( jComponent );
   }
}