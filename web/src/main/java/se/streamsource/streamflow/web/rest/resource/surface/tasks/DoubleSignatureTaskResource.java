/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.rest.resource.surface.tasks;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.workspace.cases.tasks.DoubleSignatureTaskContext;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;

/**
 * JAVADOC
 */
public class DoubleSignatureTaskResource extends CommandQueryResource
{
   public DoubleSignatureTaskResource()
   {
      super( DoubleSignatureTaskContext.class );
   }
  
   @SubResource
   public void formdraft()
   {
      DoubleSignatureTask.Data doubleSignatureTask = RoleMap.role( DoubleSignatureTask.Data.class );
      RoleMap.current().set( doubleSignatureTask.formDraft().get() );
      RoleMap.current().set( doubleSignatureTask.formDraft().get().getFormDraftValue() );
      RoleMap.current().set( doubleSignatureTask.caze().get() );
      RoleMap.current().set( doubleSignatureTask.accessPoint().get() );

      subResource( TaskFormDraftResource.class );
   }

   @SubResource
   public void submittedform()
   {
      DoubleSignatureTask.Data doubleSignatureTask = RoleMap.role( DoubleSignatureTask.Data.class );
      RoleMap.current().set( doubleSignatureTask.submittedForm().get() );

      subResource( TaskSubmittedFormResource.class );
   }
}