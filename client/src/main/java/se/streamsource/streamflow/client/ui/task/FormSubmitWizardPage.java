/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import org.jdesktop.application.ApplicationContext;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.netbeans.spi.wizard.Wizard;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.entity.EntityReference;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * JAVADOC
 */
public class FormSubmitWizardPage
      extends WizardPage
{
   public FormSubmitWizardPage(@Uses java.util.List<ListItemValue> fields,
                               @Uses Map wizardValueMap)
   {
      setLayout(new BorderLayout());
      JPanel panel = new JPanel( new FormLayout( ) );

      FormLayout formLayout = new FormLayout( "100dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      for (ListItemValue value : fields)
      {
         TextField textField = new TextField();
         wizardValueMap.put( value.entity().get().identity(), textField );
         formBuilder.append(value.description().get(), textField);
      }
      JScrollPane scroll = new JScrollPane(panel);
      add(scroll,  BorderLayout.CENTER);
   }

   private WizardPanelNavResult forward( Map map )
   {
      for (Object o : map.entrySet())
      {
         Object value = ((Map.Entry) o).getValue();
         if (value instanceof TextField)
         {
            String id = (String) ((Map.Entry) o).getKey();
            String fieldValue = ((TextField) map.get( id )).getText();
            if (fieldValue == null || fieldValue.equals( "" ))
            {
               setProblem( i18n.text( WorkspaceResources.fill_all_fields));
               return WizardPanelNavResult.REMAIN_ON_PAGE;
            }
         }
      }
      return WizardPanelNavResult.PROCEED;
   }
}