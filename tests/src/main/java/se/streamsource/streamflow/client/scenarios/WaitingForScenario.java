/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.scenarios;

import se.streamsource.streamflow.client.application.shared.steps.*;
import se.streamsource.streamflow.client.application.shared.steps.setup.TestSetupSteps;
import se.streamsource.streamflow.client.test.AbstractWebDomainApplicationScenario;

/**
 * JAVADOC
 */
public class WaitingForScenario
        extends AbstractWebDomainApplicationScenario
{

    public WaitingForScenario()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public WaitingForScenario(ClassLoader classLoader)
    {
        super(classLoader, new TestSetupSteps(),
                new FieldDefinitionsSteps(),
                new FormTemplateSteps(),
                new FormTemplatesSteps(),
                new FormsSteps(),
                new OrganizationsSteps(),
                new OrganizationalUnitsSteps(),
                new MembersSteps(),
                new ProjectsSteps(),
                new ParticipantsSteps(),
                new GroupsSteps(),
                new UserSteps(),
                new WaitingForSteps()
        );
    }
}