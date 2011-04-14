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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.calendar.DatePickerFormatter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.DateFunctions;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;

import javax.swing.text.*;
import java.awt.*;
import java.beans.*;
import java.text.*;
import java.util.*;

import static se.streamsource.streamflow.client.util.i18n.*;

public class DatePanel
      extends AbstractFieldPanel
{
   @Service
   DialogService dialogs;

   private JXDatePicker datePicker;

   public DatePanel( @Uses FieldSubmissionDTO field )
   {
      super( field );
      setLayout( new BorderLayout() );

      datePicker = new JXDatePicker();
      datePicker.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
      final DateFormat dateFormat =  DateFormat.getDateInstance( DateFormat.SHORT );
      dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

      datePicker.getEditor().setFormatterFactory( new DefaultFormatterFactory(new DatePickerFormatter( new DateFormat[]{dateFormat} ){

         @Override
         public Object stringToValue( String text ) throws ParseException
         {
            Object result;
            try
            {
            result = super.stringToValue( text );
            } catch( ParseException pe )
            {
               dialogs.showMessageDialog( DatePanel.this,
                     text( WorkspaceResources.wrong_format_msg ) + " " + ((SimpleDateFormat)dateFormat).toPattern(),
                     text( WorkspaceResources.wrong_format_title ) );
               throw pe;
            }
            return result;
         }
      }));

      add( datePicker, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      Date date = datePicker.getDate();
      return date == null ? "" : DateFunctions.toUtcString( date );
   }

   @Override
   public void setValue( String newValue )
   {
      if (!(newValue.isEmpty()))
      {
         datePicker.setDate( DateFunctions.fromString( newValue ) );
      }
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      datePicker.addPropertyChangeListener( new PropertyChangeListener()
      {
         public void propertyChange( PropertyChangeEvent e )
         {
            if ("date".equals( e.getPropertyName() ) )
            {
               binding.updateProperty( getValue() );
            }
         }
      });
   }
}