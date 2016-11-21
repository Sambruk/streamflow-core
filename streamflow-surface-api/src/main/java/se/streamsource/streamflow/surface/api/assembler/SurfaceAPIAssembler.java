/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.surface.api.assembler;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.surface.api.AttachmentFieldDTO;
import se.streamsource.streamflow.surface.api.AttachmentFieldSubmission;
import se.streamsource.streamflow.surface.api.CaseFormDTO;
import se.streamsource.streamflow.surface.api.CaseListItemDTO;
import se.streamsource.streamflow.surface.api.ClosedCaseDTO;
import se.streamsource.streamflow.surface.api.EndUserCaseDTO;
import se.streamsource.streamflow.surface.api.FormSignatureDTO;
import se.streamsource.streamflow.surface.api.OpenCaseDTO;

/**
 * TODO
 */
public class SurfaceAPIAssembler
   implements Assembler
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      module.values(
              CaseFormDTO.class,
              EndUserCaseDTO.class,
              ClosedCaseDTO.class,
              OpenCaseDTO.class,
              AttachmentFieldDTO.class,
              AttachmentFieldSubmission.class,
              FormSignatureDTO.class,
              CaseListItemDTO.class);
   }
}
