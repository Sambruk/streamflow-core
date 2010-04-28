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

package se.streamsource.streamflow.client.ui.administration.resolutions;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.administration.label.GroupedSelectionDialog;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;

/**
 * JAVADOC
 */
public class ResolutionsAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addMV( module, ResolutionsModel.class, ResolutionsView.class );
      UIAssemblers.addMV( module, SelectedResolutionsModel.class, SelectedResolutionsView.class );
   }
}