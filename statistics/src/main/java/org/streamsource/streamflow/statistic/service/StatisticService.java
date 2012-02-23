/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package org.streamsource.streamflow.statistic.service;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.streamsource.streamflow.statistic.dto.CaseCount;
import org.streamsource.streamflow.statistic.dto.CaseCountTotal;
import org.streamsource.streamflow.statistic.dto.CaseTypeValue;
import org.streamsource.streamflow.statistic.dto.ScatterChartValue;
import org.streamsource.streamflow.statistic.dto.SearchCriteria;
import org.streamsource.streamflow.statistic.dto.StatisticsResult;
import org.streamsource.streamflow.statistic.dto.TopOu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/22/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticService
{
   SearchCriteria criteria;
   String periodPattern;

   protected JdbcTemplate jdbcTemplate;

   protected String organizationQuery = "select id, name, organization.left, organization.right from organization where organization.left = 0";
   protected String topOusQuery = "select id, name, organization.left, organization.right from organization where parent in (select id from organization where organization.left = 0)";

   
   public StatisticService(SearchCriteria criteria )
   {
      this.criteria = criteria;

      Preferences preference = Preferences.userRoot().node( "/streamsource/streamflow/StreamflowServer/streamflowds/properties" );
      String dbUrl = preference.get( "url", "n/a" );
      String dbUser = preference.get( "username", "n/a" );
      String dbPwd = preference.get( "password", "n/a" );
      String dbDriver = preference.get( "driver", "n/a" );

      BasicDataSource ds = new BasicDataSource();
      ds.setDriverClassName( dbDriver );
      ds.setUrl( dbUrl );
      ds.setUsername( dbUser );
      ds.setPassword( dbPwd );

      jdbcTemplate = new JdbcTemplate( ds );
   }

   protected String getCaseOrgTotalQuery( )
   {
      return "select date_format( closed_on, '" + periodPattern +"' ) as period, count(case_id) as number " +
            "from casesdescriptions " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "group by period " +
            "order by period";
   }

   protected String getCaseOrgWithCaseTypeQuery( )
   {
      return "select date_format( closed_on, '" + periodPattern + "' ) as period, count(case_id) as number " +
            "from casesdescriptions " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype is not null " +
            "group by period " +
            "order by period";
   }

   protected String getCaseOrgWithoutCaseTypeQuery( )
   {
      return "select date_format( closed_on, '" + periodPattern + "' ) as period, count(case_id) as number " +
            "from casesdescriptions " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype is null " +
            "group by period " +
            "order by period";
   }

   protected String getTopOuCasesQuery( )
   {
      return "select date_format( cases.closed_on, '" + periodPattern + "') as period, count(case_id) as number " +
            "from cases where cases.casetype_owner in " +
            "( " +
            "select id from organization " +
            "where organization.left >= ? " +
            " and organization.right <= ? " +
            ") " +
            "and closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype_owner is not null " +
            "group by period " +
            "order by period";
   }

   protected String getCasetypeOwnerQuery( )
   {
      return "select casetype_owner, date_format(closed_on, '" + periodPattern + "') as period, count(case_id) as number " +
            "from casesdescriptions " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype_owner is not null " +
            "group by casetype_owner, period " +
            "order by casetype_owner, period";
   }

   protected String getCasetypeQuery( )
   {
      return "select casetype, date_format(closed_on, '" + periodPattern + "') as period, count(case_id) as number " +
            "from casesdescriptions " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype is not null " +
            "group by casetype, period " +
            "order by casetype, period ";
   }

   public StatisticsResult getStatistics()
   {
      StatisticsResult result = new StatisticsResult();

      // Organization information
      final TopOu org = jdbcTemplate.query( organizationQuery, new ResultSetExtractor<TopOu>()
      {

         public TopOu extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            TopOu result = null;
            while( rs.next() )
            {
               result = new TopOu( rs.getString( 1 ), rs.getString( 2 ), rs.getInt( 3 ), rs.getInt( 4 ) );
            }
            return result; 
         }
      });


      // Compile summary
      List<CaseCount> summaryTotal = new ArrayList<CaseCount>(  );

      CaseCount orgTotalCaseCount = jdbcTemplate.query( getCaseOrgTotalQuery(), new Object[]{criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<CaseCount>(){

         public CaseCount extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( org.getName() + " total", criteria.getPeriods() );
            while( rs.next() )
            {
               total.addCount( rs.getString( 1 ), rs.getInt( 2 ) );
            }
            return total;
         }
      });

      summaryTotal.add( orgTotalCaseCount );

      CaseCount orgTotalCaseCountWithCaseType = jdbcTemplate.query( getCaseOrgWithCaseTypeQuery(), new Object[]{criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<CaseCount>(){

         public CaseCount extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( "Ärenden med ärendetyp", criteria.getPeriods() );
            while( rs.next() )
            {
               total.addCount( rs.getString( 1 ), rs.getInt( 2 ) );
            }
            return total;
         }
      });

      summaryTotal.add( orgTotalCaseCountWithCaseType );

      CaseCount orgTotalCaseCountWithoutCaseType = jdbcTemplate.query( getCaseOrgWithoutCaseTypeQuery(), new Object[]{criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<CaseCount>(){

         public CaseCount extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( "Ärenden utan ärendetyp ( ingen tillhörighet )", criteria.getPeriods() );
            while( rs.next() )
            {
               total.addCount( rs.getString( 1 ), rs.getInt( 2 ) );
            }
            return total;
         }
      });

      summaryTotal.add( orgTotalCaseCountWithoutCaseType );

      List<TopOu> topOuIds = jdbcTemplate.query( topOusQuery, new ResultSetExtractor<List<TopOu>>()
      {
         public List<TopOu> extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            List<TopOu> result = new ArrayList<TopOu>(  );
            while( rs.next() )
            {
               TopOu current = new TopOu( rs.getString(1), rs.getString( 2 ), rs.getInt( 3 ), rs.getInt( 4 ) );
               result.add( current );
            }
            return result;
         }
      } );


      // Compile list of case counts where case type owner belongs to top OU's
      List<CaseCount> caseCountPerTopOu = new ArrayList<CaseCount>(  );

      final CaseCountTotal caseCountPerTopOuTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );
      for( TopOu topOu : topOuIds )
      {
         final TopOu currentTopOu = topOu;
         CaseCount caseCountForTopOu = jdbcTemplate.query( getTopOuCasesQuery(), new Object[]{currentTopOu.getLeft(), currentTopOu.getRight(), criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<CaseCount>()
         {
            public CaseCount extractData( ResultSet rs ) throws SQLException, DataAccessException
            {
               CaseCount caseCount = new CaseCount( currentTopOu.getName(), criteria.getPeriods() );
               while (rs.next()){

                  caseCount.addCount( rs.getString( 1 ), rs.getInt( 2 ) );
                  caseCountPerTopOuTotal.plus( rs.getString( 1 ), rs.getInt( 2 ) );

               }
               return caseCount;
            }
         });
         caseCountPerTopOu.add( caseCountForTopOu );
      }
      Collections.sort( caseCountPerTopOu );
      caseCountPerTopOu.add(  caseCountPerTopOuTotal );

      List<CaseCount> caseCountByCaseTypeOwner = jdbcTemplate.query( getCasetypeOwnerQuery(), new Object[]{criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<List<CaseCount>>()
      {
         Map<String, CaseCount> caseCounts = new HashMap<String, CaseCount>();
         CaseCountTotal caseCountTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );

         public List<CaseCount> extractData( ResultSet rs ) throws SQLException, DataAccessException
         {

            while (rs.next()){
               String description = rs.getString( 1 );
               CaseCount caseCount = caseCounts.get( description );
               if (caseCount == null) {
                  caseCount = new CaseCount( description, criteria.getPeriods() );
                  caseCounts.put( description, caseCount );
               }
               caseCount.addCount( rs.getString( 2 ), rs.getInt( 3 ) );
               caseCountTotal.plus( rs.getString( 2 ), rs.getInt( 3 ) );
            }

            ArrayList<CaseCount> result = new ArrayList<CaseCount>( caseCounts.values() );
            Collections.sort( result );
            result.add( caseCountTotal );
            return result;
         }
      });

      List<CaseCount> caseCountByCaseType = jdbcTemplate.query( getCasetypeQuery(), new Object[]{criteria.getFormattedFromDate(), criteria.getFormattedToDateTime() }, new ResultSetExtractor<List<CaseCount>>()
      {
         Map<String, CaseCount> caseCounts = new HashMap<String, CaseCount>();
         CaseCountTotal caseCountTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );

         public List<CaseCount> extractData( ResultSet rs ) throws SQLException, DataAccessException
         {

            while (rs.next()){
               String description = rs.getString( 1 );
               CaseCount caseCount = caseCounts.get( description );
               if (caseCount == null) {
                  caseCount = new CaseCount( description, criteria.getPeriods() );
                  caseCounts.put( description, caseCount );
               }
               caseCount.addCount( rs.getString( 2 ), rs.getInt( 3 ) );
               caseCountTotal.plus( rs.getString( 2 ), rs.getInt( 3 ) );
            }

            ArrayList<CaseCount> result = new ArrayList<CaseCount>( caseCounts.values() );
            Collections.sort( result );
            result.add( caseCountTotal );
            return result;
         }
      });

      result.setCasecountSummary( summaryTotal );
      result.setCaseCountByTopOuOwner( caseCountPerTopOu );
      result.setCaseCountByOuOwner( caseCountByCaseTypeOwner );
      result.setCaseCountByCasetype( caseCountByCaseType );

      return result;
   }
   
   public List<ScatterChartValue> getVariationForCaseType( String caseTypeId )
   {
      String sql = "select unix_timestamp(closed_on)*1000, truncate(duration/60000,0) " +
            "from cases " +
            "where closed_on >= ? " +
            "and closed_on <= ? " +
            "and casetype = ? " +
            "order by closed_on";
            
      return jdbcTemplate.query( sql, new Object[]{ criteria.getFormattedFromDate(), criteria.getFormattedToDateTime(), caseTypeId },
            new ResultSetExtractor<List<ScatterChartValue>>()
      {
         public List<ScatterChartValue> extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            List<ScatterChartValue> result = new ArrayList<ScatterChartValue>(  );
            while(rs.next())
            {
               result.add( new ScatterChartValue( rs.getString( 1 ), rs.getString( 2 ) ) );
            }
            return result; 
         }
      } );
   }

   public List<CaseTypeValue> getCaseTypes()
   {
      String sql = "select id, description from descriptions where type = 'caseType'";
      return jdbcTemplate.query( sql, new ResultSetExtractor<List<CaseTypeValue>>()
      {
         public List<CaseTypeValue> extractData( ResultSet rs ) throws SQLException, DataAccessException
         {
            List<CaseTypeValue> result = new ArrayList<CaseTypeValue>();
            while( rs.next() )
            {
               result.add( new CaseTypeValue( rs.getString( 1 ), rs.getString( 2 ) ) );
            }
            Collections.sort( result );
            return result;
         }
      } );
   }
}
