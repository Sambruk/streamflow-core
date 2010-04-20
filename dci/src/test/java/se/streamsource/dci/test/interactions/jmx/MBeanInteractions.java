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

package se.streamsource.dci.test.interactions.jmx;

import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksBuilder;
import se.streamsource.dci.value.LinksValue;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class MBeanInteractions
   extends InteractionsMixin
      implements IndexInteraction<LinksValue>, SubContexts<MBeanAttributeInteractions>
{
   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      MBeanAttributeInfo[] attributes = context.get(MBeanInfo.class).getAttributes();
      for (MBeanAttributeInfo attribute : attributes)
      {
         try
         {
            Object value = context.get( MBeanServer.class ).getAttribute( context.get( ObjectName.class), attribute.getName() );
            builder.addLink( attribute.getDescription()+(value instanceof String ? "="+ value.toString():""), attribute.getName());
         } catch (Exception e)
         {
            e.printStackTrace(  );
         }
      }

      return builder.newLinks();
   }

   public MBeanAttributeInteractions context( String id ) throws ContextNotFoundException
   {
      for (MBeanAttributeInfo mBeanAttributeInfo : context.get( MBeanInfo.class ).getAttributes())
      {
         if (mBeanAttributeInfo.getName().equals(id))
            context.set( mBeanAttributeInfo );
      }

      return subContext( MBeanAttributeInteractions.class );
   }
}
