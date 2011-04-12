/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.ui.administration.caseaccessdefaults;

import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * JAVADOC
 */
public class CaseAccessDefaultsView
      extends JPanel
      implements Observer, TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   ObjectBuilder<SelectLinkDialog> possibleDefaultAccessTypes;

   private CaseAccessDefaultsModel model;
   private final ApplicationContext context;

   public CaseAccessDefaultsView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      this.context = context;
      model = obf.newObjectBuilder( CaseAccessDefaultsModel.class ).use( client ).newInstance();
      model.addObserver( this );

      setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

      new RefreshWhenShowing( this, model );
   }

   public void update( Observable o, Object arg )
   {
      removeAll();

      LinksValue links = model.getIndex();

      for (final LinkValue linkValue : links.links().get())
      {
         JButton comp = new JButton( linkValue.text().get() );
         comp.addActionListener( new ActionListener()
         {
            public void actionPerformed( ActionEvent e )
            {
               final SelectLinkDialog dialog = possibleDefaultAccessTypes.use(model.getPossibleDefaultAccessTypes( linkValue )).newInstance();
               dialog.setPreferredSize( new Dimension(200,100) );

               dialogs.showOkCancelHelpDialog( CaseAccessDefaultsView.this, dialog, i18n.text( AdministrationResources.choose_default_access_type ) );

               if (dialog.getSelectedLink() != null)
               {
                  context.getTaskService().execute( new CommandTask()
                  {
                     @Override
                     public void command()
                        throws Exception
                     {
                        model.changeDefaultAccessType( dialog.getSelectedLink() );
                     }
                  });
               }
            }
         });
         add( comp );
      }

      revalidate();
      repaint();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.refresh();
   }
}
