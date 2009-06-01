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
import org.qi4j.bootstrap.ModuleAssembly;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.StreamFlowApplication;
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
        super.assembleUILayer(uiLayer);

        ModuleAssembly moduleAssembly = uiLayer.newModuleAssembly("Test");
        moduleAssembly.addObjects(testClass);
        moduleAssembly.importServices(Restlet.class);
        uiLayer.applicationAssembly().setMetaInfo(new Restlet());
        StreamFlowApplication application = new StreamFlowApplication();
        uiLayer.applicationAssembly().setMetaInfo(application);
        uiLayer.applicationAssembly().setMetaInfo(application.getContext());
        uiLayer.applicationAssembly().setMetaInfo(application.getMainFrame().getRootPane().getActionMap());
    }
}
