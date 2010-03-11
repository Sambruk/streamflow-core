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

package se.streamsource.dci.test.context;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.dci.test.context.file.FilesContext;
import se.streamsource.dci.test.context.jmx.JmxServerContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.lang.management.ManagementFactory;

/**
 * JAVADOC
 */
@Mixins(RootContext.Mixin.class)
public interface RootContext
   extends Context
{
   @SubContext
   FilesContext files();

   @SubContext
   JmxServerContext jmx();

   abstract class Mixin
      extends ContextMixin
      implements RootContext
   {
      public FilesContext files()
      {
         return subContext( FilesContext.class );
      }

      public JmxServerContext jmx()
      {
         context.playRoles( ManagementFactory.getPlatformMBeanServer() );
         return subContext( JmxServerContext.class );
      }
   }
}
