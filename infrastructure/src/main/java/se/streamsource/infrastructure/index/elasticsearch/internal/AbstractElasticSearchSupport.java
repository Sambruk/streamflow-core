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
package se.streamsource.infrastructure.index.elasticsearch.internal;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchSupport;

import java.io.IOException;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public abstract class AbstractElasticSearchSupport
        implements ElasticSearchSupport
{

    protected static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchSupport.class);

    protected static final String DEFAULT_CLUSTER_NAME = "qi4j_cluster";

    protected static final String DEFAULT_INDEX_NAME = "qi4j_index";

    protected static final String ENTITIES_TYPE = "qi4j_entities";

    protected static final int DEFAULT_INDEX_BUFFER_SIZE = 10;

    protected static final String DEFAULT_INDEX_REFRESH_INTERVAL = "-1";

    protected Client client;

    protected String index;

    protected boolean indexNonAggregatedAssociations;


    public final void activate()
            throws Exception
    {
        activateElasticSearch();

        // Wait for yellow status: the primary shard is allocated but replicas are not
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        createIndexWithSettings();

        LOGGER.info( "Index/Query connected to Elastic Search" );
    }

    private void createIndexWithSettings() throws IOException {
        if ( !client.admin().indices().prepareExists( index ).setIndices( index ).execute().actionGet().isExists() ) {
            // Create empty index
            LOGGER.info( "Will create '{}' index as it does not exists.", index );
            ImmutableSettings.Builder indexSettings = ImmutableSettings.settingsBuilder().loadFromSource( XContentFactory.jsonBuilder().
                    startObject().
                    startObject( "analysis" ).
                    startObject( "analyzer" ).
                    //
                            startObject( "default" ).
                    field( "type", "keyword" ). // Globally disable analysis, content is treated as a single keyword
                    endObject().
                    //
                            endObject().
                    endObject().
                    endObject().
                    string() );
            client.admin().indices().prepareCreate( index ).
                    setIndex( index ).
                    setSettings( indexSettings ).
                    execute().
                    actionGet();
            LOGGER.info( "Index '{}' created.", index );
        }
    }

    protected abstract void activateElasticSearch()
            throws Exception;


    public final void passivate()
            throws Exception
    {
        client.close();
        client = null;
        index = null;
        indexNonAggregatedAssociations = false;
        passivateElasticSearch();
    }

    protected void passivateElasticSearch()
            throws Exception
    {
        // NOOP
    }


    public Client client()
    {
        return client;
    }


    public String index()
    {
        return index;
    }


    public String entitiesType()
    {
        return ENTITIES_TYPE;
    }


    public boolean indexNonAggregatedAssociations()
    {
        return indexNonAggregatedAssociations;
    }

    public void emptyIndex() throws IOException {
        client.admin().indices().prepareDelete( index ).execute().actionGet();
        createIndexWithSettings();
    }

}

