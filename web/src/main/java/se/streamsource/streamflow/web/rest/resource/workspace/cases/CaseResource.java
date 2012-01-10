/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.rest.resource.workspace.cases;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation.ConversationResource;
import se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation.ConversationsResource;
import se.streamsource.streamflow.web.rest.resource.workspace.cases.form.CaseSubmittedFormsResource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JAVADOC
 */
@RequiresPermission( PermissionType.read )
public class CaseResource
      extends CommandQueryResource
{
   public CaseResource()
   {
      super( CaseContext.class, CaseCommandsContext.class );
   }

   public OutputRepresentation exportpdf( CaseOutputConfigDTO config ) throws Throwable
   {
      final PDDocument pdf = context(CaseCommandsContext.class).exportpdf(config);

      OutputRepresentation representation = new OutputRepresentation( MediaType.APPLICATION_PDF )
      {
         @Override
         public void write( OutputStream outputStream ) throws IOException
         {
            COSWriter writer = null;
            try
            {
               writer = new COSWriter( outputStream );
               writer.write( pdf );
            } catch (COSVisitorException e)
            {
               // Todo Handle this error more gracefully...
               e.printStackTrace();
            } finally
            {
               if (pdf != null)
               {
                  pdf.close();
               }
               if (writer != null)
               {
                  writer.close();
               }
            }
         }
      };

      Disposition disposition = new Disposition();
      disposition.setFilename( RoleMap.role(CaseId.Data.class).caseId().get() + ".pdf" );
      disposition.setType( Disposition.TYPE_ATTACHMENT );
      representation.setDisposition( disposition );

      return representation;
   }

   @SubResource
   public void general()
   {
      subResource( CaseGeneralResource.class );
   }

   @SubResource
   public void conversations()
   {
      subResource( ConversationsResource.class );
   }

   @SubResource
   public void contacts()
   {
      subResource( ContactsResource.class );
   }

   @SubResource
   public void submittedforms()
   {
      subResource(CaseSubmittedFormsResource.class);
   }

   @SubResource
   public void formdrafts()
   {
      subResource( CaseFormDraftsResource.class );
   }

   @SubResource
   public void possibleforms()
   {
      subResource( CasePossibleFormsResource.class );
   }

   @SubResource
   public void attachments()
   {
      subResource( AttachmentsResource.class );
   }

   @SubResource
   public void caselog()
   {
      subResource( CaseLogResource.class );
   }
   
   @SubResource
   public void submitformonclose()
   {
      subResource( CaseFormOnCloseResource.class );
   }

   @SubResource
   public void note()
   {
      subResource( NoteResource.class );
   }
}
