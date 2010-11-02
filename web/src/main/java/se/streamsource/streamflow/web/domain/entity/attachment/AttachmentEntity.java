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

import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.Association;
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
               // Remove all default pdf template usage of this attachement
               Association<Attachment> defaultPdfTemplate = templateFor( DefaultPdfTemplate.Data.class ).defaultPdfTemplate();
               Query<DefaultPdfTemplate> defaultPdfTemplateQuery = qbf.newQueryBuilder( DefaultPdfTemplate.class ).
                     where( QueryExpressions.eq( defaultPdfTemplate, api.dereference( attachment ) ) ).
                     newQuery( uowf.currentUnitOfWork() );

               for (DefaultPdfTemplate defaultPdfTemplateUsage : defaultPdfTemplateQuery)
               {
                  defaultPdfTemplateUsage.setDefaultPdfTemplate( null );
               }

               // Remove all form template usage of this attachement
               Association<Attachment> formPdfTemplate = templateFor( FormPdfTemplate.Data.class ).formPdfTemplate();
               Query<FormPdfTemplate> formPdfTemplateQuery = qbf.newQueryBuilder( FormPdfTemplate.class ).
                     where( QueryExpressions.eq( formPdfTemplate, api.dereference( attachment ) ) ).
                     newQuery( uowf.currentUnitOfWork() );

               for (FormPdfTemplate formPdfTemplateUsage : formPdfTemplateQuery)
               {
                  formPdfTemplateUsage.setFormPdfTemplate( null );
               }

               // Remove all case template usage of this attachement
               Association<Attachment> casePdfTemplate = templateFor( CasePdfTemplate.Data.class ).casePdfTemplate();
               Query<CasePdfTemplate> casePdfTemplateUsages = qbf.newQueryBuilder( CasePdfTemplate.class ).
                     where( QueryExpressions.eq( casePdfTemplate, api.dereference( attachment ) ) ).
                     newQuery( uowf.currentUnitOfWork() );

               for (CasePdfTemplate casePdfTemplateUsage : casePdfTemplateUsages)
               {
                  casePdfTemplateUsage.setCasePdfTemplate( null );
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

