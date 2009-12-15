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

package se.streamsource.streamflow.web.resource.users.search;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.organization.search.DateSearchKeyword;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.organization.search.SearchTaskListDTO;
import se.streamsource.streamflow.resource.organization.search.UserSearchKeyword;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JAVADOC
 */
public class SearchTasksServerResource extends AbstractTaskListServerResource
{
   public TaskListDTO search( StringDTO query )
         throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String queryString = query.string().get().trim();
      if (queryString.matches( "[\\$\\.\\*\\+\\?\\(\\)\\[\\]\\|\\^\\{\\}\\\\]+" ))
      {
         throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, ErrorResources.search_string_malformed.toString() );
      }

      if (queryString.length() > 0)
      {
         QueryBuilder<TaskEntity> queryBuilder = module
               .queryBuilderFactory().newQueryBuilder( TaskEntity.class );
         List<String> searches = extractSubQueries( queryString );
         for (int i = 0; i < searches.size(); i++)
         {
            String search = searches.get( i );
            // Remove the optional " characters
            search = search.replaceAll( "\"", "" );

            if (search.startsWith( "label:" ))
            {
               search = search.substring( "label:".length() );
               queryBuilder = queryBuilder.where( eq( QueryExpressions.oneOf(
                     templateFor( Labelable.Data.class )
                           .labels() ).description(), search ) );
            } else if (search.startsWith( "assigned:" ))
            {
               search = search.substring( "assigned:".length() );
               search = getAssignedTo( search );
               Assignee assignee = templateFor( TaskEntity.class )
                     .assignedTo().get();
               Describable.Data describable = templateFor(
                     Describable.Data.class, assignee );
               queryBuilder = queryBuilder.where( eq( describable.description(), search ) );
            } else if (search.startsWith( "project:" ))
            {
               search = search.substring( "project:".length() );
               Owner owner = templateFor( TaskEntity.class ).owner().get();
               Describable.Data describable = templateFor(
                     Describable.Data.class, owner );
               queryBuilder = queryBuilder.where( eq( describable.description(), search ) );
            } else if (search.startsWith( "created:" ))
            {
               search = search.substring( "created:".length() );
               Date referenceDate = new Date();
               Date lowerBoundDate = getLowerBoundDate( search,
                     referenceDate );
               Date upperBoundDate = getUpperBoundDate( search,
                     referenceDate );

               if (lowerBoundDate == null || upperBoundDate == null)
               {
                  continue;
               }
               queryBuilder = queryBuilder.where( and( ge( templateFor( TaskEntity.class )
                     .createdOn(), lowerBoundDate ), le( templateFor(
                     TaskEntity.class ).createdOn(), upperBoundDate ) ) );
            } else
            {
               queryBuilder = queryBuilder.where( or( eq( templateFor( TaskEntity.class )
                     .taskId(), search ), matches( templateFor(
                     TaskEntity.class ).description(), search ), matches(
                     templateFor( TaskEntity.class ).note(), search ) ) );
            }
         }

         // TODO: Do not perform a query with null whereClause! How to check
         // this?
         Query<TaskEntity> tasks = queryBuilder.newQuery( uow );
         return buildTaskList( tasks, SearchTaskDTO.class);
      } else
      {
         return vbf.newValue( SearchTaskListDTO.class );
      }
   }

   protected List<String> extractSubQueries( String query )
   {
      List<String> subQueries = new ArrayList<String>();
      // TODO: Extract regular expression to resource file.
      String regExp = "(?:\\w+\\:)?(?:\\\"[^\\\"]*?\\\")|(?:[^\\s]+)";
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
         subQueries.add( m.group() );
      }

      if (subQueries.isEmpty())
      {
         if (query.length() > 0)
            subQueries.add( query );
      }
      return subQueries;
   }

   /**
    * Get the calling user from the access controller context.
    *
    * @param search
    * @return
    */
   protected String getAssignedTo( String search )
   {
      if (UserSearchKeyword.ME.toString().equalsIgnoreCase( search ))
      {
         List<Principal> principals = getRequest().getClientInfo().getPrincipals();
         if (principals.isEmpty())
            return "administrator";
         else
         {
            return principals.iterator().next().getName();
         }
      } else
      {
         return search;
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

   @Override
   protected void addAdditionalValues( TaskDTO prototype, TaskEntity task )
   {
      if (task.assignedTo().get() != null)
      {
         ((SearchTaskDTO) prototype).assignedTo().set( task.assignedTo().get().getDescription() );
      } else
      {
         ((SearchTaskDTO) prototype).assignedTo().set( null );
      }
      ((SearchTaskDTO) prototype).project().set( task.owner().get().getDescription() );

    }
}
