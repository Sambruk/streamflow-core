/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.users;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.HtmlErrorMessageExtractor;
import se.streamsource.streamflow.client.resource.organizations.OrganizationsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.user.UserEntityDTO;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class UsersAdministrationModel
      extends AbstractTableModel
      implements EventListener, EventHandler
{

   private List<UserEntityDTO> users;
   private OrganizationsClientResource organizations;

   private String[] columnNames;
   private Class[] columnClasses;
   private boolean[] columnEditable;

   private EventHandlerFilter eventFilter = new EventHandlerFilter( this, "createdUser", "changedEnabled" );


   public UsersAdministrationModel( @Uses OrganizationsClientResource organizations ) throws ResourceException
   {
      this.organizations = organizations;
      columnNames = new String[]{text( AdministrationResources.user_enabled_label ), text( AdministrationResources.username_label )};
      columnClasses = new Class[]{Boolean.class, String.class};
      columnEditable = new boolean[]{true, false};
      refresh();
   }

   private void refresh()
   {
      try
      {
         users = organizations.users().users().get();
         fireTableDataChanged();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_organizations, e );
      }
   }

   public int getRowCount()
   {
      return users == null ? 0 : users.size();
   }

   public int getColumnCount()
   {
      return 2;
   }

   public Object getValueAt( int row, int column )
   {
      switch (column)
      {
         case 0:
            return users != null && !users.get( row ).disabled().get();
         default:
            return users == null ? "" : users.get( row ).username().get();
      }
   }

   @Override
   public void setValueAt( Object aValue, int rowIndex, int column )
   {
      switch (column)
      {
         case 0:
            UserEntityDTO user = users.get( rowIndex );
            changeDisabled( user );
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

   public void createUser( String username, String password )
   {
      try
      {
         organizations.createUser( username, password );
      } catch (ResourceException e)
      {
         throw new OperationException( ErrorResources.valueOf( HtmlErrorMessageExtractor.parse( e.getMessage() ) ), e );
      }
   }


   public void changeDisabled( UserEntityDTO user )
   {
      try
      {
         organizations.changeDisabled( user );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_change_user_disabled, e );
      }
   }

   public void importUsers( File f )
   {
      try
      {
         MediaType type = f.getName().endsWith( ".xls" )
               ? MediaType.APPLICATION_EXCEL
               : MediaType.TEXT_CSV;

         Representation representation = new FileRepresentation( f, type );

         organizations.importUsers( representation );

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_import_users, e );

      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.handleEvent( event );
   }

   public boolean handleEvent( DomainEvent event )
   {
      Logger.getLogger( "administration" ).info( "Refresh organizations users" );
      refresh();

      return false;
   }

   public void resetPassword( int index, String password )
   {
      try
      {
         organizations.resetPassword( users.get( index ).entity().get(), password );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.reset_password_failed, e );
      }
   }
}
