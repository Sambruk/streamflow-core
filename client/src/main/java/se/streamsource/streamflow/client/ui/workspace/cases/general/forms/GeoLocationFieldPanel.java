/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.ApplicationContext;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.DialogService;

public class GeoLocationFieldPanel extends AbstractFieldPanel
{
   private JTextField textField;
   private GeoLocationFieldValue fieldValue;

   @Service
   DialogService dialogs;

   private FormSubmissionWizardPageModel model;

   public GeoLocationFieldPanel(@Service ApplicationContext appContext, @Uses FieldSubmissionDTO field,
         @Uses GeoLocationFieldValue fieldValue, @Uses FormSubmissionWizardPageModel model)
   {
      super( field );
      this.model = model;
      setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
      this.fieldValue = fieldValue;

      textField = new JTextField();
      add(textField);
      textField.setColumns( 50 ); // TODO: Fix magic number

      setBorder(new LineBorder(Color.GREEN));

      JComponent mapViewer = setUpMapViewer();
      mapViewer.setPreferredSize(new Dimension(500, 400));
      add(mapViewer);
      
      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();
   }

   private JComponent setUpMapViewer() {
       JXMapViewer mapViewer = new JXMapViewer();

       // Create a TileFactoryInfo for OpenStreetMap
       TileFactoryInfo info = new OSMTileFactoryInfo();
       DefaultTileFactory tileFactory = new DefaultTileFactory(info);
       mapViewer.setTileFactory(tileFactory);

       // Use 8 threads in parallel to load the tiles
       tileFactory.setThreadPoolSize(8);

       // Set the focus
       GeoPosition frankfurt = new GeoPosition(50.11, 8.68);

       mapViewer.setZoom(7);
       mapViewer.setAddressLocation(frankfurt);

       return mapViewer;
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue(String newValue)
   {
      textField.setText( newValue );
   }

   @Override
   public boolean validateValue(Object newValue)
   {
      return true; // TODO: Validate geo value
   }

   @Override
   public void setBinding(final StateBinder.Binding binding)
   {
      final GeoLocationFieldPanel panel = this;
      textField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify(JComponent input)
         {
             // TODO: Verify geo value properly
             
//            if (!Strings.empty( fieldValue.regularExpression().get() )
//                  && !Strings.empty( ((JTextComponent) input).getText() ))
//            {
//               try
//               {
//                  new RegexPatternFormatter( fieldValue.regularExpression().get() )
//                        .stringToValue( ((JTextComponent) input).getText() );
//               } catch (ParseException e)
//               {
//                  dialogs.showMessageDialog( panel, i18n.text( CaseResources.regular_expression_does_not_validate ), "" );
//                  return false;
//               }
//            }
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      } );
   }

//   @Override
//   protected String componentName()
//   {
//      StringBuilder componentName = new StringBuilder( "<html>" );
//      componentName.append( title() );
//      if (!Strings.empty( fieldValue.hint().get() ))
//      {
//         componentName.append( " <font color='#778899'>(" ).append( fieldValue.hint().get() ).append( ")</font>" );
//      }
//
//      if (mandatory())
//      {
//         componentName.append( " <font color='red'>*</font>" );
//      }
//      componentName.append( "</html>" );
//      return componentName.toString();
//   }

}