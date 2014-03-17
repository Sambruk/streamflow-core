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

import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.JSONException;
import org.qi4j.api.Qi4j;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * ES query parser implementation
 */
public class ElasticSearchQueryParserImpl
    implements ElasticSearchQueryParser
{
    @Structure
    Qi4jSPI qi4jSPI;

    private Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    private static ThreadLocal<DateFormat> ISO8601_UTC = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
            dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            return dateFormat;
        }
    };

    public QueryBuilder getQueryBuilder(AndFilterBuilder baseFilter, BooleanExpression whereClause)
    {
        if( whereClause == null )
        {
            return matchAllQuery();
        } else {
            processFilter( baseFilter, whereClause );
            return matchAllQuery();
        }
    }

    private void processFilter( FilterBuilder filterBuilder, final BooleanExpression expression )
    {
        if( expression instanceof Conjunction )
        {
            final Conjunction conjunction = (Conjunction) expression;
            AndFilterBuilder andFilterBuilder = new AndFilterBuilder();

            processFilter(andFilterBuilder, conjunction.leftSideExpression());
            processFilter( andFilterBuilder, conjunction.rightSideExpression() );
            addFilter(andFilterBuilder, filterBuilder);

        }
        if( expression instanceof Disjunction )
        {
            final Disjunction disjunction = (Disjunction) expression;
            OrFilterBuilder orFilterBuilder = new OrFilterBuilder();
            
            processFilter(orFilterBuilder, disjunction.leftSideExpression());
            processFilter(orFilterBuilder, disjunction.rightSideExpression());
            addFilter(orFilterBuilder, filterBuilder);
        }
        if( expression instanceof Negation )
        {
            processNegation(filterBuilder, ((Negation) expression).expression()) ;
        }
        if( expression instanceof MatchesPredicate )
        {
            processMatchesPredicate(filterBuilder, (MatchesPredicate) expression);
        }
        if( expression instanceof ComparisonPredicate )
        {
            processComparisonPredicate(filterBuilder, (ComparisonPredicate) expression);
        }
        if( expression instanceof ManyAssociationContainsPredicate )
        {
            processManyAssociationContainsPredicate(filterBuilder, (ManyAssociationContainsPredicate) expression);
        }
        if( expression instanceof PropertyNullPredicate )
        {
            processNullPredicate(filterBuilder, (PropertyNullPredicate) expression);
        }
        if( expression instanceof AssociationNullPredicate )
        {
            processNullPredicate(filterBuilder, (AssociationNullPredicate) expression);
        }
        if( expression instanceof ContainsPredicate<?, ?> )
        {
            processContainsPredicate( filterBuilder, (ContainsPredicate<?, ?>) expression );
        }
        if( expression instanceof ContainsAllPredicate<?, ?> )
        {
            processContainsAllPredicate( filterBuilder, (ContainsAllPredicate<?, ?>) expression );
        }
        throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
    }

    private void processNegation(FilterBuilder filterBuilder, BooleanExpression expression )
    {
        if( expression instanceof PropertyIsNotNullPredicate)
        {
            LOGGER.trace( "Processing PropertyNotNullSpecification {}", expression );
            addFilter( existsFilter( ((PropertyIsNotNullPredicate)expression).propertyReference().propertyName() ), filterBuilder );
        } else if ( expression instanceof AssociationIsNotNullPredicate )
        {
            LOGGER.trace( "Processing AssociationNotNullSpecification {}", expression );
            addFilter( existsFilter( ((AssociationIsNotNullPredicate)expression).associationReference().associationName() + ".identity" ), filterBuilder );
        } else {
            LOGGER.trace( "Processing NotSpecification {}", expression );
            AndFilterBuilder operandFilter = new AndFilterBuilder();
            processFilter(operandFilter, expression);
            addFilter( notFilter( operandFilter ), filterBuilder );
        }
    }

    private void processContainsAllPredicate( FilterBuilder filterBuilder, final ContainsAllPredicate<?, ?> predicate )
    {
        LOGGER.trace( "Processing ContainsAllSpecification {}", predicate );
        String name = predicate.propertyReference().propertyName();
        AndFilterBuilder contAllFilter = new AndFilterBuilder();
        for ( Object value : (Collection<?>)((SingleValueExpression)predicate.valueExpression()).value() ) {
            if ( value instanceof ValueComposite ) {

                // Query by complex property "example value"
                ValueComposite valueComposite = ( ValueComposite ) value;
                ValueDescriptor valueDescriptor = qi4jSPI.getValueDescriptor( valueComposite );
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                        + "queries, ie. queries by 'example value'." );

            } else {

                contAllFilter.add( termFilter( name, toString( value ) ) );

            }
        }
        addFilter( contAllFilter, filterBuilder );
    }

    private void processContainsPredicate( FilterBuilder filterBuilder, final ContainsPredicate<?, ?> predicate )
    {
        LOGGER.trace( "Processing ContainsSpecification {}", predicate );
        String name = predicate.propertyReference().propertyName();

        if ( ((SingleValueExpression)predicate.valueExpression()).value() instanceof ValueComposite) {

            // Query by complex property "example value"
            ValueComposite value = ( ValueComposite )((SingleValueExpression)predicate.valueExpression()).value();
            ValueDescriptor valueDescriptor = qi4jSPI.getValueDescriptor( value );
            throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                    + "queries, ie. queries by 'example value'." );

        } else {

            String value = toString( ((SingleValueExpression)predicate.valueExpression()).value() );
            addFilter( termFilter( name, value ), filterBuilder );

        }
    }

    private void processMatchesPredicate( FilterBuilder filterBuilder, final MatchesPredicate predicate )
    {
        LOGGER.trace( "Processing MatchesSpecification {}", predicate );
        // https://github.com/elasticsearch/elasticsearch/issues/988
        // http://elasticsearch-users.115913.n3.nabble.com/Regex-Query-td3301347.html
        throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search: "
                + predicate.getClass() + ": " + predicate );
    }

    private void processComparisonPredicate( FilterBuilder filterBuilder, final ComparisonPredicate predicate )
    {
        LOGGER.trace( "Processing ComparisonPredicate {}", predicate );
        String name = predicate.propertyReference().propertyName();
        String value = toString( ((SingleValueExpression)predicate.valueExpression()).value() );

        if ( predicate instanceof GreaterOrEqualPredicate ) {

            addFilter( rangeFilter( name ).from( value ).includeLower( true ), filterBuilder );

        } else if ( predicate instanceof GreaterThanPredicate ) {

            addFilter( rangeFilter( name ).from( value ).includeLower( false ), filterBuilder );

        } else if ( predicate instanceof LessOrEqualPredicate ) {

            addFilter( rangeFilter( name ).to( value ).includeUpper( true ), filterBuilder );

        } else if ( predicate instanceof LessThanPredicate ) {

            addFilter( rangeFilter( name ).to( value ).includeUpper( false ), filterBuilder );

        } else {

            throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search: "
                    + predicate.getClass() + ": " + predicate );

        }
    }

    private void processManyAssociationContainsPredicate( FilterBuilder filterBuilder, ManyAssociationContainsPredicate predicate)
    {
        LOGGER.trace( "Processing ManyAssociationContainsSpecification {}", predicate );
        String name = predicate.associationReference().associationName() + ".identity";
        String value = toString( ((SingleValueExpression)predicate.valueExpression()).value() );
        addFilter( termFilter( name, value ), filterBuilder );
    }

    private void processNullPredicate( FilterBuilder filterBuilder, final PropertyNullPredicate predicate )
    {
        LOGGER.trace( "Processing PropertyNullSpecification {}", predicate );
        addFilter( missingFilter( predicate.propertyReference().propertyName() ), filterBuilder );
    }

    private void processNullPredicate( FilterBuilder filterBuilder, final AssociationNullPredicate predicate )
    {
        LOGGER.trace( "Processing AssociationNullSpecification {}", predicate );
        addFilter( missingFilter( predicate.associationReference().associationName() + ".identity" ), filterBuilder );
    }

    private void addFilter( FilterBuilder filter, FilterBuilder into )
    {
        if ( into instanceof AndFilterBuilder ) {
            ( ( AndFilterBuilder ) into ).add( filter );
        } else if ( into instanceof OrFilterBuilder) {
            ( ( OrFilterBuilder ) into ).add( filter );
        } else {
            throw new UnsupportedOperationException( "FilterBuilder is nor an AndFB nor an OrFB, cannot continue." );
        }
    }

    private String toString( Object value )
    {
        if( value == null )
        {
            return null;
        }

        if( value instanceof Date )
        {
            return ISO8601_UTC.get().format( (Date) value );
        }
        else if( value instanceof Entity)
        {
            return "urn:qi4j:entity:" + value.toString();
        }
        else
        {
            return value.toString();
        }
    }
}
