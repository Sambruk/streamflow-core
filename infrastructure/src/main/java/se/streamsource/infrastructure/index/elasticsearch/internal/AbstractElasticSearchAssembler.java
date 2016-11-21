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
package se.streamsource.infrastructure.index.elasticsearch.internal;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public abstract class AbstractElasticSearchAssembler<AssemblerType extends AbstractElasticSearchAssembler>
        implements Assembler
{

    private String identity;

    private Visibility visibility = Visibility.module;

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.module;

    public final AssemblerType withIdentity( String identity )
    {
        this.identity = identity;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return ( AssemblerType ) this;
    }


    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        if ( identity == null ) {
            identity = "es-indexing";
        }
        if ( configModule == null ) {
            configModule = module;
        }
        doAssemble( identity, module, visibility, configModule, configVisibility );
    }

    protected abstract void doAssemble( String identity,
                                        ModuleAssembly module, Visibility visibility,
                                        ModuleAssembly configModule, Visibility configVisibility )
            throws AssemblyException;

}
