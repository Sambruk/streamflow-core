package se.streamsource.streamflow.client.ui.workspace.table;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.CLOSED;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.ON_HOLD;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.OPEN;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.icon;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseActionsView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseInfoView;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.ui.workspace.cases.history.HistoryView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.WrapLayout;

public class CasesFilterView extends JPanel
{

   public class CaseFilterLabelView extends JPanel implements Refreshable
   {
      public void refresh()
      {
         removeAll();
         for (LinkValue linkValue : model.getPossibleLabels())
         {
            add(new JCheckBox(linkValue.text().get()));
         }
      }
   }

   public class CaseFilterStatusView extends JPanel
   {

      public CaseFilterStatusView()
      {
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         add(new JCheckBox(text(OPEN)));
         add(new JCheckBox(text(ON_HOLD)));
         add(new JCheckBox(text(CLOSED)));
      }
   }

   private static final long serialVersionUID = -149885124005347187L;

   private JTabbedPane tabs = new JTabbedPane( JTabbedPane.TOP );
   
   private CasesFilterModel model;

   public void initView(final @Service ApplicationContext context, final @Structure ObjectBuilderFactory obf,
         final @Uses CommandQueryClient client)
   {
      model = obf.newObjectBuilder(CasesFilterModel.class).use(client).newInstance();

      tabs.addTab( text( status), new CaseFilterStatusView()  );
      CaseFilterLabelView caseFilterLabelView = new CaseFilterLabelView();
      caseFilterLabelView.setLayout(new BoxLayout(caseFilterLabelView, BoxLayout.PAGE_AXIS));
      tabs.addTab( text( label ), caseFilterLabelView  );
      
      tabs.setMnemonicAt( 0, KeyEvent.VK_1 );
      tabs.setMnemonicAt( 1, KeyEvent.VK_2 );

      tabs.setFocusable( true );
      tabs.setFocusCycleRoot( true );

      add( tabs );
      
      new RefreshWhenShowing( this, model );
      new RefreshWhenShowing( this, caseFilterLabelView );
   }
   
}
