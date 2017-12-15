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

import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Back ported from Qi4j 2.0
 *
 * courtesy of Paul Merlin
 */

@Mixins( {ElasticSearchFinder.Mixin.class, ElasticSearchFinder.NamedEntityFinderMixin.class} )
public interface ElasticSearchFinder
        extends EntityFinder, NamedEntityFinder
{

    class Mixin
            implements EntityFinder
    {

        private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchFinder.class);

        private static final int REQUEST_SIZE_THRESHOLD = 1000;
        private static final long SCROLL_KEEP_ALIVE = TimeUnit.MINUTES.toMillis(1);

        @This
        private ElasticSearchSupport support;

        @Structure
        Qi4jSPI qi4j;

        public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                       BooleanExpression whereClause,
                                                       OrderBy[] orderBySegments,
                                                       Integer firstResult, Integer maxResults )
                throws EntityFinderException
        {
            final int size = maxResults == null ? Integer.MAX_VALUE : maxResults;

            // Prepare request
            final Client client = support.client();
            SearchRequestBuilder request = client.prepareSearch( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            //QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause );

            QueryBuilder queryBuilder = new ElasticSearchQueryParserImpl().getQueryBuilder( filterBuilder, whereClause );

            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );
            if ( firstResult != null ) {
                request.setFrom( firstResult );
            }

            boolean useScroll = size > REQUEST_SIZE_THRESHOLD;

            if (useScroll)
            {
                request.setSize( REQUEST_SIZE_THRESHOLD )
                    .setScroll( new TimeValue( SCROLL_KEEP_ALIVE ) );
            } else
            {
                request.setSize( size );
            }

            if ( orderBySegments != null ) {
                for ( OrderBy order : orderBySegments ) {
                    FieldSortBuilder sortBuilder = new FieldSortBuilder(order.propertyReference().propertyName());
                    sortBuilder.order( order.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC );
                    sortBuilder.ignoreUnmapped( true );
                    sortBuilder.missing( "_first" );
                    request.addSort( sortBuilder );
                }
            }

            // Log
            LOGGER.debug( "Will search Entities: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();
            SearchHits hits = response.getHits();
            final List<SearchHit> result = new ArrayList<>(Arrays.asList(hits.getHits()));
            if (useScroll) {
                do
                {
                    response = client
                            .prepareSearchScroll( response.getScrollId() )
                            .setScroll( new TimeValue( SCROLL_KEEP_ALIVE ) )
                            .execute()
                            .actionGet();

                    hits = response.getHits();

                    result.addAll(Arrays.asList(hits.getHits()));
                } while (hits.totalHits() != 0);
            }

            return Iterables.map(new Function<SearchHit, EntityReference>() {

                public EntityReference map(SearchHit from) {
                    return EntityReference.parseEntityReference(from.id());
                }

            }, result);
        }


        public EntityReference findEntity( Class<?> resultType,
                                           BooleanExpression whereClause )
                throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            //QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause );

            QueryBuilder queryBuilder = new ElasticSearchQueryParserImpl().getQueryBuilder( filterBuilder, whereClause );

            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );
            request.setSize( 1 );

            // Log
            LOGGER.debug( "Will search Entity: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            if ( response.getHits().totalHits() == 1 ) {
                return EntityReference.parseEntityReference( response.getHits().getAt( 0 ).id() );
            }

            return null;
        }


        public long countEntities( Class<?> resultType,
                                   BooleanExpression whereClause )
                throws EntityFinderException
        {
            // Prepare request
            CountRequestBuilder request = support.client().prepareCount( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            //QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause );

            QueryBuilder queryBuilder = new ElasticSearchQueryParserImpl().getQueryBuilder( filterBuilder, whereClause );

            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );

            // Log
            LOGGER.debug( "Will count Entities: {}", request );

            // Execute
            CountResponse count = request.execute().actionGet();

            return count.getCount();
        }

        private static AndFilterBuilder baseFilters( Class<?> resultType )
        {
            return andFilter( termFilter("_types", resultType.getName()) );
        }
    }

    class NamedEntityFinderMixin implements NamedEntityFinder
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchFinder.class);

        @This
        private ElasticSearchSupport support;

        public Iterable<EntityReference> findEntities(NamedQueryDescriptor descriptor,
                                                      String resultType,
                                                      @Optional Map<String, Object> variables,
                                                      @Optional OrderBy[] orderBySegments,
                                                      @Optional Integer firstResult,
                                                      @Optional Integer maxResults) throws EntityFinderException
        {
            return null;
        }

        public EntityReference findEntity(NamedQueryDescriptor descriptor,
                                          String resultType,
                                          Map<String, Object> variables) throws EntityFinderException
        {
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            request.setQuery( descriptor.compose( variables,null,null,null ) );
            request.setSize( 1 );

            // Log
            LOGGER.debug( "Will search Entity: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            if ( response.getHits().totalHits() == 1 ) {
                return EntityReference.parseEntityReference( response.getHits().getAt( 0 ).id() );
            }

            return null;
        }

        public long countEntities(NamedQueryDescriptor descriptor, String resultType, @Optional Map<String, Object> variables) throws EntityFinderException {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String showQuery(NamedQueryDescriptor descriptor) {
            return descriptor.compose(null,null,null,null);
        }
    }
}
