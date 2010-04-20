/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web;

import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.domain.CommonDomainAssembler;
import se.streamsource.streamflow.resource.CommonResourceAssembler;
import se.streamsource.streamflow.web.application.console.ConsoleAssembler;
import se.streamsource.streamflow.web.application.mail.MailAssembler;
import se.streamsource.streamflow.web.application.management.ManagementAssembler;
import se.streamsource.streamflow.web.application.migration.MigrationAssembler;
import se.streamsource.streamflow.web.application.notification.NotificationAssembler;
import se.streamsource.streamflow.web.application.organization.BootstrapAssembler;
import se.streamsource.streamflow.web.application.security.SecurityAssembler;
import se.streamsource.streamflow.web.application.statistics.StatisticsAssembler;
import se.streamsource.streamflow.web.configuration.ConfigurationAssembler;
import se.streamsource.streamflow.web.context.InteractionsAssembler;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationAssembler;
import se.streamsource.streamflow.web.domain.entity.form.FormAssembler;
import se.streamsource.streamflow.web.domain.entity.label.LabelAssembler;
import se.streamsource.streamflow.web.domain.entity.organization.GroupAssembler;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationAssembler;
import se.streamsource.streamflow.web.domain.entity.organization.RoleAssembler;
import se.streamsource.streamflow.web.domain.entity.project.ProjectAssembler;
import se.streamsource.streamflow.web.domain.entity.task.TaskAssembler;
import se.streamsource.streamflow.web.domain.entity.tasktype.TaskTypeAssembler;
import se.streamsource.streamflow.web.domain.entity.user.UserAssembler;
import se.streamsource.streamflow.web.domain.interaction.comment.CommentAssembler;
import se.streamsource.streamflow.web.infrastructure.database.DatabaseAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.EntityFinderAssembler;
import se.streamsource.streamflow.web.infrastructure.domain.ServerEntityStoreAssembler;
import se.streamsource.streamflow.web.infrastructure.event.EventAssembler;
import se.streamsource.streamflow.web.infrastructure.index.EmbeddedSolrAssembler;
import se.streamsource.streamflow.web.resource.ServerResourceAssembler;
import se.streamsource.streamflow.web.rest.StreamFlowRestAssembler;

/**
 * JAVADOC
 */
public class StreamFlowWebAssembler
      implements ApplicationAssembler
{
   private Object[] serviceObjects;

   public StreamFlowWebAssembler( Object... serviceObjects )
   {
      this.serviceObjects = serviceObjects;
   }

   public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException
   {
      ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
      assembly.setName( "StreamFlowServer" );
      assembly.setVersion( "0.6.24.1488" );
      LayerAssembly configurationLayer = assembly.layerAssembly( "Configuration" );
      LayerAssembly domainInfrastructureLayer = assembly.layerAssembly( "Domain infrastructure" );
      LayerAssembly domainLayer = assembly.layerAssembly( "Domain" );
      LayerAssembly contextLayer = assembly.layerAssembly( "Interactions" );
      LayerAssembly appLayer = assembly.layerAssembly( "Application" );
      LayerAssembly webLayer = assembly.layerAssembly( "Web" );

      webLayer.uses( appLayer, contextLayer, domainLayer, domainInfrastructureLayer );
      appLayer.uses( contextLayer, domainLayer, domainInfrastructureLayer, configurationLayer );
      contextLayer.uses(domainLayer);
      domainLayer.uses( domainInfrastructureLayer );
      domainInfrastructureLayer.uses( configurationLayer );

      assembleWebLayer( webLayer );

      assembleApplicationLayer( appLayer );

      assembleInteractionsLayer( contextLayer );

      assembleDomainLayer( domainLayer );

      assembleDomainInfrastructureLayer( domainInfrastructureLayer );

      assembleConfigurationLayer( configurationLayer );

      for (Object serviceObject : serviceObjects)
      {
         assembly.setMetaInfo( serviceObject );
      }

      return assembly;
   }

   private void assembleConfigurationLayer( LayerAssembly configurationlayer ) throws AssemblyException
   {
      new ConfigurationAssembler().assemble( configurationlayer.moduleAssembly( "Configuration" ) );
   }

   private void assembleDomainInfrastructureLayer( LayerAssembly domainInfrastructureLayer ) throws AssemblyException
   {
      new DatabaseAssembler().assemble( domainInfrastructureLayer.moduleAssembly( "Database" ) );
      new ServerEntityStoreAssembler().assemble( domainInfrastructureLayer.moduleAssembly( "Entity Store" ) );
      new EntityFinderAssembler().assemble( domainInfrastructureLayer.moduleAssembly( "Entity Finder" ) );
      new EventAssembler().assemble( domainInfrastructureLayer.moduleAssembly( "Events" ) );
      new EmbeddedSolrAssembler().assemble( domainInfrastructureLayer.moduleAssembly( "Search Engine" ));
   }


   protected void assembleWebLayer( LayerAssembly webLayer ) throws AssemblyException
   {
      ModuleAssembly restModule = webLayer.moduleAssembly( "REST" );
      new StreamFlowRestAssembler().assemble( restModule );
      new ServerResourceAssembler().assemble( restModule );
   }

   protected void assembleApplicationLayer( LayerAssembly appLayer ) throws AssemblyException
   {
      new ConsoleAssembler().assemble( appLayer.moduleAssembly( "Console" ) );
      new MigrationAssembler().assemble( appLayer.moduleAssembly( "Migration" ) );
      new ManagementAssembler().assemble( appLayer.moduleAssembly( "Management" ) );
      new SecurityAssembler().assemble( appLayer.moduleAssembly( "Security" ) );
      new BootstrapAssembler().assemble( appLayer.moduleAssembly( "Organization" ) );
      new StatisticsAssembler().assemble( appLayer.moduleAssembly( "Statistics" ) );
      new NotificationAssembler().assemble( appLayer.moduleAssembly( "Notification" ));
      new MailAssembler().assemble( appLayer.moduleAssembly( "Mail" ));
   }

   protected void assembleInteractionsLayer( LayerAssembly interactionsLayer ) throws AssemblyException
   {
      ModuleAssembly moduleAssembly = interactionsLayer.moduleAssembly( "Interactions" );
      new InteractionsAssembler().assemble( moduleAssembly );
   }

   protected void assembleDomainLayer( LayerAssembly domainLayer ) throws AssemblyException
   {
      new CommonDomainAssembler().assemble( domainLayer );
      new CommentAssembler().assemble( domainLayer.moduleAssembly( "Comments" ) );
      new CommonResourceAssembler().assemble( domainLayer.moduleAssembly( "Common" ) );
      new ConversationAssembler().assemble( domainLayer.moduleAssembly( "Conversations" ));
      new FormAssembler().assemble( domainLayer.moduleAssembly( "Forms" ) );
      new GroupAssembler().assemble( domainLayer.moduleAssembly( "Groups" ) );
      new LabelAssembler().assemble( domainLayer.moduleAssembly( "Labels" ) );
      new OrganizationAssembler().assemble( domainLayer.moduleAssembly( "Organizations" ) );
      new ProjectAssembler().assemble( domainLayer.moduleAssembly( "Projects" ) );
      new RoleAssembler().assemble( domainLayer.moduleAssembly( "Roles" ) );
      new TaskAssembler().assemble( domainLayer.moduleAssembly( "Tasks" ) );
      new TaskTypeAssembler().assemble( domainLayer.moduleAssembly( "Task types" ) );
      new UserAssembler().assemble( domainLayer.moduleAssembly( "Users" ) );
   }
}
