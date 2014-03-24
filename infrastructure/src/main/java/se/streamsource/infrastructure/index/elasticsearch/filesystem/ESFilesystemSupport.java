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
package se.streamsource.infrastructure.index.elasticsearch.filesystem;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchConfiguration;
import se.streamsource.infrastructure.index.elasticsearch.ElasticSearchSupport;
import se.streamsource.infrastructure.index.elasticsearch.internal.AbstractElasticSearchSupport;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */
public abstract class ESFilesystemSupport
        extends AbstractElasticSearchSupport
        implements ESFilesystemIndexQueryService
{

    @This
    private Configuration<ElasticSearchConfiguration> configuration;

    @This
    private Identity hasIdentity;

    @Service
    private FileConfiguration fileConfig;

    private Node node;

    @Override
    protected void activateElasticSearch()
            throws Exception
    {
        configuration.refresh();
        ElasticSearchConfiguration config = configuration.configuration();

        String clusterName = config.clusterName().get() == null ? DEFAULT_CLUSTER_NAME : config.clusterName().get();
        index = config.index().get() == null ? DEFAULT_INDEX_NAME : config.index().get();
        indexNonAggregatedAssociations = config.indexNonAggregatedAssociations().get();

        String identity = hasIdentity.identity().get();
        Settings settings = ImmutableSettings.settingsBuilder().
                put( "path.work", new File( fileConfig.temporaryDirectory(), identity ).getAbsolutePath() ).
                put( "path.logs", new File( fileConfig.logDirectory(), identity ).getAbsolutePath() ).
                put( "path.data", new File( fileConfig.dataDirectory(), identity ).getAbsolutePath() ).
                put( "path.conf", new File( fileConfig.configurationDirectory(), identity ).getAbsolutePath() ).
                put( "gateway.type", "local" ).
                put( "http.enabled", config.httpEnabled().get() ).
                put( "index.cache.type", "weak" ).
                put( "index.number_of_shards", 1 ).
                put( "index.number_of_replicas", 0 ).
                put( "index.refresh_interval", -1 ). // Controlled by ElasticSearchIndexer
                build();
        node = NodeBuilder.nodeBuilder().
                clusterName( clusterName ).
                settings( settings ).
                local( true ).
                node();
        client = node.client();
    }

    @Override
    public void passivateElasticSearch()
            throws Exception
    {
        node.close();
        node = null;
    }

    @Override
    public Client client() {
        return super.client();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String index() {
        return super.index();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String entitiesType() {
        return super.entitiesType();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean indexNonAggregatedAssociations() {
        return super.indexNonAggregatedAssociations();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
