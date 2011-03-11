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

package se.streamsource.streamflow.client.ui.workspace.table;

import org.jdesktop.swingx.JXMonthView;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.util.Strings;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * This view shows a date picker combined with a period list.
 * If a date is picked in the date picker the period is picked date plus period and if no date is picked the time period
 * will be today minus period.
 */
public class PerspectivePeriodView
      extends JPanel
{
   @Service
   DialogService dialogs;

   boolean isCreationDate;
   PerspectiveModel model;

   public void initView( @Uses final PerspectiveModel model, @Uses final Boolean isCreationDate )
   {
      setLayout( new BorderLayout() );
      this.isCreationDate = (isCreationDate != null && isCreationDate);
      this.model = model;

      final JList list = new JList( Period.values() );

      list.setSelectedValue( this.isCreationDate ? model.getCreatedPeriod() : model.getDueOnPeriod(), true );
      list.setCellRenderer( new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
                                                        boolean cellHasFocus )
         {
            setFont( list.getFont() );
            setBackground( list.getBackground() );
            setForeground( list.getForeground() );
            if (value.equals( PerspectivePeriodView.this.isCreationDate ? model.getCreatedPeriod() : model.getDueOnPeriod() ))
            {
               setIcon( icon( Icons.check, 12 ) );
               setBorder( BorderFactory.createEmptyBorder( 4, 0, 0, 0 ) );
            } else
            {

               setIcon( null );
               setBorder( BorderFactory.createEmptyBorder( 4, 16, 0, 0 ) );
            }
            setText( text( (Period) value ) );
            return this;
         }
      } );

      list.addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent event )
         {
            if (!event.getValueIsAdjusting())
            {
               if ( PerspectivePeriodView.this.isCreationDate )
               {
                  model.setCreatedPeriod( (Period) list.getSelectedValue() );
               } else
               {
                  model.setDueOnPeriod( (Period) list.getSelectedValue() );
               }
            }
         }
      } );
      add( list, BorderLayout.WEST );

      JPanel datePicker = new JPanel( new BorderLayout() );
      datePicker.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );

      final JXMonthView monthView = new JXMonthView();
      monthView.setTraversable( true );

      final JTextField dateField = new JTextField();
      dateField.setBorder( BorderFactory.createTitledBorder( text( WorkspaceResources.from_date ) ) );

      if( isCreationDate )
      {
         if( model.getCreatedOn() != null )
         {
           dateField.setText( formatDate( model.getCreatedOn() ) );
           monthView.setSelectionDate( model.getCreatedOn() );
           monthView.ensureDateVisible( model.getCreatedOn() );
         }
      } else
      {
         if( model.getDueOn() != null )
         {
           dateField.setText( formatDate( model.getDueOn() ) );
           monthView.setSelectionDate( model.getDueOn() );
           monthView.ensureDateVisible( model.getDueOn() );
         }
      }
     
      dateField.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            if( !Strings.empty( dateField.getText() ) )
            {
               Date date = parseDate( dateField.getText() );

               if (date != null)
               {
                  monthView.setSelectionDate( date );
                  monthView.ensureDateVisible( monthView.getSelectionDate() );
                  setDateToModel( parseDate( dateField.getText() ) );
               } else
               {
                  dateField.setText( "" );
               }
            } else
            {
               monthView.setSelectionDate( null );
               setDateToModel( null );
            }
         }
      } );

      monthView.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent e )
         {
            dateField.setText( formatDate( monthView.getSelectionDate() ) );
            setDateToModel( monthView.getSelectionDate() );
         }
      } );

      datePicker.add( dateField, BorderLayout.NORTH );

      datePicker.add( monthView, BorderLayout.CENTER );

      add( datePicker, BorderLayout.EAST );
   }


   private Date parseDate( String date )
   {
      DateTimeFormatter format = DateTimeFormat.forPattern( text( WorkspaceResources.date_format) );
      Date result = null;
      try{
        result = format.parseDateTime( date ).toDate();
      } catch( IllegalArgumentException e )
      {
         dialogs.showMessageDialog( this,
               text( WorkspaceResources.wrong_format_msg) + text( WorkspaceResources.date_format ),
               text( WorkspaceResources.wrong_format_title));
      }
      return result;

   }

   private String formatDate( Date date )
   {
      DateTimeFormatter format = DateTimeFormat.forPattern( text( WorkspaceResources.date_format) );
      return format.print( new DateTime( date ) );
   }

   private void setDateToModel( Date date )
   {
      if (this.isCreationDate)
      {
         model.setCreatedOn( date );
      } else
      {
         model.setDueOn( date );
      }
   }


}
