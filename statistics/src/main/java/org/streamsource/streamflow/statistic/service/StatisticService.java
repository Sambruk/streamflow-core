/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.streamsource.streamflow.statistic.web.AppContextListener;
import org.streamsource.streamflow.statistic.web.Dao;

/**
 * A service that fetches case statistics data from the statistics database.
 */
public abstract class StatisticService
{
   SearchCriteria criteria;

   protected JdbcTemplate jdbcTemplate;

   protected String organizationQueryMysql = "select id, name, organization.left, organization.right from organization where organization.left = 0";
   protected String topOusQueryMysql = "select id, name, organization.left, organization.right from organization where parent in (select id from organization where organization.left = 0)";
   String topOuCasesQueryMysql = "select id from organization where organization.left >= ? and organization.right <= ? ";

   protected String organizationQueryMsSql = "select id, name, organization.[left], organization.[right] from organization where organization.[left] = 0";
   protected String topOusQueryMsSql = "select id, name, organization.[left], organization.[right] from organization where parent in (select id from organization where organization.[left] = 0)";
   String topOuCasesQueryMsSql = "select id from organization where organization.[left] >= ? and organization.[right] <= ? ";

   protected String organizationQuery = "";
   protected String topOusQuery = "";
   protected String topOuCasesQuery = "";

   public StatisticService(SearchCriteria criteria)
   {
      this.criteria = criteria;

      jdbcTemplate = AppContextListener.getJdbcTemplate();

      if (Dao.getDbVendor().equalsIgnoreCase( "mssql" ))
      {
         organizationQuery = organizationQueryMsSql;
         topOusQuery = topOusQueryMsSql;
         topOuCasesQuery = topOuCasesQueryMsSql;

      } else
      {
         organizationQuery = organizationQueryMysql;
         topOusQuery = topOusQueryMysql;
         topOuCasesQuery = topOuCasesQueryMysql;
      }
   }

   protected String getCaseOrgTotalQuery()
   {
      return "select " + getPeriodFunction( "closed_on" ) + " as period, count(case_id) as number " //
            + "from casesdescriptions " //
            + "where closed_on >= ? " //
            + "and closed_on <= ? " //
            + "group by " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by " + getGroupOrOrderByClause( "closed_on", "period" );
   }

   protected String getCaseOrgWithCaseTypeQuery()
   {
      return "select " + getPeriodFunction( "closed_on" ) + " as period, count(case_id) as number " //
            + "from casesdescriptions " //
            + "where closed_on >= ? " //
            + "and closed_on <= ? " //
            + "and casetype is not null " //
            + "group by " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by " + getGroupOrOrderByClause( "closed_on", "period" );
   }

   protected String getCaseOrgWithoutCaseTypeQuery()
   {
      return "select " + getPeriodFunction( "closed_on" ) + " as period, count(case_id) as number " //
            + "from casesdescriptions " //
            + "where closed_on >= ? " //
            + "and closed_on <= ? " //
            + "and casetype is null " //
            + "group by " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by " + getGroupOrOrderByClause( "closed_on", "period" );
   }

   protected String getTopOuCasesQuery()
   {
      /* was cases.closed_on */
      return "select " + getPeriodFunction( "closed_on" ) + " as period, count(case_id) as number " //
            + "from cases " //
            + "where casetype_owner in (" + topOuCasesQuery + ") " //
            + "and closed_on >= ? " //
            + "and closed_on <= ? " //
            + "and casetype_owner is not null " //
            + "group by " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by " + getGroupOrOrderByClause( "closed_on", "period" );
   } //

   protected String getCaseTypeOwnerQuery()
   {
      return "select casetype_owner, " + getPeriodFunction( "closed_on" ) + " as period, count(case_id) as number " //
            + "from casesdescriptions " //
            + "where closed_on >= ? " //
            + "and closed_on <= ? " //
            + "and casetype_owner is not null " //
            + "group by casetype_owner, " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by casetype_owner, " + getGroupOrOrderByClause( "closed_on", "period" );
   }

   protected String getCaseTypeQuery()
   {
      return "select '[Ärendetyp saknas]' as the_casetype, "
            + getPeriodFunction( "closed_on" )
            + " as period, count(case_id) as number " //
            + "from casesdescriptions c " //
            + "where c.closed_on >= ? " //
            + "and c.closed_on <= ? " //
            + "and c.casetype is null " //
            + "group by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" )
            + " " //
            + "union " //
            + "select casetype as the_casetype, " + getPeriodFunction( "closed_on" )
            + " as period, count(case_id) as number " //
            + "from casesdescriptions c " //
            + "where c.closed_on >= ? " //
            + "and c.closed_on <= ? " //
            + "and c.casetype is not null " //
            + "group by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" ) + " " //
            + "order by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" );
   }

   protected String getLabelQuery()
   {
      return "select '[Ärendetyp saknas]' as the_casetype, d2.description as the_label, "
            + getPeriodFunction( "closed_on" )
            + " as period, count(case_id) as number " //
            + "from cases c, labels l, descriptions d2 " //
            + "where c.id = l.id " //
            + "and l.label = d2.id "
            + "and c.closed_on >= ? " //
            + "and c.closed_on <= ? " //
            + "and c.casetype is null " //
            + "group by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" )
            + ", label " //
            + "union " //
            + "select d1.description as the_casetype, d2.description as the_label, " + getPeriodFunction( "closed_on" )
            + " as period, count(case_id) as number " //
            + "from cases c, descriptions d1, labels l, descriptions d2 " //
            + "where c.casetype = d1.id " + "and c.id = l.id " //
            + "and l.label = d2.id " //
            + "and c.closed_on >= ? " //
            + "and c.closed_on <= ? " //
            + "and c.casetype is not null " //
            + "group by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" ) + ", the_label " //
            + "order by the_casetype, " + getGroupOrOrderByClause( "closed_on", "period" ) + ", the_label";
   }

   public StatisticsResult getStatistics()
   {
      StatisticsResult result = new StatisticsResult();

      // Organization information
      final TopOu org = jdbcTemplate.query( organizationQuery, new ResultSetExtractor<TopOu>()
      {
         public TopOu extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            TopOu result = null;
            while (rs.next())
            {
               String id = rs.getString( 1 );
               String name = rs.getString( 2 );
               int left = rs.getInt( 3 );
               int right = rs.getInt( 4 );
               result = new TopOu( id, name, left, right );
            }

            return result;
         }
      } );

      // Compile summary
      List<CaseCount> summaryTotal = new ArrayList<CaseCount>();

      final String fromDate = criteria.getFormattedFromDate();
      final String toDateTime = criteria.getFormattedToDateTime();
      CaseCount orgTotalCaseCount = jdbcTemplate.query( getCaseOrgTotalQuery(), new Object[]
      { fromDate, toDateTime }, new ResultSetExtractor<CaseCount>()
      {
         public CaseCount extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( org.getName() + " total", criteria.getPeriods() );
            while (rs.next())
            {
               String period = rs.getString( 1 );
               int count = rs.getInt( 2 );
               total.addCount( period, count );
            }

            return total;
         }
      } );

      summaryTotal.add( orgTotalCaseCount );

      CaseCount orgTotalCaseCountWithCaseType = jdbcTemplate.query( getCaseOrgWithCaseTypeQuery(), new Object[]
      { fromDate, toDateTime }, new ResultSetExtractor<CaseCount>()
      {
         public CaseCount extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( "Ärenden med ärendetyp", criteria.getPeriods() );
            while (rs.next())
            {
               String period = rs.getString( 1 );
               int count = rs.getInt( 2 );
               total.addCount( period, count );
            }

            return total;
         }
      } );

      summaryTotal.add( orgTotalCaseCountWithCaseType );

      CaseCount orgTotalCaseCountWithoutCaseType = jdbcTemplate.query( getCaseOrgWithoutCaseTypeQuery(), new Object[]
      { fromDate, toDateTime }, new ResultSetExtractor<CaseCount>()
      {
         public CaseCount extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            CaseCount total = new CaseCount( "Ärenden utan ärendetyp (ingen tillhörighet)", criteria.getPeriods() );
            while (rs.next())
            {
               String period = rs.getString( 1 );
               int count = rs.getInt( 2 );
               total.addCount( period, count );
            }

            return total;
         }
      } );

      summaryTotal.add( orgTotalCaseCountWithoutCaseType );

      List<TopOu> topOuIds = jdbcTemplate.query( topOusQuery, new ResultSetExtractor<List<TopOu>>()
      {
         public List<TopOu> extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            List<TopOu> result = new ArrayList<TopOu>();
            while (rs.next())
            {
               String id = rs.getString( 1 );
               String name = rs.getString( 2 );
               int left = rs.getInt( 3 );
               int right = rs.getInt( 4 );
               TopOu current = new TopOu( id, name, left, right );
               result.add( current );
            }

            return result;
         }
      } );

      // Compile list of case counts where case type owner belongs to top OU's
      List<CaseCount> caseCountPerTopOu = new ArrayList<CaseCount>();

      final CaseCountTotal caseCountPerTopOuTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );
      for (TopOu topOu : topOuIds)
      {
         final TopOu currentTopOu = topOu;
         CaseCount caseCountForTopOu = jdbcTemplate.query( getTopOuCasesQuery(), new Object[]
         { currentTopOu.getLeft(), currentTopOu.getRight(), fromDate, toDateTime }, new ResultSetExtractor<CaseCount>()
         {
            public CaseCount extractData(ResultSet rs) throws SQLException, DataAccessException
            {
               CaseCount caseCount = new CaseCount( currentTopOu.getName(), criteria.getPeriods() );
               while (rs.next())
               {
                  String period = rs.getString( 1 );
                  int count = rs.getInt( 2 );
                  caseCount.addCount( period, count );
                  caseCountPerTopOuTotal.plus( period, count );
               }

               return caseCount;
            }
         } );
         caseCountPerTopOu.add( caseCountForTopOu );
      }
      Collections.sort( caseCountPerTopOu );
      caseCountPerTopOu.add( caseCountPerTopOuTotal );

      List<CaseCount> caseCountByCaseTypeOwner = jdbcTemplate.query( getCaseTypeOwnerQuery(), new Object[]
      { fromDate, toDateTime }, new ResultSetExtractor<List<CaseCount>>()
      {
         Map<String, CaseCount> caseCounts = new HashMap<String, CaseCount>();
         CaseCountTotal caseCountTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );

         public List<CaseCount> extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            while (rs.next())
            {
               String owner = rs.getString( 1 );
               CaseCount caseCount = caseCounts.get( owner );
               if (caseCount == null)
               {
                  caseCount = new CaseCount( owner, criteria.getPeriods() );
                  caseCounts.put( owner, caseCount );
               }
               String period = rs.getString( 2 );
               int count = rs.getInt( 3 );
               caseCount.addCount( period, count );
               caseCountTotal.plus( period, count );
            }

            ArrayList<CaseCount> result = new ArrayList<CaseCount>( caseCounts.values() );
            Collections.sort( result );
            result.add( caseCountTotal );

            return result;
         }
      } );

      List<CaseCount> caseCountByCaseType = jdbcTemplate.query( getCaseTypeQuery(), new Object[]
      { fromDate, toDateTime, fromDate, toDateTime }, new ResultSetExtractor<List<CaseCount>>()
      {
         Map<String, CaseCount> caseCounts = new HashMap<String, CaseCount>();
         CaseCountTotal caseCountTotal = new CaseCountTotal( "Summa", criteria.getPeriods() );

         public List<CaseCount> extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            while (rs.next())
            {
               String caseType = rs.getString( 1 );
               CaseCount caseCount = caseCounts.get( caseType );
               if (caseCount == null)
               {
                  caseCount = new CaseCount( caseType, criteria.getPeriods() );
                  caseCounts.put( caseType, caseCount );
               }
               String period = rs.getString( 2 );
               int count = rs.getInt( 3 );
               caseCount.addCount( period, count );
               caseCountTotal.plus( period, count );
            }

            ArrayList<CaseCount> result = new ArrayList<CaseCount>( caseCounts.values() );
            Collections.sort( result );
            result.add( caseCountTotal );

            return result;
         }
      } );

      Map<String, List<CaseCount>> caseCountByLabelPerCaseType = jdbcTemplate.query( getLabelQuery(), new Object[]
      { fromDate, toDateTime, fromDate, toDateTime }, new ResultSetExtractor<Map<String, List<CaseCount>>>()
      {
         Map<String, Map<String, CaseCount>> caseCounts = new HashMap<String, Map<String, CaseCount>>();

         public Map<String, List<CaseCount>> extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            while (rs.next())
            {
               String caseType = rs.getString( 1 );
               Map<String, CaseCount> caseCountPerLabel = caseCounts.get( caseType );
               if (caseCountPerLabel == null)
               {
                  caseCountPerLabel = new HashMap<String, CaseCount>();
                  caseCounts.put( caseType, caseCountPerLabel );
               }
               String label = rs.getString( 2 );
               CaseCount caseCount = caseCountPerLabel.get( label );
               if (caseCount == null)
               {
                  caseCount = new CaseCount( label, criteria.getPeriods() );
                  caseCountPerLabel.put( label, caseCount );
               }
               String period = rs.getString( 3 );
               int count = rs.getInt( 4 );
               caseCount.addCount( period, count );
            }

            Map<String, List<CaseCount>> result = new HashMap<String, List<CaseCount>>();
            for (String caseType : caseCounts.keySet())
            {
               Map<String, CaseCount> caseCountPerLabel = caseCounts.get( caseType );
               List<CaseCount> labelCaseCounts = new ArrayList<CaseCount>( caseCountPerLabel.values() );
               Collections.sort( labelCaseCounts );
               result.put( caseType, labelCaseCounts );
            }

            return result;
         }
      } );

      result.setCaseCountSummary( summaryTotal );
      result.setCaseCountByTopOuOwner( caseCountPerTopOu );
      result.setCaseCountByOuOwner( caseCountByCaseTypeOwner );
      result.setCaseCountByCaseType( caseCountByCaseType );
      result.setCaseCountByLabelPerCaseType( caseCountByLabelPerCaseType );

      return result;
   }

   public List<ScatterChartValue> getVariationForCaseType(String caseTypeId)
   {
      String sql = "";
      if (Dao.getDbVendor().equals( "mssql" ))
      {
         sql = "select CONVERT( BIGINT ,DATEDIFF(s,'19700101', [closed_on])) * 1000, " + " CAST( closed_on AS Date )"
               + " from cases\n" + "where closed_on >= ? " + "and closed_on <= ? " + "and casetype = ? "
               + "order by closed_on";
      } else
      {
         sql = "select unix_timestamp(closed_on)*1000, truncate(duration/3600000,0) " + "from cases "
               + "where closed_on >= ? " + "and closed_on <= ? " + "and casetype = ? " + "order by closed_on";
      }

      return jdbcTemplate.query( sql, new Object[]
      { criteria.getFormattedFromDate(), criteria.getFormattedToDateTime(), caseTypeId },
            new ResultSetExtractor<List<ScatterChartValue>>()
            {
               public List<ScatterChartValue> extractData(ResultSet rs) throws SQLException, DataAccessException
               {
                  List<ScatterChartValue> result = new ArrayList<ScatterChartValue>();
                  while (rs.next())
                  {
                     result.add( new ScatterChartValue( rs.getString( 1 ), rs.getString( 2 ) ) );
                  }

                  return result;
               }
            } );
   }

   public List<CaseTypeValue> getCaseTypes()
   {
      String sql = "select distinct a.id, a.description from descriptions a, cases b " + "where a.type = 'caseType' "
            + "and a.id = b.caseType " + "and b.closed_on >= '" + criteria.getFormattedFromDate() + "' "
            + "and b.closed_on <= '" + criteria.getFormattedToDateTime() + "' " + "order by description";

      return jdbcTemplate.query( sql, new ResultSetExtractor<List<CaseTypeValue>>()
      {
         public List<CaseTypeValue> extractData(ResultSet rs) throws SQLException, DataAccessException
         {
            List<CaseTypeValue> result = new ArrayList<CaseTypeValue>();
            while (rs.next())
            {
               result.add( new CaseTypeValue( rs.getString( 1 ), rs.getString( 2 ) ) );
            }

            return result;
         }
      } );
   }

   public abstract String getPeriodFunction(String column);

   public abstract String getGroupOrOrderByClause(String column, String alias);
}
