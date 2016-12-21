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
package se.streamsource.infrastructure.index.elasticsearch;

import org.elasticsearch.client.Client;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.service.Activatable;

import java.io.IOException;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public interface ElasticSearchSupport
        extends Activatable
{

    Client client();

    String index();

    String entitiesType();

    boolean indexNonAggregatedAssociations();

    void emptyIndex() throws IOException;

}
