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

package se.streamsource.dci.test.context.jmx;

import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.ContextNotFoundException;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Set;

/**
 * JAVADOC
 */
public class DomainContext
      extends ContextMixin
      implements SubContexts<MBeanContext>
{
   public LinksValue index()
   {
      Set<ObjectName> mbeans = context.role( MBeanServer.class ).queryNames( context.role( ObjectName.class ), null );

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (ObjectName mbean : mbeans)
      {
         builder.addLink( mbean.getCanonicalKeyPropertyListString(), mbean.getCanonicalKeyPropertyListString() );
      }

      return builder.newLinks();
   }

   public MBeanContext context( String id ) throws ContextNotFoundException
   {
      try
      {
         ObjectName mbeanName = new ObjectName( context.role( ObjectName.class ).getDomain() + ":" + id );

         context.playRoles( mbeanName );
         context.playRoles( context.role( MBeanServer.class ).getMBeanInfo( mbeanName ) );

         return subContext( MBeanContext.class );
      } catch (Exception e)
      {
         throw new ContextNotFoundException();
      }
   }
}
