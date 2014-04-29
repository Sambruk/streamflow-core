/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.infrastructure.index.elasticsearch.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.filesystem.ESFilesystemIndexQueryService;
import se.streamsource.infrastructure.index.elasticsearch.internal.AbstractElasticSearchAssembler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public class ESFilesystemIndexQueryAssembler
        extends AbstractElasticSearchAssembler<ESFilesystemIndexQueryAssembler>
{

    @Override
    protected void doAssemble( String identity,
                               ModuleAssembly module, Visibility visibility,
                               ModuleAssembly configModule, Visibility configVisibility )
            throws AssemblyException
    {
        module.services( ESFilesystemIndexQueryService.class ).
                identifiedBy( identity ).
                visibleIn( visibility ).
                instantiateOnStartup()
        .withConcerns(ESPerformanceLogConcern.class);

        configModule.entities( ElasticSearchConfiguration.class ).
                visibleIn( configVisibility );
    }

    public static class ESPerformanceLogConcern
            extends GenericConcern
    {
        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            long start = System.nanoTime();
            List<String> methodsOfInterest = Arrays.asList("notifyChanges", "findEntity", "findEntities", "countEntities");
            try
            {
                return next.invoke( proxy, method, args );
            } finally
            {
                long end = System.nanoTime();
                long timeMicro = (end - start) / 1000;
                double timeMilli = timeMicro / 1000.0;
                if(methodsOfInterest.contains(method.getName()))
                {
                    System.out.println("ES." + method.getName()+":"+ timeMilli );
                }
            }
        }
    }

}

