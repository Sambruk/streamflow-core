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

package se.streamsource.streamflow.web.assembler;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rest.EntitiesResource;
import org.qi4j.library.rest.EntityResource;
import org.qi4j.library.rest.IndexResource;
import org.qi4j.library.rest.SPARQLResource;
import org.restlet.security.ChallengeAuthenticator;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.ResourceFinder;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.streamflow.util.ClassScanner;
import se.streamsource.streamflow.web.application.security.AuthenticationFilter;
import se.streamsource.streamflow.web.rest.StreamflowCaseResponseWriter;
import se.streamsource.streamflow.web.rest.StreamflowRestApplication;
import se.streamsource.streamflow.web.rest.StreamflowRestlet;
import se.streamsource.streamflow.web.rest.StreamflowResultConverter;
import se.streamsource.streamflow.web.rest.resource.APIRouter;
import se.streamsource.streamflow.web.rest.resource.RootResource;
import se.streamsource.streamflow.web.rest.service.conversation.ConversationResponseService;
import se.streamsource.streamflow.web.rest.service.conversation.NotificationService;
import se.streamsource.streamflow.web.rest.service.filter.FilterConfiguration;
import se.streamsource.streamflow.web.rest.service.filter.FilterService;

import static org.qi4j.api.common.Visibility.*;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * JAVADOC
 */
public class WebAssembler
   extends AbstractLayerAssembler
{
   public void assemble(LayerAssembly layer)
           throws AssemblyException
   {
      super.assemble(layer);

      rest(layer.module("REST"));
      services(layer.module("Services"));
   }

   private void rest(ModuleAssembly module) throws AssemblyException
   {
      module.objects(StreamflowRestApplication.class,
              ResourceFinder.class,
              EntityStateSerializer.class,
              EntityTypeSerializer.class);

      module.objects(SPARQLResource.class,
              IndexResource.class,
              EntitiesResource.class,
              EntityResource.class);

      module.importedServices(ChallengeAuthenticator.class);

      // Resources
      module.objects(
              APIRouter.class,
              AuthenticationFilter.class
      );

      new DCIAssembler().assemble(module);

      // Import file handling service for file uploads
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(1024 * 1000 * 30); // 30 Mb threshold TODO Make this into real service and make this number configurable
      module.importedServices(FileItemFactory.class).importedBy(INSTANCE).setMetaInfo(factory);

      module.importedServices(ResultConverter.class).importedBy(ImportedServiceDeclaration.NEW_OBJECT);
      module.objects(StreamflowResultConverter.class);

      module.importedServices(StreamflowCaseResponseWriter.class).importedBy(ImportedServiceDeclaration.NEW_OBJECT);
      module.objects(StreamflowCaseResponseWriter.class);

      module.objects(StreamflowRestlet.class).visibleIn(Visibility.application);

      // Register all resources
      for (Class aClass : Iterables.filter(ClassScanner.matches(".*Resource"), ClassScanner.getClasses(RootResource.class)))
      {
         module.objects(aClass);
      }
   }

   private void services(ModuleAssembly module)
   {
      module.services(FilterService.class).identifiedBy("filter").visibleIn(application).instantiateOnStartup();
      configuration().entities(FilterConfiguration.class);
      configuration().forMixin( FilterConfiguration.class ).declareDefaults().enabled().set( true );

      module.services( NotificationService.class )
            .identifiedBy( "notification" )
            .instantiateOnStartup()
            .visibleIn(application);

      module.services( ConversationResponseService.class )
            .identifiedBy("conversationresponse")
            .instantiateOnStartup()
            .visibleIn(application);

   }
}
