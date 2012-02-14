/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.dci.value;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.dci.value.link.TitledLinksValue;
import se.streamsource.dci.value.table.CellValue;
import se.streamsource.dci.value.table.ColumnValue;
import se.streamsource.dci.value.table.RowValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableResponseValue;
import se.streamsource.dci.value.table.TableValue;

/**
 * Assembler all DCI values.
 */
public class ValueAssembler
   implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.values( ResourceValue.class,
            EntityValue.class,
            StringValue.class,
            FormValue.class,

            LinksValue.class,
            LinkValue.class,
            TitledLinkValue.class,
            TitledLinksValue.class,

            TableResponseValue.class,
            TableValue.class,
            ColumnValue.class,
            RowValue.class,
            CellValue.class,
            TableQuery.class ).visibleIn( Visibility.application );
   }
}
