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
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.error.ErrorInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.menu.AccountsDialog;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;
import se.streamsource.streamflow.client.ui.menu.MenuView;
import se.streamsource.streamflow.client.ui.overview.OverviewModel;
import se.streamsource.streamflow.client.ui.overview.OverviewResources;
import se.streamsource.streamflow.client.ui.overview.OverviewView;
import se.streamsource.streamflow.client.ui.search.SearchResources;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.search.SearchView;
import se.streamsource.streamflow.client.ui.status.StatusBarView;
import se.streamsource.streamflow.client.ui.status.StatusResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the application
 */
public class StreamFlowApplication
        extends SingleFrameApplication
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    DialogService dialogs;

    @Service
    IndividualRepository individualRepo;

    AccountsModel accountsModel;

    JLabel label;

    JXFrame searchWindow;

    MenuView menuView;

    JXFrame workspaceWindow;
    WorkspaceView workspaceView;
    WorkspaceModel workspaceModel;

    JXFrame overviewWindow;
    OverviewView overviewView;
    OverviewModel overviewModel;

    JXFrame administrationWindow;
    AdministrationView administrationView;
    AdministrationModel administrationModel;
    public SearchView searchView;

    public StreamFlowApplication()
    {
        super();

        getContext().getResourceManager().setApplicationBundleNames(Arrays.asList("se.streamsource.streamflow.client.resources.StreamFlowApplication"));

        workspaceWindow = new JXFrame(i18n.text(WorkspaceResources.window_name));
        overviewWindow = new JXFrame(i18n.text(OverviewResources.window_name));

        administrationWindow = new JXFrame(i18n.text(AdministrationResources.window_name));
        searchWindow = new JXFrame(i18n.text(SearchResources.window_name));

        workspaceWindow.setLocationByPlatform(true);

        JXStatusBar bar = new StatusBarView(getContext());
        workspaceWindow.setStatusBar(bar);

        setMainFrame(workspaceWindow);

/*
        Application application = Application.getApplication();
        application.setEnabledAboutMenu(true);
        application.addApplicationListener(new ApplicationAdapter()
        {
            @Override
            public void handleAbout(ApplicationEvent applicationEvent)
            {
                applicationEvent.setHandled(true);
                JXDialog dialog = new JXDialog(workspaceWindow, new AboutDialog());
                dialog.pack();
                dialog.setVisible(true);
            }

            @Override
            public void handleQuit(ApplicationEvent applicationEvent)
            {
                applicationEvent.setHandled(true);
                shutdown();
            }
        });
*/
    }
    
    public void init(@Uses final AccountsModel accountsModel, @Structure final ObjectBuilderFactory obf) throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException, ClassNotFoundException
    {
        this.accountsModel = accountsModel;
        JFrame frame = getMainFrame();
        frame.setTitle("StreamFlow");

        ListDataListener workspaceListener = new ListDataListener()
        {
            public void intervalAdded(ListDataEvent e)
            {
                contentsChanged(e);
            }

            public void intervalRemoved(ListDataEvent e)
            {
                contentsChanged(e);
            }

            public void contentsChanged(ListDataEvent e)
            {
                if (accountsModel.getSize() > 0 && workspaceView == null)
                {
                    AccountModel accountModel = accountsModel.accountModel(0);
                    workspaceModel = obf.newObjectBuilder(WorkspaceModel.class).use(accountModel).newInstance();
                    workspaceView = obf.newObjectBuilder(WorkspaceView.class).use(workspaceModel, accountModel).newInstance();
                    getMainFrame().getContentPane().add(workspaceView);
                } else
                {
                    if (workspaceView != null)
                    {
                        Container container = getMainFrame().getContentPane();
                        container.remove(workspaceView);
                        container.validate();
                        getMainFrame().pack();
                    }
                    workspaceModel = null;
                    workspaceView = null;
                }
            }
        };
        accountsModel.addListDataListener(workspaceListener);
        workspaceListener.contentsChanged(null);

        administrationModel = obf.newObjectBuilder(AdministrationModel.class).use(accountsModel).newInstance();
        administrationView = obf.newObjectBuilder(AdministrationView.class).use(administrationModel).newInstance();
        administrationWindow.getContentPane().setLayout(new BorderLayout());
        administrationWindow.getContentPane().add(administrationView, BorderLayout.CENTER);

        overviewWindow.getContentPane().setLayout(new BorderLayout());

        searchWindow.getContentPane().setLayout(new BorderLayout());
        searchWindow.setMinimumSize(new Dimension(600, 600));

        menuView = obf.newObject(MenuView.class);

        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
        frame.setJMenuBar(menuView);

        showWorkspaceWindow();
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

                    Logger.getLogger(LoggerCategories.HTTP).info(request.getResourceRef().toString() +"->"+response.getStatus());

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
        System.out.println("Startup done");

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

    public AccountsModel accountsModel()
    {
        return accountsModel;
    }

    public WorkspaceView getWorkspaceView()
    {
        return workspaceView;
    }

    // Controller actions -------------------------------------------

    // Menu actions
    // Account menu
    @Action
    public void showWorkspaceWindow()
    {
        if (!workspaceWindow.isVisible())
        {
            if (workspaceView != null)
                try
                {
                    workspaceView.refreshTree();
                } catch (Exception e)
                {
                    // Ignore
                }
            show(workspaceWindow);
        }
    }

    @Action
    public void showOverviewWindow() throws Exception
    {
        if (overviewView == null)
        {
            AccountModel accountModel = accountsModel.accountModel(0);
            overviewModel = obf.newObjectBuilder(OverviewModel.class).use(accountModel).newInstance();
            overviewView = obf.newObjectBuilder(OverviewView.class).use(overviewModel, accountModel).newInstance();
            overviewWindow.getContentPane().add(overviewView);
        }

        if (!overviewWindow.isVisible())
        {
            overviewView.refreshTree();
            show(overviewWindow);
        }
    }

    @Action
    public void showAdministrationWindow() throws Exception
    {
        administrationModel.refresh();
        if (!administrationWindow.isVisible())
            show(administrationWindow);
    }

    @Action
    public void showSearchWindow() throws Exception
    {
        if (searchView == null)
        {
            String organization = accountsModel.accountModel(0).userResource().administration().organizations().roots().get().get(0).entity().get().identity();
            SearchResultTableModel model = obf.newObjectBuilder(SearchResultTableModel.class).use(accountsModel.accountModel(0).serverResource().organizations().organization(organization).search()).newInstance();
            searchView = obf.newObjectBuilder(SearchView.class).use(model).newInstance();
            searchWindow.getContentPane().add(searchView, BorderLayout.CENTER);
            searchWindow.pack();
        }

        if (!searchWindow.isVisible())
            show(searchWindow);
    }

    @Action
    public void help()
    {
        JOptionPane.showMessageDialog(this.getMainFrame(), "#showhelp");
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
}