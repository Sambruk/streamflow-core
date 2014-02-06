/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ProxyActions;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskService;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
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
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.client.assembler.StreamflowClientAssembler;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.account.AccountResources;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.account.AccountsDialog;
import se.streamsource.streamflow.client.ui.account.AccountsModel;
import se.streamsource.streamflow.client.ui.account.ProfileView;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.concurrent.Executors;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * Controller for the application
 */
@ProxyActions({"cut", "copy", "paste",
        "createDraft", "complete", "assign", "drop", "forward", // Case related proxy actions
        "find", "selectTree", "selectTable", "selectDetails"})
public class StreamflowApplication
        extends SingleFrameApplication
        implements TransactionListener, SingleInstanceListener
{
   public static ValueType DOMAIN_EVENT_TYPE;

   final Logger logger = LoggerFactory.getLogger(getClass().getName());
   final Logger streamflowLogger = LoggerFactory.getLogger(LoggerCategories.STREAMFLOW);

   @Structure
   ModuleSPI module;

   @Service
   DialogService dialogs;

   @Service
   EventStream stream;

   @Service
   JavaHelp javaHelp;

   AccountsModel accountsModel;

   private AccountSelector accountSelector;

   WorkspaceWindow workspaceWindow;
   OverviewWindow overviewWindow;
   AdministrationWindow administrationWindow;
   DebugWindow debugWindow;

   public ApplicationSPI app;
   private String openCaseJson;

   public StreamflowApplication()
   {
      super();

      // We have to ensure that calls to the server are done in the order they were executed,
      // so make it single threaded
      getContext().removeTaskService(getContext().getTaskService());
      getContext().addTaskService(new TaskService("default", Executors.newSingleThreadExecutor()));

      getContext().getResourceManager().setApplicationBundleNames(Arrays.asList("se.streamsource.streamflow.client.resources.StreamflowApplication"));
   }

   @Override
   protected void initialize(String[] args)
   {
      // Check if we are supposed to open a particular case
      final File[] openFile = new File[1];
      if (args.length > 0)
      {
         if (args[0].equals("-open"))
         {
            openFile(new File(args[1]));
         }
      }

      try
      {
         SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager.lookup(SingleInstanceService.class.getName());
         singleInstanceService.addSingleInstanceListener(this);
      } catch (UnavailableServiceException e)
      {
         // Ignore
      }
   }

   public void newActivation(String[] args)
   {
      System.out.println("New args:" + Arrays.asList(args));

      if (args.length > 0)
      {
         initialize(args);
         CaseDTO caseDTO = module.valueBuilderFactory().newValueFromJSON(CaseDTO.class, openCaseJson);
         openCaseJson = null;
         workspaceWindow.getCurrentWorkspace().openCase(caseDTO.caseId().get());
      }
   }

   public void openFile(File file)
   {
      System.out.println("Opening: " + file);
      try
      {
         final StringBuffer buf = new StringBuffer();
         Inputs.text(file.getAbsoluteFile()).transferTo(Outputs.withReceiver(new Receiver<String, RuntimeException>()
         {
            public void receive(String item) throws RuntimeException
            {
               buf.append(item);
            }
         }));
         openCaseJson = buf.toString();
         System.out.println(buf);
      } catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
   }

   public void init(@Uses final AccountsModel accountsModel,
                    @Uses final AccountSelector accountSelector,
                    @Service EventStream stream
   ) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
   {
      DOMAIN_EVENT_TYPE = module.valueDescriptor(DomainEvent.class.getName()).valueType();

      this.stream = stream;

//      NotificationGlassPane.install();

      try
      {
         //Check for Mac OS - and load if we are on Mac
         getClass().getClassLoader().loadClass("com.apple.eawt.Application");
         MacOsUIExtension osUIExtension = new MacOsUIExtension(this);
         osUIExtension.attachMacUIExtension();
         osUIExtension.attachMacOpenFileExtension();
         osUIExtension.convertAccelerators();
      } catch (Throwable e)
      {
         //Do nothing
      }


      // General UI settings
      String toolTipDismissDelay = i18n.text(StreamflowResources.tooltip_delay_dismiss);
      String toolTipInitialDelay = i18n.text(StreamflowResources.tooltip_delay_initial);
      String toolTipReshowDelay = i18n.text(StreamflowResources.tooltip_delay_reshow);
      if (toolTipInitialDelay != null && !toolTipInitialDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setInitialDelay(Integer.parseInt(toolTipInitialDelay));
      }
      if (toolTipDismissDelay != null && !toolTipDismissDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setDismissDelay(Integer.parseInt(toolTipDismissDelay));
      }
      if (toolTipReshowDelay != null && !toolTipReshowDelay.trim().equals(""))
      {
         ToolTipManager.sharedInstance().setReshowDelay(Integer.parseInt(toolTipReshowDelay));
      }

      getContext().getActionMap().get("myProfile").setEnabled(false);


      this.accountSelector = accountSelector;
      this.workspaceWindow = module.objectBuilderFactory().newObjectBuilder(WorkspaceWindow.class).use(accountSelector).newInstance();
      this.overviewWindow = module.objectBuilderFactory().newObjectBuilder(OverviewWindow.class).use(accountSelector).newInstance();
      this.administrationWindow = module.objectBuilderFactory().newObjectBuilder(AdministrationWindow.class).use(accountSelector).newInstance();
      this.debugWindow = module.objectBuilderFactory().newObjectBuilder(DebugWindow.class).newInstance();
      setMainFrame(workspaceWindow.getFrame());

      this.accountsModel = accountsModel;

      showWorkspaceWindow();

      // Auto-select first account if only one available
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if (accountsModel.getAccounts().size() == 1)
            {
               accountSelector.setSelectedIndex(0);

               if (openCaseJson != null)
               {
                  CaseDTO caseDTO = module.valueBuilderFactory().newValueFromJSON(CaseDTO.class, openCaseJson);
                  openCaseJson = null;
                  workspaceWindow.getCurrentWorkspace().openCase(caseDTO.caseId().get());
               }
            }
         }
      });

      accountSelector.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            StreamflowApplication.this.getContext().getActionMap().get("myProfile").setEnabled(!accountSelector.getSelectionModel().isSelectionEmpty());
         }
      });

      getContext().getActionMap().get("savePerspective").setEnabled(false);
      getContext().getActionMap().get("managePerspectives").setEnabled(false);
   }

   @Override
   protected void startup()
   {
      try
      {
         Client client = new Client(Protocol.HTTP);
         client.start();
         // Make it slower to get it more realistic
         Restlet restlet = new Filter(client.getContext(), client)
         {
            @Override
            protected int beforeHandle(Request request, Response response)
            {
               workspaceWindow.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

               return super.beforeHandle(request, response);
            }

            @Override
            protected void afterHandle(Request request, Response response)
            {
               workspaceWindow.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

               super.afterHandle(request, response);
            }
         };

         Energy4Java is = new Energy4Java();
         app = is.newApplication(new StreamflowClientAssembler(this,
                 org.jdesktop.application.Application.getInstance().getContext(),
                 restlet));

         logger.info("Starting in " + app.mode() + " mode");

         app.activate();
      } catch (Throwable e)
      {
         JXErrorPane.showDialog(getMainFrame(), new ErrorInfo(i18n.text(StreamflowResources.startup_error), e.getMessage(), null, "#error", e, java.util.logging.Level.SEVERE, Collections.<String, String>emptyMap()));
         shutdown();
      }

      streamflowLogger.info("Startup done");

   }

   // Menu actions

   @Action
   public void manageAccounts()
   {
      LinkValue selectedValue = (LinkValue) accountSelector.getSelectedValue();
      AccountsDialog dialog = module.objectBuilderFactory().newObjectBuilder(AccountsDialog.class).use(accountsModel).newInstance();
      dialog.setSelectedAccount(selectedValue);
      dialogs.showOkDialog(getMainFrame(), dialog, text(AccountResources.account_title));
   }

   @Action
   public void selectAccount()
   {
      accountSelector.clearSelection();
      if (administrationWindow.getFrame().isVisible())
      {
         administrationWindow.getFrame().setVisible(false);
         overviewWindow.getFrame().setVisible(false);
      }
   }

   @Action
   public void myProfile()
   {
      ProfileView profile = module.objectBuilderFactory().newObjectBuilder(ProfileView.class).use(accountSelector.getSelectedAccount().getProfileModel()).newInstance();
      dialogs.showOkDialog(getMainFrame(), profile, text(AccountResources.profile_title));
   }

   // Proxy actions for perspective
   @Action
   public void savePerspective(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("savePerspective").getValue("proxy")).actionPerformed( e );
   }

   @Action
   public void managePerspectives(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("managePerspectives").getValue("proxy")).actionPerformed( e );
   }

   // Proxy actions for message view
   @Action
   public void closeMessageDetails(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("closeMessageDetails").getValue("proxy")).actionPerformed( e );
   }

   @Action
   public void createMessage(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("createMessage").getValue("proxy")).actionPerformed(e);
   }

   @Action
   public void cancelNewMessage(ActionEvent e)
   {
      ((ApplicationAction) getContext().getActionMap().get("cancelNewMessage").getValue("proxy")).actionPerformed(e);
   }

   public EventStream getSource()
   {
      return stream;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      for (Window window : Frame.getWindows())
      {
         dispatchTransactions(window, transactions);
      }
   }

   private void dispatchTransactions(Component component, Iterable<TransactionDomainEvents> transactionEventsIterable)
   {
      if (!component.isShowing())
         return;

      if (component instanceof TransactionListener)
         ((TransactionListener) component).notifyTransactions(transactionEventsIterable);

      if (component instanceof Container)
      {
         Container container = (Container) component;
         for (Component childComponent : container.getComponents())
         {
            // Only dispatch to visible components - they will refresh once visible anyway
            dispatchTransactions(childComponent, transactionEventsIterable);
         }
      }
   }

   // Controller actions -------------------------------------------

   // Menu actions
   // Account menu

   @Action
   public void showWorkspaceWindow()
   {
      if (!workspaceWindow.getFrame().isVisible())
      {
         show(workspaceWindow);
      }
      workspaceWindow.getFrame().toFront();
   }

   @Action
   public void showOverviewWindow() throws Exception
   {
      if (!overviewWindow.getFrame().isVisible())
      {
         show(overviewWindow);
      }
      overviewWindow.getFrame().toFront();
   }

   @Action
   public void showAdministrationWindow() throws Exception
   {
      if (!administrationWindow.getFrame().isVisible())
         show(administrationWindow);
      administrationWindow.getFrame().toFront();
   }

   @Action
   public void showDebugWindow() throws Exception
   {
      if (!debugWindow.getFrame().isVisible())
         show(debugWindow);
      debugWindow.getFrame().toFront();
   }

   @Action
   public void close(ActionEvent e)
   {
      WindowUtils.findWindow( (Component) e.getSource() ).dispose();
   }

   @Action
   public void cancel(ActionEvent e)
   {
      WindowUtils.findWindow((Component) e.getSource()).dispose();
   }

   @Action
   public void showAbout()
   {
      dialogs.showOkDialog( getMainFrame(), new AboutDialog( getContext() ) );
   }

   @Action
   public void showHelp(ActionEvent event)
   {
      // Turn off java help for 1.0 release
      // javaHelp.init();
   }

   @Override
   public void exit(EventObject eventObject)
   {
      super.exit(eventObject);
   }

   @Override
   protected void shutdown()
   {
      try
      {
         SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager.lookup(SingleInstanceService.class.getName());
         singleInstanceService.removeSingleInstanceListener(this);
      } catch (UnavailableServiceException e)
      {
         // Ignore
      }

      try
      {
         if (app != null)
            app.passivate();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Override
   protected void show(JComponent jComponent)
   {
      super.show(jComponent);
   }

   public String currentUserId()
   {
      return accountSelector.getSelectedAccount().settings().userName().get();
   }

   public long markReadTimeout()
   {
      return accountSelector.getSelectedAccount().getProfileModel().getIndex().markReadTimeout().get() * 1000;
   }

   public void callRefresh()
   {
      if( workspaceWindow.getCurrentWorkspace().isVisible() && workspaceWindow.getCurrentWorkspace().isShowing() )
      {
         workspaceWindow.getCurrentWorkspace().refresh();
      }
   }
}