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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JPanel;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.BindingFormBuilder;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.util.Strings;

/**
 * Abstract class that each field type must extend
 */
public abstract class AbstractFieldPanel extends JPanel
{
   private FieldSubmissionDTO field;
   protected StateBinder.Binding binding;

   @Structure
   protected Module module;

   public AbstractFieldPanel( FieldSubmissionDTO field )
   {
      this.field = field;
   }

   abstract public String getValue();

   abstract public void setValue( String newValue );

   abstract public boolean validateValue( Object newValue );

   public StateBinder bindComponent( BindingFormBuilder bb, FieldSubmissionDTO DTO)
   {
      StateBinder stateBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      FieldSubmissionDTO value1 = stateBinder.bindingTemplate( FieldSubmissionDTO.class );

      String toolTip = DTO.field().get().note().get();
      if (Strings.empty( toolTip ))
      {
         bb.append( componentName(), this, value1.value(), stateBinder );
      } else
      {
         bb.append( componentName(), toolTip, this, value1.value(), stateBinder );
      }

      stateBinder.updateWith(DTO);
      return stateBinder;
   }

   public FieldSubmissionDTO getField()
   {
      return field;
   }

   public String title()
   {
      return field.field().get().description().get();
   }

   public boolean mandatory()
   {
      return field.field().get().mandatory().get();
   }

   protected String componentName()
   {
      StringBuilder componentName = new StringBuilder( "<html>" );
      componentName.append( title() );
      if (mandatory())
      {
         componentName.append( " <font color='red'>*</font>" );
      }
      componentName.append( "</html>" );
      return componentName.toString();
   }

   public Component firstFocusableComponent( Container container)
   {
      for( Component component : container.getComponents() )
      {
         if( component instanceof JPanel )
         {
            return firstFocusableComponent( (JPanel)component );
         } else
         {
            if( component.isFocusable())
               return component;
         }
      }
      return null;
   }

   abstract public void setBinding( StateBinder.Binding binding );
}