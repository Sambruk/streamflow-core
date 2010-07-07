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

package se.streamsource.streamflow.client.ui.administration.users;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class UsersAdministrationModel
      extends AbstractTableModel
      implements EventListener, EventVisitor
{
   final Logger logger = LoggerFactory.getLogger( "administration" );
   @Structure
   ValueBuilderFactory vbf;

   private List<UserEntityDTO> users;

   private String[] columnNames;
   private Class[] columnClasses;
   private boolean[] columnEditable;

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "createdUser", "changedEnabled" );

   private CommandQueryClient client;

   public UsersAdministrationModel( @Uses CommandQueryClient client ) throws ResourceException
   {
      this.client = client;
      columnNames = new String[]{text( AdministrationResources.user_enabled_label ), text( AdministrationResources.username_label )};
      //columnNames = new String[]{text( AdministrationResources.username_label )};
      columnClasses = new Class[]{Boolean.class, String.class};
      //columnClasses = new Class[]{String.class};
      columnEditable = new boolean[]{true, false};
      //columnEditable = new boolean[]{false};
      refresh();
   }

   private void refresh()
   {
      try
      {
         users = client.query("users", UserEntityListDTO.class).users().get();
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
      //return 1;
   }

   public Object getValueAt( int row, int column )
   {
      //return users == null ? "" : users.get( row ).username().get();
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

   public void createUser( NewUserCommand userCommand )
   {
      try
      {
         client.postCommand( "createuser", userCommand );
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


   public void changeDisabled( UserEntityDTO user )
   {
      try
      {
         client.getSubClient(user.entity().get().identity()).postCommand( "changedisabled" );
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

         client.postCommand( "importusers", representation );

      } catch (ResourceException e)
      {
         throw new RuntimeException( e.getMessage(), e );

      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      logger.info( "Refresh organizations users" );
      refresh();

      return false;
   }

   public void resetPassword( int index, String password )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( password );

         client.getSubClient( users.get( index ).entity().get().identity() ).putCommand( "resetpassword", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.reset_password_failed, e );
      }
   }
}
