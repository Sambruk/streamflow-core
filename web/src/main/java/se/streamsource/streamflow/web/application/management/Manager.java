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

package se.streamsource.streamflow.web.application.management;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.Immutable;

import java.io.IOException;

/**
 * Management methods for StreamFlow. These operations are available
 * through JMX.
 */
public interface Manager
{
    public void reindex();

    public String exportDatabase(@Name("Compress") boolean compress) throws IOException;

    public String importDatabase(@Name("Filename") String name) throws IOException;

    public String exportEvents(@Name("Compress") boolean compress) throws IOException;

    public String startCustomJmxConnector(@Name("Port") String port);

    public String stopCustomJmxConnector();

    @Computed
    Property<Integer> failedLogins();

    @Immutable
    Property<String> version();

}
