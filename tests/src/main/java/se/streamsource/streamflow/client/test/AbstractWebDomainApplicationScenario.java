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

import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.parser.CasePreservingResolver;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.PatternScenarioParser;
import org.jbehave.scenario.steps.CandidateSteps;

/**
 * Base class for web domain scenarios.
 * <p/>
 * Before scenario starts it starts the StreamFlow web server,
 * with all steps being registered as objects in the Domain layer. Then
 * all steps are executed. After the scenario the application is passivated,
 * and the web server is stopped.
 */
public class AbstractWebDomainApplicationScenario
        extends JUnitScenario
{
    public AbstractWebDomainApplicationScenario(final ClassLoader classLoader, CandidateSteps... steps)
    {
        super(new PropertyBasedConfiguration()
        {
            @Override
            public ClasspathScenarioDefiner forDefiningScenarios()
            {
                return new ClasspathScenarioDefiner(
                        new CasePreservingResolver(".scenario"),
                        new PatternScenarioParser(this),
                        classLoader);
            }
        }, new BeforeAndAfterWebDomainApplicationSteps(steps));
    }
}