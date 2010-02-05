/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.infrastructure.database;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;

/**
 * Use a registered service that implements ServiceImporter to do the actual
 * import.
 */
public class ServiceInstanceImporter
   implements ServiceImporter
{
   @Structure
   ServiceFinder finder;

   ServiceImporter service;

   String serviceId;

   public Object importService( ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
   {
      serviceId = importedServiceDescriptor.metaInfo( String.class );

      return getServiceImporter().importService( importedServiceDescriptor );
   }

   public boolean isActive( Object o )
   {
      return getServiceImporter().isActive( o );
   }

   private ServiceImporter getServiceImporter()
   {
      if (service == null)
      {
         for (ServiceReference<ServiceImporter> reference : finder.<ServiceImporter>findServices( ServiceImporter.class ))
         {
            if (reference.identity().equals( serviceId ))
            {
               service = reference.get();
               break;
            }
         }
      }

      if (service == null)
         throw new ServiceImporterException("No service importer with id '"+ serviceId +"' was found");

      return service;
   }
}
