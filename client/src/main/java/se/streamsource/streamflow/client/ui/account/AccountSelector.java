/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.account;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import info.aduna.io.IOUtil;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * Selection of active account
 */
public class AccountSelector
      extends JList
      implements ListEventListener, VetoableChangeListener
{
   private AccountsModel dataModel;
   private ApplicationContext context;

   private
   @Service
   DialogService dialogs;

   private @Service
   StreamflowApplication main;

   public AccountSelector( @Uses final AccountsModel dataModel )
   {
      super( new EventListModel( dataModel.getAccounts() ) );
      this.dataModel = dataModel;
      this.context = Application.getInstance().getContext();
      setCellRenderer( new LinkListCellRenderer() );

      dataModel.getAccounts().addListEventListener( this );
      VetoableListSelectionModel veto = new VetoableListSelectionModel();
      setSelectionModel( veto );
      setSelectionMode( VetoableListSelectionModel.SINGLE_SELECTION );
      veto.addVetoableChangeListener( this );

   }

   public AccountModel getSelectedAccount()
   {
      return getSelectedIndex() == -1 ? null : dataModel.accountModel( (LinkValue) getSelectedValue() );
   }

   public void listChanged( ListEvent listEvent )
   {
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            if (isSelectionEmpty() && dataModel.getAccounts().size() == 1)
            {
               setSelectedIndex( 0 );
            }
         }
      } );
   }

   public void vetoableChange( PropertyChangeEvent evt ) throws PropertyVetoException
   {
      try
      {
         if (evt.getNewValue() != null && (Integer) evt.getNewValue() != -1)
         {
            InputStream is = getClass().getResourceAsStream( "/version.properties" );
            Properties p = IOUtil.readProperties( is );

            String clientVersion = p.getProperty( "application.version" );

            if (clientVersion.startsWith("$"))
               return; // Dev mode - skip versioning check

            String response = dataModel.accountModel( (LinkValue) getModel().getElementAt((Integer) evt.getNewValue() )).test();
            System.out.print( response );

            if (response != null)
            {
               String str;
               BufferedReader reader = new BufferedReader(
                     new StringReader( response ) );

               while ((str = reader.readLine()) != null)
               {
                  str = str.trim();
                  if (str.startsWith( "Variant:" ))
                  {
                     if( Locale.getDefault().equals( new Locale( "sv", "SE") ) )
                     {
                        Locale.setDefault( new Locale( "sv", "SE", str.substring( str.indexOf( ":" ) + 1, str.length() ).trim()) );  
                     }
                  } else if (str.startsWith( "Version:" ))
                  {
                     int toIndex = str.indexOf( '-' ) != -1 ? str.indexOf( '-' ) : str.lastIndexOf( '.' );

                     if (toIndex == -1)
                        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

                     String serverVersion = str.substring( str.indexOf( ":" ) + 1, toIndex );

                     if (!clientVersion.startsWith( serverVersion.trim() ))
                     {
                        String msg = MessageFormat.format( i18n.text( AccountResources.version_missmatch ), clientVersion, serverVersion );
                        dialogs.showMessageDialog( this, msg, "Info" );
                        throw new PropertyVetoException( msg, evt );
                     }
                  }
               }
            }
         }

      } catch (ResourceException e)
      {
         String msg;
         if (Status.CLIENT_ERROR_UNAUTHORIZED.equals( e.getStatus() ))
         {
            msg = i18n.text( ErrorResources.unauthorized_access );
         } else
         {
            msg = i18n.text( AccountResources.resource_failure ) + " \r\n" + e.getStatus().toString();
         }
         dialogs.showMessageDialog( this, msg, "Info" );
         main.manageAccounts();
         throw new PropertyVetoException( msg, evt );
      } catch (IOException e)
      {
         String msg = i18n.text( AccountResources.cannot_read_stream );
         dialogs.showMessageDialog( this, msg, "Info" );
         throw new PropertyVetoException( msg, evt );
      }
   }
}
