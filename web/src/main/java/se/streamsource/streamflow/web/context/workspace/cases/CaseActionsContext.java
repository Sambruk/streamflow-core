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

package se.streamsource.streamflow.web.context.workspace.cases;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseVisitorConfigValue;
import se.streamsource.streamflow.web.application.pdf.CasePdfGenerator;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.entity.caze.PossibleActions;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresAssigned;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.CaseStructure;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import static se.streamsource.dci.api.RoleMap.*;
import static se.streamsource.streamflow.domain.interaction.gtd.CaseStates.*;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountCacheConcern.class)
@Mixins(CaseActionsContext.Mixin.class)
public interface CaseActionsContext
      extends DeleteContext, Context // , InteractionConstraints
{
   // List possible actions

   public Actions actions();

   public LinksValue possiblesendto();

   public LinksValue possibleresolutions();

   // Commands

   /**
    * Assign the case to the user invoking the method
    */
   @RequiresAssigned(false)
   public void assign();

   /**
    * Mark the draft case as open
    */
   @RequiresStatus({DRAFT})
   public void open();

   /**
    * Mark the case as closed
    */
   @RequiresStatus({OPEN})
   public void close();


   /**
    * Mark the case as resolved and closed
    */
   @RequiresStatus({OPEN})
   public void resolve( EntityValue resolution );

   /**
    * Mark the case as on-hold
    */
   @RequiresStatus({OPEN})
   public void onhold();

   public void sendto( EntityValue entity );

   @RequiresStatus({CLOSED})
   public void reopen();

   @RequiresStatus({CaseStates.ON_HOLD})
   public void resume();

   @RequiresAssigned(true)
   public void unassign();

   public void delete();

   public OutputRepresentation exportpdf( CaseVisitorConfigValue config ) throws Throwable;

   abstract class Mixin
         implements CaseActionsContext
   {
      @Structure
      Module module;

      // List possible actions

      public Actions actions()
      {
         ValueBuilder<Actions> builder = module.valueBuilderFactory().newValueBuilder( se.streamsource.streamflow.domain.interaction.gtd.Actions.class );
         List<String> actions = builder.prototype().actions().get();

         PossibleActions possibleActions = RoleMap.role( PossibleActions.class );
         Actor actor = RoleMap.role( Actor.class );

         possibleActions.addActions( actor, actions );

         return builder.newInstance();
      }

      public LinksValue possiblesendto()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "sendto" );
         List<Project> projects = RoleMap.role( CaseTypeQueries.class ).possibleProjects();
         Ownable ownable = RoleMap.role( Ownable.class );
         CaseType caseType = RoleMap.role( TypedCase.Data.class ).caseType().get();
         for (Project project : projects)
         {
            if (!ownable.isOwnedBy( (Owner) project ))
            {
               if (caseType == null || project.hasSelectedCaseType( caseType ))
                  builder.addDescribable( project, ((OwningOrganizationalUnit.Data) project).organizationalUnit().get() );
            }
         }
         return builder.newLinks();
      }

      public LinksValue possibleresolutions()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "resolve" );
         CaseType type = RoleMap.role( TypedCase.Data.class ).caseType().get();
         if (type != null)
         {
            Iterable<Resolution> resolutions = type.getSelectedResolutions();
            builder.addDescribables( resolutions );
         }
         return builder.newLinks();
      }

      // Commands

      public void assign()
      {
         Assignable assignable = RoleMap.role( Assignable.class );

         Assignee assignee = RoleMap.role( Actor.class );

         assignable.assignTo( assignee );
      }

      public void open()
      {
         Status aCase = RoleMap.role( Status.class );

         aCase.open();
      }

      public void close()
      {
         CaseEntity aCase = RoleMap.role( CaseEntity.class );

         Actor actor = RoleMap.role( Actor.class );

         if (!aCase.isAssigned())
         {
            aCase.assignTo( actor );
         }

         aCase.close();
      }

      public void resolve( EntityValue resolutionDTO )
      {
         Resolution resolution = module.unitOfWorkFactory().currentUnitOfWork().get( Resolution.class, resolutionDTO.entity().get() );

         Assignable assignable = RoleMap.role( Assignable.class );
         Resolvable resolvable = RoleMap.role( Resolvable.class );
         Status status = RoleMap.role( Status.class );

         Actor actor = RoleMap.role( Actor.class );

         if (!assignable.isAssigned())
         {
            assignable.assignTo( actor );
         }

         resolvable.resolve( resolution );

         status.close();
      }

      public void onhold()
      {
         RoleMap.role( Status.class ).onHold();
      }

      public void sendto( EntityValue entity )
      {
         CaseEntity aCase = RoleMap.role( CaseEntity.class );

         Owner toOwner = module.unitOfWorkFactory().currentUnitOfWork().get( Owner.class, entity.entity().get() );

         aCase.unassign();

         aCase.changeOwner( toOwner );
      }

      public void reopen()
      {
         // Reopen the case, take away resolution, and assign to user who did the reopen
         Status caze = RoleMap.role( Status.class );
         caze.reopen();
         Resolvable resolvable = RoleMap.role( Resolvable.class );
         resolvable.unresolve();
         Assignable assignable = RoleMap.role( Assignable.class );
         Assignee assignee = RoleMap.role( Assignee.class );
         assignable.assignTo( assignee );
      }

      public void resume()
      {
         RoleMap.role( Status.class ).resume();
      }

      public void unassign()
      {
         Assignable caze = RoleMap.role( Assignable.class );

         caze.unassign();
      }

      public void delete()
      {
         Removable caze = RoleMap.role( Removable.class );
         caze.deleteEntity();
      }

      public OutputRepresentation exportpdf( CaseVisitorConfigValue config ) throws Throwable
      {
         Locale locale = role( Locale.class );

         Ownable.Data caze = RoleMap.role( Ownable.Data.class );
         Ownable.Data project = (Ownable.Data) caze.owner().get();
         Owner ou = project.owner().get();

         Organization org = ((OwningOrganization) ou).organization().get();

         AttachedFile.Data template = (AttachedFile.Data) ((CasePdfTemplate.Data) org).casePdfTemplate().get();

         if(template == null)
         {
            template = (AttachedFile.Data) ((DefaultPdfTemplate.Data) org).defaultPdfTemplate().get();
         }

         String uri = null;
         if (template != null)
         {
            uri = template.uri().get();
         }

         CasePdfGenerator exporter = module.objectBuilderFactory().newObjectBuilder( CasePdfGenerator.class ).use( config, uri, locale ).newInstance();//new CasePdfGenerator( config, "", locale );

         ((CaseStructure)caze).visitCase( exporter );

         final PDDocument pdf = exporter.getPdf();

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
         disposition.setFilename( ((Describable) caze).getDescription() + ".pdf" );
         disposition.setType( Disposition.TYPE_ATTACHMENT );
         representation.setDisposition( disposition );

         return representation;
      }
   }
}