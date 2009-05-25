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

package se.streamsource.streamflow.client.application.shared;

import se.streamsource.streamflow.client.application.shared.steps.GroupSteps;
import se.streamsource.streamflow.client.application.shared.steps.LoginSteps;
import se.streamsource.streamflow.client.application.shared.steps.UserSteps;
import se.streamsource.streamflow.client.application.shared.steps.OrganizationalUnitSteps;
import se.streamsource.streamflow.client.test.AbstractClientApplicationScenario;

/**
 * JAVADOC
 */
public class NewGroup
        extends AbstractClientApplicationScenario
{
    public NewGroup()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public NewGroup(ClassLoader classLoader)
    {
        super(classLoader, new LoginSteps(),
                new UserSteps(),
                new OrganizationalUnitSteps(),
                new GroupSteps());
    }
}
