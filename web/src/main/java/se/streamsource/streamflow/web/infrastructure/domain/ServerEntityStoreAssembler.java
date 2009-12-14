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

package se.streamsource.streamflow.web.infrastructure.domain;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmEntityStoreService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.migration.MigrationService;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * JAVADOC
 */
public class ServerEntityStoreAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (mode.equals( Application.Mode.development ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.test ))
      {
         // In-memory store
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
      } else if (mode.equals( Application.Mode.production ))
      {
         // JDBM storage
         module.addServices( JdbmEntityStoreService.class ).identifiedBy( "data" ).visibleIn( Visibility.application );
         module.addServices( UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );

         // Migration service
         // Enter all migration rules here
         // To-version should be of the form:
         // <major-version>.<minor-version>.<sprint>.<Svn-revision>
         // This way we can control how migrations are done from one
         // revision to the next.
         MigrationBuilder migrationBuilder = new MigrationBuilder( "0.0" );
         migrationBuilder.
               toVersion( "0.1.14.357" ).
               renameEntity( "se.streamsource.streamflow.web.domain.project.RoleEntity",
                     "se.streamsource.streamflow.web.domain.project.ProjectRoleEntity" ).
               forEntities( "se.streamsource.streamflow.web.domain.organization.OrganizationEntity",
                     "se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity" ).
               renameManyAssociation( "roles", "projectRoles" );

         module.addServices( MigrationService.class ).setMetaInfo( migrationBuilder );
      }
   }
}