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

package se.streamsource.streamflow.web.domain.entity.attachment;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.SelectedTemplate;

/**
 * Attachment
 */
@Concerns(AttachmentEntity.RemovableConcern.class)
public interface AttachmentEntity
      extends DomainEntity,
      Attachment,

      Describable.Data,
      AttachedFile.Data
{

   abstract class RemovableConcern
         extends ConcernOf<Removable>
         implements Removable
   {
      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Attachment attachment;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this attachment
         if (removed)
         {
            {
               // TODO remove all usage of this attachement
               SelectedTemplate.Data selectedTemplate = QueryExpressions.templateFor( SelectedTemplate.Data.class );
               Query<SelectedTemplate> templateUsages = qbf.newQueryBuilder( SelectedTemplate.class ).
                     where( QueryExpressions.eq( selectedTemplate.selectedTemplate(), attachment ) ).
                     newQuery( uowf.currentUnitOfWork() );

               for (SelectedTemplate selectedTemplateUsage : templateUsages)
               {
                  selectedTemplateUsage.removeSelectedTemplate( ((SelectedTemplate.Data) selectedTemplateUsage).selectedTemplate().get() );
               }
            }
         }

         return removed;
      }

      public void deleteEntity()
      {
         next.deleteEntity();
      }
   }
}

