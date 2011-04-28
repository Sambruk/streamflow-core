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

package se.streamsource.dci.test.interactions.jmx;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

/**
 * JAVADOC
 */
public class MBeanResource
      extends CommandQueryResource
      implements SubResources
{
   public MBeanResource()
   {
      super( MBeanContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      for (MBeanAttributeInfo mBeanAttributeInfo : RoleMap.role( MBeanInfo.class ).getAttributes())
      {
         if (mBeanAttributeInfo.getName().equals( segment ))
            RoleMap.current().set( mBeanAttributeInfo );
      }

      subResourceContexts( MBeanAttributeContext.class );
   }
}
