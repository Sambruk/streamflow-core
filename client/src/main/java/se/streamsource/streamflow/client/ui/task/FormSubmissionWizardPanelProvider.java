/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.entity.EntityReference;

import javax.swing.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.TextField;

import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;

/**
 * JAVADOC
 */
public class FormSubmissionWizardPanelProvider
      extends WizardPanelProvider
{
   public static final String FORM_SUBMISSION_STEP = "FORM_SUBMISSION_STEP";

   private FormSubmitModel formSubmitModel;
   private ObjectBuilderFactory obf;
   private ValueBuilderFactory vbf;
   private Map<String, FormSubmitWizardPage> viewMap = new HashMap<String, FormSubmitWizardPage>();

   protected FormSubmissionWizardPanelProvider( ObjectBuilderFactory obf,
                                                ValueBuilderFactory vbf,
         FormSubmitModel formSubmitModel,
         String title, String[] pageIds, String[] pageNames)
   {
      super(title, pageIds, pageNames);
      this.formSubmitModel = formSubmitModel;
      this.obf = obf;
      this.vbf = vbf;
   }

   protected JComponent createPanel(WizardController wizardController, String pageId, final Map map)
   {
      return obf.newObjectBuilder(FormSubmitWizardPage.class)
            .use( formSubmitModel.fieldsForPage( pageId ), map ).newInstance();
   }

   @Override
   protected Object finish(Map map) throws WizardException
   {
      ValueBuilder<SubmitFormDTO> submittedFormBuilder = vbf.newValueBuilder(SubmitFormDTO.class);
      ValueBuilder<SubmittedFieldValue> fieldBuilder =vbf.newValueBuilder(SubmittedFieldValue.class);
      java.util.List<SubmittedFieldValue> fields = new ArrayList<SubmittedFieldValue>();

      for (Object o : map.entrySet())
      {
         Object value = ((Map.Entry) o).getValue();
         if (value instanceof TextField)
         {
            String id = (String) ((Map.Entry) o).getKey();
            EntityReference entityReference = EntityReference.parseEntityReference( id );
            fieldBuilder.prototype().field().set(entityReference);
            fieldBuilder.prototype().value().set(((TextField)map.get( id )).getText());
            fields.add(fieldBuilder.newInstance());
         }
      }
      submittedFormBuilder.prototype().values().set( fields );
      submittedFormBuilder.prototype().form().set( formSubmitModel.formEntityReference() );
      formSubmitModel.submit( submittedFormBuilder.newInstance() );
      return null;
   }
}