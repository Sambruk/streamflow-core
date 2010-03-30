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

package se.streamsource.dci.test.context.jmx;

import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.ContextNotFoundException;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class JmxServerContext
   extends ContextMixin
   implements IndexContext<LinksValue>, SubContexts<DomainContext>
{
   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      for (String domain : context.role( MBeanServer.class ).getDomains())
      {
         builder.addLink( domain, domain );
      }

      return builder.newLinks();
   }

   public DomainContext context( String id ) throws ContextNotFoundException
   {
      try
      {
         context.playRoles( new ObjectName(id+":*") );

         return subContext( DomainContext.class );
      } catch (MalformedObjectNameException e)
      {
         throw new ContextNotFoundException();
      }
   }
}
