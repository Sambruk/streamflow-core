/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import se.streamsource.streamflow.client.StreamFlowClientAssembler;

/**
 * JAVADOC
 */
public class StreamFlowClientTestAssembler
        extends StreamFlowClientAssembler
{
    private Class[] testClass;

    public StreamFlowClientTestAssembler(Class[] testClass)
    {
        this.testClass = testClass;
    }

    @Override
    protected void assembleUILayer(LayerAssembly uiLayer) throws AssemblyException
    {
        uiLayer.newModuleAssembly("Test").addObjects(testClass);
    }
}
