/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.resource.organization.search.DateSearchKeyword;
import se.streamsource.streamflow.resource.organization.search.UserSearchKeyword;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JAVADOC
 */
@Mixins(SearchCaseQueries.Mixin.class)
public interface
      SearchCaseQueries
{
   Query<Case> search( String query );

   abstract class Mixin
         implements SearchCaseQueries
   {
      @Structure
      Module module;

      @This
      UserAuthentication.Data user;

      public Query<Case> search( String query )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         String queryString = query.trim();

         if (queryString.length() > 0)
         {

            StringBuilder queryBuilder = new StringBuilder();
            List<SubQuery> searches = extractSubQueries( queryString );
            for (int i = 0; i < searches.size(); i++)
            {
               SubQuery search = searches.get( i );

               if (search.hasName("status"))
               {
                  queryBuilder.append( " status:" ).append( search.getValue() );
               } else if (search.hasName( "label" ))
               {
                  List<LabelEntity> labels = new ArrayList<LabelEntity>();
                  for (String label : search.getValue().split(","))
                  {

                     StringBuilder labelQueryBuilder = new StringBuilder(
                           "type:se.streamsource.streamflow.web.domain.entity.label.LabelEntity");
                     labelQueryBuilder.append(" (description:").append(label);
                     labelQueryBuilder.append(" OR ntext:").append(label).append(")");

                      Iterables.addAll(labels,  module.queryBuilderFactory()
                           .newNamedQuery(LabelEntity.class, uow, "solrquery")
                           .setVariable("query", labelQueryBuilder.toString()));

                  }
                  if (labels.iterator().hasNext())
                  {
                     queryBuilder.append(" labels:(");
                     int count = 0;
                     for (LabelEntity labelEntity : labels)
                     {
                        if (count == 0)
                        {
                           queryBuilder.append(labelEntity.identity().get());
                        } else
                        {
                           queryBuilder.append(" OR ").append(labelEntity.identity().get());
                        }

                        count++;
                     }
                     queryBuilder.append(")");
                  } else
                  {
                     // dismiss search - no label/s with given name exist.
                     // Return empty search
                     return module.queryBuilderFactory().newQueryBuilder(Case.class)
                           .newQuery(Collections.<Case> emptyList());
                  }
               } else if (search.hasName( "caseType" ))
               {
                  StringBuilder caseTypeQueryBuilder = new StringBuilder( "type:se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity" );
                  caseTypeQueryBuilder.append( " (description:" ).append( search.getQuotedValue() );
                  caseTypeQueryBuilder.append( " OR ntext:").append( search.getQuotedValue() ).append( ")" );

                  Query<CaseTypeEntity> caseTypes = module.queryBuilderFactory()
                        .newNamedQuery( CaseTypeEntity.class, uow, "solrquery" ).setVariable( "query", caseTypeQueryBuilder.toString() );

                  if (caseTypes.iterator().hasNext())
                  {
                     queryBuilder.append( " caseType:(" );
                     int count = 0;
                     for (CaseTypeEntity caseType : caseTypes)
                     {
                        if (count == 0)
                        {
                           queryBuilder.append( caseType.identity().get() );
                        } else
                        {
                           queryBuilder.append( " OR " ).append( caseType.identity().get() );
                        }

                        count++;
                     }
                     queryBuilder.append( ")" );
                  } else
                  {
                     // dismiss search - no case type/s for given name exists. Return empty search
                     return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );

                  }
               } else if (search.hasName( "project" ))
               {
                  StringBuilder projectQueryBuilder = new StringBuilder( "type:se.streamsource.streamflow.web.domain.entity.project.ProjectEntity" );
                  projectQueryBuilder.append( " ( description:" ).append( search.getQuotedValue() );
                  projectQueryBuilder.append( " OR ntext:").append( search.getQuotedValue() ).append( ")" );

                  Query<ProjectEntity> projects = module.queryBuilderFactory()
                        .newNamedQuery( ProjectEntity.class, uow, "solrquery" ).setVariable( "query", projectQueryBuilder.toString() );

                  if (projects.iterator().hasNext())
                  {
                     queryBuilder.append( " owner:(" );
                     int count = 0;
                     for (ProjectEntity project : projects)
                     {
                        if (count == 0)
                        {
                           queryBuilder.append( project.identity().get() );
                        } else
                        {
                           queryBuilder.append( " OR " ).append( project.identity().get() );
                        }

                        count++;
                     }
                     queryBuilder.append( ")" );
                  } else
                  {
                     // dismiss search - no project/s for given name exists. Return empty search
                     return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );
                  }
               } else if (search.hasName( "createdBy" ))
               {
                  StringBuilder creatorQueryBuilder = new StringBuilder( "type:se.streamsource.streamflow.web.domain.entity.user.UserEntity" );
                  String userName = user.userName().get();
                  creatorQueryBuilder.append( " (id:" ).append( getUserInSearch( search, userName ) )
                        .append(" OR ").append( " description:" ).append( getUserInSearch( search, userName ) )
                        .append(" OR ").append( " ntext:" ).append( getUserInSearch( search, userName ) ).append( ")" );

                  Query<UserEntity> users = module.queryBuilderFactory()
                        .newNamedQuery( UserEntity.class, uow, "solrquery" ).setVariable( "query", creatorQueryBuilder.toString() );

                  int count = 0;
                  for (UserEntity user : users)
                  {
                     if (count == 0)
                     {
                        queryBuilder.append( " createdBy:(" ).append( user.identity().get() );
                     } else
                     {
                        queryBuilder.append( " OR " ).append( user.identity().get() );
                     }

                     count++;
                  }

                  if (count > 0)
                     queryBuilder.append( ")" );
                  else
                  {
                     // dismiss search - no user/s for given name exists. Return empty search
                     return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );
                  }

               } else if (search.hasName( "description", "note", "name", "contactId", "phoneNumber", "emailAddress" ))
               {
                  queryBuilder.append( " " ).append( search.getName() ).append( ":" ).append( search.getQuotedValue() );

               } else if (search.hasName( "createdOn" ))
               {
                  buildDateQuery( queryBuilder, search );
               } else if (search.hasName( "assignedTo" ))
               {

                  StringBuilder creatorQueryBuilder = new StringBuilder( "type:se.streamsource.streamflow.web.domain.entity.user.UserEntity" );
                  String userName = user.userName().get();
                  creatorQueryBuilder.append( " (id:" ).append( getUserInSearch( search, userName ) )
                        .append(" OR ").append( " description:" ).append( getUserInSearch( search, userName ) )
                        .append(" OR ").append( " ntext:" ).append( getUserInSearch( search, userName ) ).append( ")" );

                  Query<UserEntity> users = module.queryBuilderFactory()
                        .newNamedQuery( UserEntity.class, uow, "solrquery" ).setVariable( "query", creatorQueryBuilder.toString() );

                  int count = 0;
                  for (UserEntity user : users)
                  {
                     if (count == 0)
                     {
                        queryBuilder.append( " assignedTo:(" ).append( user.identity().get() );
                     } else
                     {
                        queryBuilder.append( " OR " ).append( user.identity().get() );
                     }

                     count++;
                  }

                  if (count > 0)
                     queryBuilder.append( ")" );
                  else
                  {
                     // dismiss search - no user/s for given name exists. Return empty search
                     return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );
                  }
               } else
               {
                  if (queryBuilder.length() > 0)
                     queryBuilder.append( " " );
                  queryBuilder.append( search.getValue() );
               }
            }

            if (queryBuilder.length() != 0)
            {
               queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
               queryBuilder.append( " !status:DRAFT" );
               Query<Case> cases = module.queryBuilderFactory()
                     .newNamedQuery( Case.class, uow, "solrquery" ).setVariable( "query", queryBuilder.toString() );
               return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Iterables.filter( new Specification<Case>()
               {
                  public boolean satisfiedBy( Case item )
                  {
                     return item.hasPermission( user.userName().get(), PermissionType.read.name() );
                  }
               }, cases) );
            }
         }
         return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );
      }

      private void buildDateQuery( StringBuilder queryBuilder, SubQuery search )
      {
         String name = search.getName();
         String searchDateFrom = search.getValue();
         String searchDateTo = search.getValue();
         if (occurrancesOfInString( "-", searchDateFrom ) == 1)
         {
            searchDateFrom = searchDateFrom.substring( 0, searchDateFrom.indexOf( "-" ) );
            searchDateTo = searchDateTo.substring( searchDateTo.indexOf( "-" ) + 1, searchDateTo.length() );
         }
         Date referenceDate = new Date();
         Date lowerBoundDate = getLowerBoundDate( searchDateFrom,
               referenceDate );
         Date upperBoundDate = getUpperBoundDate( searchDateTo,
               referenceDate );

         if (lowerBoundDate == null || upperBoundDate == null)
         {
            return;
         }
         queryBuilder.append( " " ).append( name ).append( ":[" ).
               append( DateFunctions.toUtcString( lowerBoundDate ) ).
               append( " TO " ).
               append( DateFunctions.toUtcString( upperBoundDate ) ).
               append( "]" );
      }

      protected List<SubQuery> extractSubQueries( String query )
      {
         List<SubQuery> subQueries = new ArrayList<SubQuery>();
         // TODO: Extract regular expression to resource file.
         String regExp = "((\\w+)\\:)?((\\\"([^\\\"]*)\\\")|([^\\s]+))"; // old "(?:\\w+\\:)?(?:\\\"[^\\\"]*?\\\")|(?:[^\\s]+)";
         Pattern p;
         try
         {
            p = Pattern.compile( regExp );
         } catch (PatternSyntaxException e)
         {
            return subQueries;
         }
         Matcher m = p.matcher( query );
         while (m.find())
         {
            String value = m.group( 5 );
            if (value == null)
               value = m.group( 3 );
            subQueries.add( new SubQuery( m.group( 2 ), value ) );
         }

         if (subQueries.isEmpty())
         {
            if (query.length() > 0)
               subQueries.add( new SubQuery( null, query ) );
         }
         return subQueries;
      }

      /**
       * Get the calling user from the access controller roleMap.
       *
       * @param search
       * @return
       */
      protected String getUserInSearch( SubQuery search, String user )
      {
         if (UserSearchKeyword.ME.toString().equalsIgnoreCase( search.getValue() ))
         {
            return user;
         } else
         {
            return search.getQuotedValue();
         }
      }

      protected Date getLowerBoundDate( String dateAsString, Date referenceDate )
      {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime( referenceDate );
         Date lowerBoundDate = null;

         // TODAY, YESTERDAY, HOUR, WEEK,
         if (DateSearchKeyword.TODAY.toString().equalsIgnoreCase( dateAsString ))
         {
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
            lowerBoundDate = calendar.getTime();
         } else if (DateSearchKeyword.YESTERDAY.toString().equalsIgnoreCase(
               dateAsString ))
         {
            calendar.add( Calendar.DAY_OF_MONTH, -1 );
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
            lowerBoundDate = calendar.getTime();
         } else if (DateSearchKeyword.HOUR.toString().equalsIgnoreCase(
               dateAsString ))
         {
            calendar.add( Calendar.HOUR_OF_DAY, -1 );
            lowerBoundDate = calendar.getTime();
         } else if (DateSearchKeyword.WEEK.toString().equalsIgnoreCase(
               dateAsString ))
         {
            calendar.add( Calendar.WEEK_OF_MONTH, -1 );
            lowerBoundDate = calendar.getTime();
         } else
         {
            try
            {
               lowerBoundDate = parseToDate( dateAsString );
               calendar.setTime( lowerBoundDate );
               calendar.set( Calendar.HOUR_OF_DAY, 0 );
               calendar.set( Calendar.MINUTE, 0 );
               calendar.set( Calendar.SECOND, 0 );
               lowerBoundDate = calendar.getTime();
            } catch (ParseException e)
            {
               // Skip the "created:" search as the input query can not be
               // interpreted.
               lowerBoundDate = null;
            } catch (IllegalArgumentException e)
            {
               lowerBoundDate = null;
            }
         }
         return lowerBoundDate;
      }

      protected Date getUpperBoundDate( String dateAsString, Date referenceDate )
      {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime( referenceDate );
         Date upperBoundDate = calendar.getTime();

         // TODAY, YESTERDAY, HOUR, WEEK,
         if (DateSearchKeyword.TODAY.toString().equalsIgnoreCase( dateAsString ))
         {
            calendar.set( Calendar.HOUR_OF_DAY, 23 );
            calendar.set( Calendar.MINUTE, 59 );
            calendar.set( Calendar.SECOND, 59 );
            upperBoundDate = calendar.getTime();
         } else if (DateSearchKeyword.YESTERDAY.toString().equalsIgnoreCase(
               dateAsString ))
         {
            calendar.add( Calendar.DAY_OF_MONTH, -1 );
            calendar.set( Calendar.HOUR_OF_DAY, 23 );
            calendar.set( Calendar.MINUTE, 59 );
            calendar.set( Calendar.SECOND, 59 );
            upperBoundDate = calendar.getTime();
         } else if (DateSearchKeyword.HOUR.toString().equalsIgnoreCase(
               dateAsString ))
         {
            // Do nothing
         } else if (DateSearchKeyword.WEEK.toString().equalsIgnoreCase(
               dateAsString ))
         {
            // Do nothing
         } else
         {
            try
            {
               upperBoundDate = parseToDate( dateAsString );
               calendar.setTime( upperBoundDate );
               calendar.set( Calendar.HOUR_OF_DAY, 23 );
               calendar.set( Calendar.MINUTE, 59 );
               calendar.set( Calendar.SECOND, 59 );
               upperBoundDate = calendar.getTime();
            } catch (ParseException e)
            {
               upperBoundDate = null;
            } catch (IllegalArgumentException e)
            {
               upperBoundDate = null;
            }
         }
         return upperBoundDate;
      }

      private Date parseToDate( String dateAsString ) throws ParseException,
            IllegalArgumentException
      {
         // Formats that should pass: yyyy-MM-dd, yyyyMMdd.
         // TODO: Should we also support yyyy?
         SimpleDateFormat dateFormat = null;
         if (dateAsString == null)
         {
            throw new ParseException( "Date string can not be null!", 0 );
         }
         dateAsString = dateAsString.replaceAll( "-", "" );
         if (dateAsString.length() != 8)
         {
            throw new IllegalArgumentException( "Date format not supported!" );
         }
         if (dateAsString.length() == 8)
         {
            // TODO: Extract date format to resource file.
            dateFormat = new SimpleDateFormat( "yyyyMMdd" );
         }
         return dateFormat.parse( dateAsString );
      }

      protected int occurrancesOfInString( String pattern, String source )
      {
         Pattern p = Pattern.compile( pattern );
         Matcher m = p.matcher( source );
         int count = 0;
         while (m.find())
         {
            count++;
            System.out.println( "Match number " + count );
            System.out.println( "start(): " + m.start() );
            System.out.println( "end(): " + m.end() );
         }
         return count;
      }

      class SubQuery
      {
         String name;

         String value;

         public SubQuery( String name, String value )
         {
            this.name = name;
            this.value = value;
         }

         public String getName()
         {
            return name;
         }

         public String getValue()
         {
            return value;
         }

         public String getQuotedValue()
         {
            return "\""+value+"\"";
         }

         public boolean hasName( String... names )
         {
            return name == null ? false : Arrays.asList( names ).contains( name );
         }
      }
   }
}
