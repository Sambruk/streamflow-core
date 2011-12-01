package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public class CaseLogView extends JPanel implements TransactionListener, Refreshable
{

   private final CaseLogModel model;
   private final Module module;

   private JList list = new JXList();
   private JScrollPane newMessagePane;

   public CaseLogView(@Service ApplicationContext context, @Uses CaseLogModel model, @Structure Module module)
   {
      this.model = model;
      this.module = module;

      ActionMap am;
      setActionMap(am = context.getActionMap(this));

      // Layout and form for the left panel
      FormLayout rightLayout = new FormLayout( "30dlu, 300:grow, 50dlu", "pref, fill:pref:grow, 60dlu" );
      setLayout( rightLayout );
      setFocusable( false );
      DefaultFormBuilder rightBuilder = new DefaultFormBuilder( rightLayout, this );
      rightBuilder.setBorder( Borders.createEmptyBorder( Sizes.DLUY2, Sizes.DLUX2, Sizes.DLUY2, Sizes.DLUX2 ) );

      JLabel caseLogLabel = new JLabel( i18n.text( WorkspaceResources.case_log ) );
      rightBuilder.add( caseLogLabel, new CellConstraints( 1, 1, 2, 1, CellConstraints.LEFT, CellConstraints.TOP,
            new Insets( 0, 0, 0, 0 ) ) );
      rightBuilder.nextColumn();
      rightBuilder.add( new JButton( "Filter" ), new CellConstraints( 3, 1, 1, 1, CellConstraints.RIGHT,
            CellConstraints.TOP, new Insets( 0, 0, 0, 0 ) ) );
      rightBuilder.nextLine();

      // Caselog
      ((JXList) list).addHighlighter( HighlighterFactory.createAlternateStriping() );
      list.setModel( new EventListModel<CaseLogEntryDTO>( model.caselogs() ) );
      list.setCellRenderer( new CaseLogListCellRenderer() );
      list.setFixedCellHeight( -1 );
      list.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      JScrollPane scroll = new JScrollPane( list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
      scroll.setMinimumSize( new Dimension( 250, 100 ) );
      scroll.setPreferredSize( new Dimension( 400, 300 ) );
      rightBuilder.setExtent( 3, 1 );
      rightBuilder.add( scroll, new CellConstraints( 1, 2, 3, 1, CellConstraints.FILL, CellConstraints.FILL,
            new Insets( 0, 0, 0, 0 ) ) );

      newMessagePane = (JScrollPane) TEXTAREA.newField();
      newMessagePane.setMinimumSize( new Dimension( 10, 10 ) );
      newMessagePane.setPreferredSize( new Dimension( 10, 70 ) );
      rightBuilder.add( newMessagePane, new CellConstraints( 1, 3, 2, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 10, 0, 0, 0 ) ) );

      javax.swing.Action addMessageAction = am.get( "addMessage" );
      JButton writeMessage = new JButton( addMessageAction );
      rightBuilder.add( writeMessage, new CellConstraints( 3, 3, 1, 1, CellConstraints.FILL, CellConstraints.TOP,
            new Insets( 8, 5, 0, 0 ) ) );
      
      // refreshComponents.enabledOn( "changeNewMessage",
      // newMessagePane.getViewport().getView() );
      // actionBinder.bind( "changeNote", newMessagePane );
      // valueBinder.bind( "note", newMessagePane );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();

      list.ensureIndexIsVisible( list.getModel().getSize() - 1 );
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (matches( withNames( "changedOwner", "changedCaseType", "changedStatus" ), transactions ))
      {
         refresh();
      }
   }

   @Action
   public void addMessage()
   {
      System.out.println( "Added the message..." );
   }
}
