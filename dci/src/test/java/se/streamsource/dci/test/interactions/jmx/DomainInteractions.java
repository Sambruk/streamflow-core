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

import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Set;

/**
 * JAVADOC
 */
public class DomainInteractions
      extends ContextMixin
      implements SubContexts<MBeanInteractions>
{
   public LinksValue index()
   {
      Set<ObjectName> mbeans = roleMap.get( MBeanServer.class ).queryNames( roleMap.get( ObjectName.class ), null );

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (ObjectName mbean : mbeans)
      {
         builder.addLink( mbean.getCanonicalKeyPropertyListString(), mbean.getCanonicalKeyPropertyListString() );
      }

      return builder.newLinks();
   }

   public MBeanInteractions context( String id ) throws ContextNotFoundException
   {
      try
      {
         ObjectName mbeanName = new ObjectName( roleMap.get( ObjectName.class ).getDomain() + ":" + id );

         roleMap.set( mbeanName );
         roleMap.set( roleMap.get( MBeanServer.class ).getMBeanInfo( mbeanName ) );

         return subContext( MBeanInteractions.class );
      } catch (Exception e)
      {
         throw new ContextNotFoundException();
      }
   }
}
