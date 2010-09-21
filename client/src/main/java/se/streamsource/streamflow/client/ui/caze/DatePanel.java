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

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.DateFunctions;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DatePanel
      extends AbstractFieldPanel
{
   private JXDatePicker datePicker;

   public DatePanel( @Uses FieldSubmissionValue field )
   {
      super( field );
      setLayout( new BorderLayout( ) );

      datePicker = new JXDatePicker();
      datePicker.setFormats( DateFormat.getDateInstance( DateFormat.MEDIUM, Locale.getDefault() ) );

      add( datePicker, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      Date date = datePicker.getDate();
      return date==null ? "" : DateFunctions.toUtcString( date );
   }

   @Override
   public void setValue( String newValue )
   {
      if ( !( newValue.isEmpty() ) )
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
      datePicker.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            binding.updateProperty( getValue() );
         }
      });
   }
}