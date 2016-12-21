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
package se.streamsource.streamflow.client;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.envisage.Envisage;
import org.qi4j.spi.structure.ApplicationModelSPI;
import se.streamsource.streamflow.client.assembler.StreamflowClientAssembler;

/**
 * Run this to start Envisage to visualize the Streamflow client application.
 */
public class Visualize
{
   public static void main( String[] args ) throws AssemblyException
   {
        Energy4Java is = new Energy4Java();
        ApplicationModelSPI app = is.newApplicationModel(new StreamflowClientAssembler());
        new Envisage().run(app);
   }
}
