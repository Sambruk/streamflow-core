/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.entity.EntityReference;
import org.restlet.resource.ResourceException;
import org.restlet.data.Reference;
import org.restlet.Context;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.AbstractListModel;
import java.util.List;

public class FormsListModel
      extends AbstractListModel
      implements Refreshable
{
   private List<ListItemValue> forms;

   WeakModelMap<String, FormSubmitModel> formSubmitModels = new WeakModelMap<String, FormSubmitModel>()
   {
      @Override
      protected FormSubmitModel newModel(String key)
      {
         Reference ref = client.getReference().clone();
         List<String> segments = ref.getSegments();
         segments.remove( ref.getSegments().size() - 1 );
         ref.setSegments( segments );
         ref.addSegment( "formdefinitions" ).addSegment( key );

         CommandQueryClient formDefinition = obf.newObjectBuilder( CommandQueryClient.class )
               .use( client.getClient(), new Context(), ref ).newInstance();

         List<ListItemValue> itemValues;
         try
         {
            itemValues = formDefinition.query( "fields", ListValue.class ).items().get();
         } catch (ResourceException e)
         {
            throw new OperationException(WorkspaceResources.could_not_get_form, e);
         }

         return obf.newObjectBuilder(FormSubmitModel.class)
               .use(client, itemValues, EntityReference.parseEntityReference( key )).newInstance();
      }
   };

   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   public void refresh() throws OperationException
   {
      try
      {
         forms = client.query( "applicableforms", ListValue.class ).items().get();
         fireContentsChanged(this, 0, forms.size());
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_get_submitted_form, e);
      }
   }

   public int getSize()
   {
      return forms.size();
   }

   public Object getElementAt(int i)
   {
      return forms.get(i);
   }

   public List<ListItemValue> formsList()
   {
      return forms;
   }

   public FormSubmitModel getFormSubmitModel(String key)
   {
      return formSubmitModels.get(key);
   }
}
