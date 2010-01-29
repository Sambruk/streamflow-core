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
 * JAVADOC
 */
public class ServiceInstanceImporter
   implements ServiceImporter
{
   @Structure
   ServiceFinder finder;

   String serviceId;

   public Object importService( ImportedServiceDescriptor importedServiceDescriptor ) throws ServiceImporterException
   {
      serviceId = importedServiceDescriptor.metaInfo( String.class );

      for (ServiceReference<ServiceImporter> reference : finder.<ServiceImporter>findServices( ServiceImporter.class ))
      {
         if (reference.identity().equals( serviceId ))
            return reference.get().importService( importedServiceDescriptor );
      }

      throw new ServiceImporterException("No service importer with id '"+ serviceId +"' was found");
   }

   public boolean isActive( Object o )
   {
      for (ServiceReference<ServiceImporter> reference : finder.<ServiceImporter>findServices( ServiceImporter.class ))
      {
         if (reference.identity().equals(serviceId))
            return reference.get().isActive( o );
      }

      return false;
   }
}
