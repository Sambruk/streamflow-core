/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseTypeQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresAssigned;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresOwner;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccess;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessRestriction;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.interaction.security.RequiresRestricted;
import se.streamsource.streamflow.web.domain.interaction.security.RequiresUnrestricted;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.FormOnClose;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.dci.api.RoleMap.*;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.*;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountCacheConcern.class)
@Mixins(CaseCommandsContext.Mixin.class)
@RequiresPermission(PermissionType.write)
public interface CaseCommandsContext
      extends DeleteContext, Context
{
   // List possible actions

   public LinksValue possiblesendto();

   public LinksValue possibleresolutions();

   /**
    * Assign the case to the user invoking the method
    */
   @RequiresStatus( OPEN )
   @RequiresAssigned(false)
   @RequiresRemoved(false)
   public void assign();

   /**
    * Mark the draft case as open
    */
   @RequiresStatus(DRAFT)
   @RequiresOwner
   @RequiresRemoved(false)
   public void open();

   /**
    * Mark the case as closed
    */
   @RequiresStatus( OPEN )
   @HasResolutions(false)
   @HasFormOnClose(false)
   @SubCasesAreClosed
   @RequiresRemoved(false)
   public void close();

   /**
    * Mark the case as resolved and closed
    */
   @RequiresStatus( OPEN )
   @HasResolutions(true)
   @HasFormOnClose(false)
   @RequiresRemoved(false)
   @SubCasesAreClosed
   public void resolve( EntityValue resolution );

   /**
    * Mark the case for submission of a certain form on close
    * and close the case when submitted.
    */
   @RequiresStatus( OPEN )
   @HasFormOnClose(true)
   @RequiresRemoved(false)
   @SubCasesAreClosed
   public void formonclose();

   @RequiresStatus({OPEN, DRAFT})
   @HasFormOnDelete(true)
   @RequiresRemoved(false)
   public void formondelete();

   @RequiresStatus({OPEN, DRAFT})
   @HasFormOnDelete(true)
   @RequiresRemoved(false)
   public StringValue formondeletename();

   /**
    * Mark the case as on-hold
    */
   @RequiresAssigned
   @RequiresStatus(OPEN)
   @RequiresRemoved(false)
   public void onhold();

   @RequiresStatus({DRAFT,OPEN})
   @RequiresRemoved(false)
   public void sendto( EntityValue entity );

   @RequiresStatus(CLOSED)
   @RequiresRemoved(false)
   public void reopen();

   @RequiresStatus(ON_HOLD)
   @RequiresRemoved(false)
   public void resume();

   @RequiresAssigned()
   @RequiresStatus(OPEN)
   @RequiresRemoved(false)
   public void unassign();

   @RequiresStatus({OPEN, DRAFT})
   @HasFormOnDelete(false)
   @RequiresRemoved(false)
   public void delete();

   @RequiresRemoved()
   @RequiresPermission(PermissionType.administrator)
   public void reinstate();

   @RequiresStatus( OPEN )
   @RequiresUnrestricted()
   @RequiresRemoved(false)
   public void restrict();

   @RequiresStatus( OPEN )
   @RequiresRestricted()
   @RequiresRemoved(false)
   public void unrestrict();

   @RequiresRemoved(false)
   public PDDocument exportpdf( CaseOutputConfigDTO config ) throws Throwable;

   public void read();

   abstract class Mixin
         implements CaseCommandsContext
   {
      @Structure
      Module module;

      @Service
      PdfGeneratorService pdfGenerator;

      // List possible actions
      public LinksValue possiblesendto()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "sendto" );
         List<Project> projects = RoleMap.role( CaseTypeQueries.class ).possibleProjects();
         Ownable ownable = RoleMap.role( Ownable.class );
         CaseType caseType = RoleMap.role( TypedCase.Data.class ).caseType().get();
         for (Project project : projects)
         {
            if (!ownable.isOwnedBy( project ))
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

      public void formonclose()
      {
         CaseType caseType = RoleMap.role( TypedCase.Data.class ).caseType().get();
         final Form formOnClose = (( FormOnClose.Data )caseType).formOnClose().get();
         List<SubmittedFormValue> submittedForms = RoleMap.role( SubmittedForms.Data.class ).submittedForms().get();

         boolean formOnCloseExists = matchesAny( new Specification<SubmittedFormValue>()
         {
            public boolean satisfiedBy( SubmittedFormValue item )
            {
               if (item.form().get().identity().equals( formOnClose.toString() ))
                  return true;
               return false;
            }
         }, submittedForms );

         if ( formOnCloseExists )
         {
            close();
         } else
         {
            throw new RuntimeException( "No form on close submission." );
         }

      }

      public void formondelete()
      {
         final Form form = getFormOnDelete();

         List<SubmittedFormValue> submittedForms = RoleMap.role( SubmittedForms.Data.class ).submittedForms().get();

         boolean formOnCloseExists = matchesAny( new Specification<SubmittedFormValue>()
         {
            public boolean satisfiedBy( SubmittedFormValue item )
            {
               if (item.form().get().identity().equals( form.toString() ))
                  return true;
               return false;
            }
         }, submittedForms );

         if ( formOnCloseExists )
         {
            delete();
         } else
         {
            throw new RuntimeException( "No form on remove submission." );
         }
      }

      public StringValue formondeletename()
      {
         final Form form = getFormOnDelete();

         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( form.getDescription() );
         return builder.newInstance();
      }

      private Form getFormOnDelete()
      {
         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         FormOnRemove.Data data = (FormOnRemove.Data) orgs.organization().get();

         return data.formOnRemove().get();
      }

      public void onhold()
      {
         RoleMap.role( Status.class ).onHold();
      }

      public void sendto( EntityValue entity )
      {
         CaseEntity aCase = RoleMap.role( CaseEntity.class );

         Owner toOwner = module.unitOfWorkFactory().currentUnitOfWork().get( Owner.class, entity.entity().get() );

         if (aCase.isAssigned())
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
         if( (CaseStates.DRAFT.equals( ((Status.Data) caze ).status().get() )) )
            caze.deleteEntity();
         else
            // just mark the case as removed
            caze.removeEntity();
      }

      public void reinstate()
      {
         Removable caze = RoleMap.role( Removable.class );
         caze.reinstate();
      }

      public void restrict()
      {
         CaseAccessRestriction secrecy = RoleMap.role( CaseAccessRestriction.class );
         secrecy.restrict();

         Organizations.Data orgs = module.unitOfWorkFactory().currentUnitOfWork().get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
         Organization org = orgs.organization().get();
         CaseAccessDefaults.Data defaults = (CaseAccessDefaults.Data) org;

         CaseAccess access = RoleMap.role( CaseAccess.class );
         for (Map.Entry<PermissionType, CaseAccessType> entry : defaults.accessPermissionDefaults().get().entrySet())
         {
            access.changeAccess( entry.getKey(), entry.getValue() );
         }
      }

      /**
       * This is not a perfect "undo". We cannot
       * go back to the previous settings before the
       * secrecy was enabled. Instead we force the
       * settings for the project and the case type
       */
      public void unrestrict()
      {
         CaseAccessRestriction secrecy = RoleMap.role( CaseAccessRestriction.class );
         secrecy.unrestrict();

         Ownable.Data owner = RoleMap.role( Ownable.Data.class );

         // force set secrecy setting of the project
         CaseAccessDefaults.Data defaults = (CaseAccessDefaults.Data) owner.owner().get();
         CaseAccess access = RoleMap.role( CaseAccess.class );
         access.clearAccess();
         for (Map.Entry<PermissionType, CaseAccessType> entry : defaults.accessPermissionDefaults().get().entrySet())
         {
            access.changeAccess( entry.getKey(), entry.getValue() );
         }

         // apply the case type setting
         TypedCase.Data data = RoleMap.role( TypedCase.Data.class );
         defaults = (CaseAccessDefaults.Data) data.caseType().get();
         if( defaults != null )
         {
            for (Map.Entry<PermissionType, CaseAccessType> entry : defaults.accessPermissionDefaults().get().entrySet())
            {
               access.changeAccess( entry.getKey(), entry.getValue() );
            }
         }
      }

      public PDDocument exportpdf( CaseOutputConfigDTO config ) throws Throwable
      {
         return pdfGenerator.generateCasePdf( role( CaseEntity.class ), config, role( Locale.class ) );
      }

      public void read()
      {
         RoleMap.role( Case.class ).setUnread( false );
      }
   }
}