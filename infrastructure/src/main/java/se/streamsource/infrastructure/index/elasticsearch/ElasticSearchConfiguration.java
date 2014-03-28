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
package se.streamsource.infrastructure.index.elasticsearch;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public interface ElasticSearchConfiguration
        extends ConfigurationComposite
{

    /**
     * Cluster name.
     * Defaults to 'qi4j_cluster'.
     */
    @Optional
    Property<String> clusterName();

    /**
     * Index name.
     * Defaults to 'qi4j_index'.
     */
    @Optional Property<String> index();

    /**
     * Set to true to index non aggregated associations as if they were aggregated.
     * WARN: Don't use this if your domain model contains circular dependencies.
     * Defaults to 'FALSE'.
     */
    @UseDefaults
    Property<Boolean> indexNonAggregatedAssociations();


    /**
     * Set to true if ES should open the http port 9200 to be able to post queries
     * in json form directly to ES.
     * i.e.  curl -X GET "http://localhost:9200/qi4j_index/_search?from=0&load=true&size=10&pretty=true" -d '{<json/query/filter>}'
     * @return
     */
    @UseDefaults
    Property<Boolean> httpEnabled();

    /**
     * The index buffer size in percent of the jvm heap.
     * Defaults to 10%
     * @return
     */
    @Optional
    Property<Integer> indexBufferSizePercent();
}

