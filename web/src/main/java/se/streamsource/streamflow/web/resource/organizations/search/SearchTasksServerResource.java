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

package se.streamsource.streamflow.web.resource.organizations.search;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.le;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.organization.search.DateSearchKeyword;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.organization.search.SearchTaskListDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * JAVADOC
 */
public class SearchTasksServerResource
    extends AbstractTaskListServerResource
{
    public SearchTaskListDTO search(StringDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String queryString = query.string().get().trim();
        if (queryString.length() > 0)
        {
            QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            List<String> searches = extractSubQueries(queryString);
            for (int i = 0; i < searches.size(); i++)
            {
                String search = searches.get(i);
                // Remove the optional " characters
                search = search.replaceAll("\"", "");

                if (search.startsWith("label:"))
                {
                    search = search.substring("label:".length());
                    queryBuilder.where(eq(QueryExpressions.oneOf(templateFor(Labelable.LabelableState.class).labels()).description(), search));
                }
//              else if (search.startsWith("assigned:"))
//              {
//                  search = search.substring("assigned:".length());
//                  queryBuilder.where(eq(QueryExpressions.oneOf(templateFor(Labelable.LabelableState.class).labels()).description(), search));
//              }
				else if (search.startsWith("project:")) 
				{
					search = search.substring("project:".length());
					Owner owner = templateFor(TaskEntity.class).owner().get();
					Describable.DescribableState describable = templateFor(
							Describable.DescribableState.class, owner);
					queryBuilder.where(eq(describable.description(), search));
				} else if (search.startsWith("created:"))                
				{
                    search = search.substring("created:".length());
                    Date referenceDate = new Date();
                    Date lowerBoundDate = getLowerBoundDate(search, referenceDate);
                    Date upperBoundDate = getUpperBoundDate(search, referenceDate);

                    if(lowerBoundDate == null || upperBoundDate == null) 
                    {
                    	continue;
                    }
                    queryBuilder.where(and(
                            ge(templateFor(TaskEntity.class).createdOn(), lowerBoundDate),
                            le(templateFor(TaskEntity.class).createdOn(), upperBoundDate)));
                } else
                {
                    queryBuilder.where(or(
                            eq(templateFor(TaskEntity.class).taskId(), search),
                            matches(templateFor(TaskEntity.class).description(), search),
                            matches(templateFor(TaskEntity.class).note(), search)));
                }
            }

            // TODO: Do not perform a query with null whereClause! How to check this?
            Query<TaskEntity> tasks = queryBuilder.newQuery(uow);
            return buildTaskList(tasks, SearchTaskDTO.class, SearchTaskListDTO.class);
        } else
        {
            return vbf.newValue(SearchTaskListDTO.class);
        }
    }

	private List<String> extractSubQueries(String query) 
	{
		List<String> subQueries = null;
		// TODO: Extract regular expression to resource file
		String regExp = "(?:\\w+\\:)?(?:\\\"[^\\\"]*?\\\")|(?:[^\\s]+)";
		Pattern p;
		try 
		{
			p = Pattern.compile(regExp);
		} catch (PatternSyntaxException e) 
		{
			return subQueries;
		}
		Matcher m = p.matcher(query);
		while (m.find()) 
		{
			if(subQueries == null) 
			{
				subQueries = new ArrayList<String>();
			}
			
			subQueries.add(m.group());
		}
		return subQueries;
	}

    protected Date getLowerBoundDate(String search, Date referenceDate) 
    {
        Calendar calendar = Calendar.getInstance();
    	calendar.setTime(referenceDate);
    	Date lowerBoundDate = null;
        
        // TODAY, YESTERDAY, HOUR, WEEK, 
        if (DateSearchKeyword.TODAY.toString().equalsIgnoreCase(search)) 
        {
        	calendar.set(Calendar.HOUR_OF_DAY, 0);
        	calendar.set(Calendar.MINUTE, 0);
        	calendar.set(Calendar.SECOND, 0);
        	lowerBoundDate = calendar.getTime();
        } else if (DateSearchKeyword.YESTERDAY.toString().equalsIgnoreCase(search)) 
        {
        	calendar.add(Calendar.DAY_OF_MONTH, -1);
        	calendar.set(Calendar.HOUR_OF_DAY, 0);
        	calendar.set(Calendar.MINUTE, 0);
        	calendar.set(Calendar.SECOND, 0);
        	lowerBoundDate = calendar.getTime();
        } else if (DateSearchKeyword.HOUR.toString().equalsIgnoreCase(search)) 
        {
        	calendar.add(Calendar.HOUR_OF_DAY, -1);
        	lowerBoundDate = calendar.getTime();
        } else if (DateSearchKeyword.WEEK.toString().equalsIgnoreCase(search)) 
        {
        	calendar.add(Calendar.WEEK_OF_MONTH, -1);
        	lowerBoundDate = calendar.getTime();
        } else 
        {
        	try 
        	{
                // Formats that passes: yyyy-MM-dd, yyyyMMdd and yyMMdd
            	// TODO: Support the above specified date formats.
             	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					lowerBoundDate = dateFormat.parse(search);
			} catch (ParseException e) 
			{
				// Skip the "created:" search as the input query can not be interpreted.
				lowerBoundDate = null;
			}
        }
        return lowerBoundDate;
    }

    protected Date getUpperBoundDate(String search, Date workingDate) 
    {
        Calendar calendar = Calendar.getInstance();
    	calendar.setTime(workingDate);
    	Date upperBoundDate = calendar.getTime();
        
        // TODAY, YESTERDAY, HOUR, WEEK, 
        if (DateSearchKeyword.TODAY.toString().equalsIgnoreCase(search)) 
        {
        	calendar.set(Calendar.HOUR_OF_DAY, 23);
        	calendar.set(Calendar.MINUTE, 59);
        	calendar.set(Calendar.SECOND, 59);
        	upperBoundDate = calendar.getTime();
        } else if (DateSearchKeyword.YESTERDAY.toString().equalsIgnoreCase(search)) 
        {
        	calendar.add(Calendar.DAY_OF_MONTH, -1);
        	calendar.set(Calendar.HOUR_OF_DAY, 23);
        	calendar.set(Calendar.MINUTE, 59);
        	calendar.set(Calendar.SECOND, 59);
        	upperBoundDate = calendar.getTime();
        } else if (DateSearchKeyword.HOUR.toString().equalsIgnoreCase(search)) 
        {
        	// Do nothing
        } else if (DateSearchKeyword.WEEK.toString().equalsIgnoreCase(search)) 
        {
        	// Do nothing
        } 
        return upperBoundDate;
    }

}
