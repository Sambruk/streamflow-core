/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.JavaHelp;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.AccountSelector;
import se.streamsource.streamflow.client.ui.DebugWindow;
import se.streamsource.streamflow.client.ui.administration.AdministrationWindow;
import se.streamsource.streamflow.client.ui.menu.AccountsDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;
import se.streamsource.streamflow.client.ui.overview.OverviewWindow;
import se.streamsource.streamflow.client.ui.search.SearchWindow;
import se.streamsource.streamflow.client.ui.status.StatusResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceWindow;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the application
 */
@ProxyActions({"cut","copy","paste",
        "createTask", "completeTasks", "assignTasksToMe", "markTasksAsRead", "markTasksAsUnread", "dropTasks", "forwardTasks", "delegateTasks", // Task related proxy actions 
        "find", "selectTree", "selectTable", "selectDetails"})
public class StreamFlowApplication
        extends SingleFrameApplication
{
    public static ValueType DOMAIN_EVENT_TYPE;

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

    SearchWindow searchWindow;

    DebugWindow debugWindow;

    AdministrationWindow administrationWindow;

    public StreamFlowApplication()
    {
        super();

        getContext().getResourceManager().setApplicationBundleNames(Arrays.asList("se.streamsource.streamflow.client.resources.StreamFlowApplication"));
    }

    public void init(@Uses final AccountsModel accountsModel,
                     @Structure final ObjectBuilderFactory obf,
                     @Uses AccountSelector accountSelector
    ) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
    {
        DOMAIN_EVENT_TYPE = module.valueDescriptor(DomainEvent.class.getName()).valueType();

        try
        {
            //Check for Mac OS - and load if we are on Mac
            getClass().getClassLoader().loadClass("com.apple.eawt.Application");
            MacOsUIExtension osUIExtension = new MacOsUIExtension(this);
            osUIExtension.attachMacUIExtension();
            osUIExtension.convertAccelerators();
        } catch (ClassNotFoundException e)
        {
            //Do nothing
        }

        this.accountSelector = accountSelector;
        this.workspaceWindow = obf.newObjectBuilder(WorkspaceWindow.class).use(accountSelector).newInstance();
        this.overviewWindow = obf.newObjectBuilder(OverviewWindow.class).use(accountSelector).newInstance();
        this.searchWindow = obf.newObjectBuilder(SearchWindow.class).use(accountSelector).newInstance();
        this.administrationWindow = obf.newObjectBuilder(AdministrationWindow.class).use(accountSelector).newInstance();
        this.debugWindow = obf.newObjectBuilder(DebugWindow.class).newInstance();
        setMainFrame(workspaceWindow.getFrame());

        this.accountsModel = accountsModel;

        showWorkspaceWindow();

        // Auto-select first account
        if (accountsModel.getSize() == 1)
        {
            accountSelector.setSelectedIndex(0);
        }
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
                    Logger.getLogger(LoggerCategories.STATUS).info(StatusResources.loading.name());
                    Logger.getLogger(LoggerCategories.PROGRESS).info("loading");

/*
                    try
                    {
                        Thread.sleep(2000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
*/
                    return super.beforeHandle(request, response);
                }

                @Override
                protected void afterHandle(Request request, Response response)
                {
                    Logger.getLogger(LoggerCategories.STATUS).info(StatusResources.ready.name());
                    Logger.getLogger(LoggerCategories.PROGRESS).info(LoggerCategories.DONE);

                    Logger.getLogger(LoggerCategories.HTTP).info(request.getResourceRef().toString() + "->" + response.getStatus());

                    super.afterHandle(request, response);
                }
            };

            Energy4Java is = new Energy4Java();
            ApplicationSPI app = is.newApplication(new StreamFlowClientAssembler(this,
                    org.jdesktop.application.Application.getInstance().getContext(),
                    restlet));

            Logger.getLogger(getClass().getName()).info("Starting in " + app.mode() + " mode");

            app.activate();
        } catch (Throwable e)
        {
            JXErrorPane.showDialog(getMainFrame(), new ErrorInfo(i18n.text(StreamFlowResources.startup_error), e.getMessage(), null, "#error", e, Level.SEVERE, Collections.<String, String>emptyMap()));
            shutdown();
        }

        Logger.getLogger("streamflow").info("Startup done");

    }

    // Menu actions

    @Uses
    private ObjectBuilder<AccountsDialog> accountsDialog;

    @Action
    public void manageAccounts()
    {
        AccountsDialog dialog = accountsDialog.use(accountsModel).newInstance();
        dialogs.showOkDialog(getMainFrame(), dialog);
    }

    @Action
    public void selectAccount()
    {
        accountSelector.clearSelection();
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
            show(workspaceWindow);
        }
    }

    @Action
    public void showOverviewWindow() throws Exception
    {
        if (!overviewWindow.getFrame().isVisible())
        {
            show(overviewWindow);
        }
    }

    @Action
    public void showAdministrationWindow() throws Exception
    {
        if (!administrationWindow.getFrame().isVisible())
            show(administrationWindow);
    }

    @Action
    public void showSearchWindow() throws Exception
    {
        if (!searchWindow.getFrame().isVisible())
            show(searchWindow);
    }

    @Action
    public void showDebugWindow() throws Exception
    {
        if (!debugWindow.getFrame().isVisible())
            show(debugWindow);
    }

    @Action
    public void close(ActionEvent e)
    {
        WindowUtils.findWindow((Component) e.getSource()).dispose();
    }

    @Action
    public void cancel(ActionEvent e)
    {
        WindowUtils.findWindow((Component) e.getSource()).dispose();
    }

    @Action
    public void showAbout()
    {
        dialogs.showOkDialog(getMainFrame(), new AboutDialog());
    }

    @Action
    public void showHelp(ActionEvent event)
    {
        javaHelp.init();
    }

    @Override
    public void exit(EventObject eventObject)
    {
        super.exit(eventObject);
    }

    @Override
    protected void shutdown()
    {
        super.shutdown();
    }

    @Override
    protected void show(JComponent jComponent)
    {
        super.show(jComponent);
    }
}