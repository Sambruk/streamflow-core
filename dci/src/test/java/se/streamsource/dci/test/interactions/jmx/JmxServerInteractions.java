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

package se.streamsource.dci.test.interactions.jmx;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class JmxServerInteractions
   extends ContextMixin
   implements IndexContext<LinksValue>, SubContexts<DomainInteractions>
{
   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      for (String domain : roleMap.get( MBeanServer.class ).getDomains())
      {
         builder.addLink( domain, domain );
      }

      return builder.newLinks();
   }

   public DomainInteractions context( String id ) throws ContextNotFoundException
   {
      try
      {
         roleMap.set( new ObjectName(id+":*") );

         return subContext( DomainInteractions.class );
      } catch (MalformedObjectNameException e)
      {
         throw new ContextNotFoundException();
      }
   }
}
