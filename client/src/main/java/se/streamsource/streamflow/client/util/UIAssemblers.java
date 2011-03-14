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

package se.streamsource.streamflow.client.util;

import org.jdesktop.application.Task;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public final class UIAssemblers
{
   public static void addMV( ModuleAssembly module, Class modelClass, Class<? extends JComponent> viewClass ) throws AssemblyException
   {
      addModels( module, modelClass );
      addViews( module, viewClass );
   }

   public static void addModels( ModuleAssembly module, Class... modelClasses ) throws AssemblyException
   {
      module.objects( modelClasses ).visibleIn( Visibility.layer );
   }

   public static void addViews( ModuleAssembly module, Class<? extends JComponent>... viewClasses ) throws AssemblyException
   {
      module.objects( viewClasses ).visibleIn( Visibility.layer );
   }

   public static void addDialogs( ModuleAssembly module, Class<? extends JComponent>... dialogClasses ) throws AssemblyException
   {
      module.objects( dialogClasses ).visibleIn( Visibility.layer );
   }

   public static void addTasks( ModuleAssembly module, Class<? extends Task>... taskClasses ) throws AssemblyException
   {
      module.objects( taskClasses );
   }
}
