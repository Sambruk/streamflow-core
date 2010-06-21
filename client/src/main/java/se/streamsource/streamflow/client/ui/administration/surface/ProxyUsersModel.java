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

package se.streamsource.streamflow.client.ui.administration.surface;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.user.NewProxyUserCommand;
import se.streamsource.streamflow.resource.user.ProxyUserDTO;
import se.streamsource.streamflow.resource.user.ProxyUserListDTO;
import se.streamsource.streamflow.resource.user.UserEntityDTO;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class ProxyUsersModel
      extends AbstractTableModel
      implements EventListener, EventVisitor
{
   @Structure
   ValueBuilderFactory vbf;

   private List<ProxyUserDTO> proxyUsers;

   private String[] columnNames;
   private Class[] columnClasses;
   private boolean[] columnEditable;

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "createdProxyUser", "changedEnabled" );

   private CommandQueryClient client;

   public ProxyUsersModel( @Uses CommandQueryClient client ) throws ResourceException
   {
      this.client = client;
      columnNames = new String[]{
            text( AdministrationResources.user_enabled_label ),
            text( AdministrationResources.username_label ),
            text(AdministrationResources.description_label)
      };
      columnClasses = new Class[]{Boolean.class, String.class, String.class};
      columnEditable = new boolean[]{true, false, false};
      refresh();
   }

   private void refresh()
   {
      try
      {
         proxyUsers = client.query("index", ProxyUserListDTO.class).users().get();
         fireTableDataChanged();
       } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public int getRowCount()
   {
      return proxyUsers == null ? 0 : proxyUsers.size();
   }

   public int getColumnCount()
   {
      return 3;
   }

   public Object getValueAt( int row, int column )
   {
      switch (column)
      {
         case 0:
            return proxyUsers != null && !proxyUsers.get( row ).disabled().get();
         case 1:
            return proxyUsers == null ? "" : proxyUsers.get( row ).username().get();
         default:
            return proxyUsers == null ? "" : proxyUsers.get( row ).description().get();
      }
   }

   @Override
   public void setValueAt( Object aValue, int rowIndex, int column )
   {

      switch (column)
      {
         case 0:
            ProxyUserDTO user = proxyUsers.get( rowIndex );
            changeEnabled( user );
      }
   }

   @Override
   public boolean isCellEditable( int rowIndex, int columnIndex )
   {
      return columnEditable[columnIndex];
   }

   @Override
   public Class<?> getColumnClass( int column )
   {
      return columnClasses[column];
   }

   @Override
   public String getColumnName( int column )
   {
      return columnNames[column];
   }

   public void createProxyUser( NewProxyUserCommand proxyUserCommand )
   {
      try
      {
         client.postCommand( "createproxyuser", proxyUserCommand );
      } catch (ResourceException e)
      {
         try
         {
            ErrorResources resources = ErrorResources.valueOf( e.getMessage() );
            throw new OperationException( resources, e );
         } catch (Throwable t)
         {
            throw new RuntimeException( e.getMessage(), e );
         }
      }
   }


   public void changeEnabled( ProxyUserDTO proxyUser )
   {
      try
      {
         client.getSubClient( proxyUser.username().get() ).postCommand( "changeenabled" );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_change_user_disabled, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      Logger.getLogger( "administration" ).info( "Refresh proxy users" );
      refresh();

      return false;
   }

   public void resetPassword( int index, String password )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( password );

         client.getSubClient( proxyUsers.get( index ).username().get()).putCommand( "resetpassword", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.reset_password_failed, e );
      }
   }

      public void remove( int index )
   {
      try
      {
         client.getSubClient( proxyUsers.get( index ).username().get() ).putCommand( "delete" );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_proxyuser, e );
      }
      refresh();
   }
}