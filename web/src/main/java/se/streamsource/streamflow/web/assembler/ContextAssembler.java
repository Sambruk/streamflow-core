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
package se.streamsource.streamflow.web.assembler;

import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.EntityAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;

import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.surface.api.assembler.SurfaceAPIAssembler;
import se.streamsource.streamflow.util.ClassScanner;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.administration.HasJoined;
import se.streamsource.streamflow.web.context.util.TableQueryConverter;
import se.streamsource.streamflow.web.context.workspace.cases.HasFormOnDelete;
import se.streamsource.streamflow.web.infrastructure.index.NamedSolrDescriptor;

import java.util.Properties;

import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.INSTANCE;

/**
 * JAVADOC
 */
public class ContextAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
      interactions(layer.module("Context"));
   }

   private void interactions(ModuleAssembly module) throws AssemblyException
   {
      new ValueAssembler().assemble(module);
      new SurfaceAPIAssembler().assemble(module);

      module.importedServices(InteractionConstraintsService.class).
              importedBy(NewObjectImporter.class).
              visibleIn(Visibility.application);
      module.objects(InteractionConstraintsService.class);
      module.objects(TableQueryConverter.class);

      module.objects(RequiresPermission.RequiresPermissionConstraint.class,
              ServiceAvailable.ServiceAvailableConstraint.class, HasJoined.HasJoinedConstraint.class,  HasFormOnDelete.HasFormOnRemoveConstraint.class).visibleIn(Visibility.application);

      // Named queries
      NamedQueries namedQueries = new NamedQueries();
      NamedQueryDescriptor queryDescriptor = new NamedSolrDescriptor("solrquery", "");
      namedQueries.addQuery(queryDescriptor);

      module.importedServices(NamedEntityFinder.class).
              importedBy(ServiceSelectorImporter.class).
              setMetaInfo(ServiceQualifier.withId("solr")).
              setMetaInfo(namedQueries);

      // Velocity Engine for context layer
      Properties props = new Properties();
      try
      {
         props.load(getClass().getResourceAsStream("/velocity.properties"));

         VelocityEngine velocity = new VelocityEngine(props);

         module.importedServices(VelocityEngine.class)
               .importedBy(INSTANCE).setMetaInfo(velocity).visibleIn( layer );

      } catch (Exception e)
      {
         throw new AssemblyException("Could not load velocity properties", e);
      }

      // Register all contexts
      for (Class aClass : Iterables.filter(ClassScanner.matches(".*Context"), ClassScanner.getClasses(LinksBuilder.class)))
      {
         addResourceContexts(module, aClass);
      }
      module.values(Specifications.<Object>TRUE()).visibleIn(Visibility.application);
   }

   private Specification<EntityAssembly> assignableFrom(final Class<?> dataClass)
   {
      return new Specification<EntityAssembly>()
      {
         public boolean satisfiedBy(EntityAssembly item)
         {
            return dataClass.isAssignableFrom(item.type());
         }
      };
   }

   private void addResourceContexts(ModuleAssembly module, Class<?>... resourceContextClasses) throws AssemblyException
   {
      for (Class<?> resourceContextClass : resourceContextClasses)
      {
         if (CommandQueryResource.class.isAssignableFrom(resourceContextClass))
         {
            module.objects(resourceContextClass).visibleIn(Visibility.application);
         } else if (resourceContextClass.isInterface())
         {
            module.transients((Class<TransientComposite>) resourceContextClass).visibleIn(Visibility.application);
         } else
         {
            module.objects(resourceContextClass).visibleIn(Visibility.application);
         }
      }
   }
}
