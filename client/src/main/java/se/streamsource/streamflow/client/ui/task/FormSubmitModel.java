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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.domain.form.FormDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

/**
 * Model for a FormDefinition
 */
public class FormSubmitModel
{
   @Uses
   private FormDefinitionValue formDefinition;

   @Uses
   private EntityReference formEntityReference;

   @Uses
   private CommandQueryClient client;

   @Structure
   private ValueBuilderFactory vbf;

   private String[] pageIds;
   private String[] pageNames;
   private Map<String, List<FieldDefinitionValue>> wizardPageMap;

   public List<FieldDefinitionValue> fieldsForPage(String pageId)
   {
      return wizardPageMap.get( pageId );
   }

   public FormDefinitionValue formDefinition()
   {
      return formDefinition;
   }

   public EntityReference formEntityReference()
   {
      return formEntityReference;
   }

   public void submit(SubmitFormDTO submitFormDTO)
   {
      try
      {
         client.postCommand( "submitform", submitFormDTO);
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_submit_form, e);
      }
   }

   public void setUpWizardPages()
   {
      if (wizardPageMap == null)
      {
         wizardPageMap = new HashMap<String, List<FieldDefinitionValue>>();

         //ValueBuilder<ListValue> listBuilder = vbf.newValueBuilder( ListValue.class );
         //ValueBuilder<ListItemValue> itemBuilder = vbf.newValueBuilder( ListItemValue.class );
         List<FieldDefinitionValue> wizardPage = new ArrayList<FieldDefinitionValue>();
         //ValueBuilder<List<FieldDefinitionValue>> listBuilder = new vbf.newValueBuilder( List.class );
         List<String> pageIds = new ArrayList<String>();
         List<String> pageNames = new ArrayList<String>();
         pageIds.add( formDefinition.form().get().identity() );
         pageNames.add( formDefinition.description().get() );

         for (FieldDefinitionValue value : formDefinition.fields().get())
         {
            if (value.fieldValue().get() instanceof PageBreakFieldValue)
            {
               wizardPageMap.put( pageIds.get( pageIds.size()-1 ), wizardPage);
               wizardPage = new ArrayList<FieldDefinitionValue>();
               pageIds.add( value.field().get().identity() );
               pageNames.add( value.description().get() );
            } else
            {
               wizardPage.add( value );
               //itemBuilder.prototype().entity().set( value.field().get() );
               //itemBuilder.prototype().description().set( value.description().get() );
               //listBuilder.prototype().items().get().add( itemBuilder.newInstance() );
            }
         }
         wizardPageMap.put( pageIds.get( pageIds.size()-1 ), wizardPage);

         this.pageIds = new String[ pageIds.size() ];
         pageIds.toArray( this.pageIds );
         this.pageNames = new String[ pageNames.size() ];
         pageNames.toArray( this.pageNames );
      }
   }

   public String[] getPageIds()
   {
      return pageIds;
   }

   public String[] getPageNames()
   {
      return pageNames;
   }
}