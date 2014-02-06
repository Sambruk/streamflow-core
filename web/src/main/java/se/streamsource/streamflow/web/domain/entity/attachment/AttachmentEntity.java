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
package se.streamsource.streamflow.web.domain.entity.attachment;

import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Attachment
 */
@Concerns(AttachmentEntity.RemovableConcern.class)
public interface AttachmentEntity
      extends DomainEntity,
      Attachment,

      Describable.Data,
      AttachedFile.Data,
      Removable.Data
{

   abstract class RemovableConcern
         extends ConcernOf<Removable>
         implements Removable
   {
      @Structure
      Qi4j api;

      @Structure
      Module module;

      @This
      Attachment attachment;

      public boolean removeEntity()
      {
         if( !saveToRemove() )
         {
             throw new IllegalStateException( ErrorResources.attachment_remove_failed_template_usage.name() );
         }
         boolean removed = next.removeEntity();

         return removed;
      }

       private boolean saveToRemove() {
           int result = 0;
           // check if this attachment is used as template anywhere.
           Association<Attachment> defaultPdfTemplate = templateFor( DefaultPdfTemplate.Data.class ).defaultPdfTemplate();
           Query<DefaultPdfTemplate> defaultPdfTemplateQuery = module.queryBuilderFactory().newQueryBuilder(DefaultPdfTemplate.class).
                   where(QueryExpressions.eq(defaultPdfTemplate, api.dereference(attachment))).
                   newQuery(module.unitOfWorkFactory().currentUnitOfWork());

           result += defaultPdfTemplateQuery.count();

           // Remove all form template usage of this attachement
           Association<Attachment> formPdfTemplate = templateFor( FormPdfTemplate.Data.class ).formPdfTemplate();
           Query<FormPdfTemplate> formPdfTemplateQuery = module.queryBuilderFactory().newQueryBuilder(FormPdfTemplate.class).
                   where(QueryExpressions.eq(formPdfTemplate, api.dereference(attachment))).
                   newQuery(module.unitOfWorkFactory().currentUnitOfWork());

           result += formPdfTemplateQuery.count();

           // Remove all case template usage of this attachement
           Association<Attachment> casePdfTemplate = templateFor( CasePdfTemplate.Data.class ).casePdfTemplate();
           Query<CasePdfTemplate> casePdfTemplateUsages = module.queryBuilderFactory().newQueryBuilder(CasePdfTemplate.class).
                   where(QueryExpressions.eq(casePdfTemplate, api.dereference(attachment))).
                   newQuery(module.unitOfWorkFactory().currentUnitOfWork());

           result += casePdfTemplateUsages.count();

           return result == 0;
       }

       public void deleteEntity()
      {
         if( !saveToRemove() )
         {
             throw new IllegalStateException( ErrorResources.attachment_remove_failed_template_usage.name() );
         }
         attachment.deleteFile();
         next.deleteEntity();
      }
   }
}

