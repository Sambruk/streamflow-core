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

package se.streamsource.streamflow.web.domain.entity.gtd;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * This interface with mixin provides general application of a filter for queries.
 */
@Mixins(AbstractCaseQueriesFilter.Mixin.class)
public interface AbstractCaseQueriesFilter
{
   QueryBuilder<Case> applyFilter(QueryBuilder<Case> builder, String filter);

   abstract class Mixin
           implements AbstractCaseQueriesFilter
   {
      @Structure
      Module module;

      public QueryBuilder<Case> applyFilter(QueryBuilder<Case> builder, String filter)
      {

         List<SubQuery> searches = extractSubQueries(filter);
         for (int i = 0; i < searches.size(); i++)
         {
            SubQuery search = searches.get(i);

            if (search.hasName("status"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String status : Arrays.asList(search.getValue().split(",")))
               {
                  if (count == 0)
                  {
                     expression = eq(templateFor(Status.Data.class).status(), CaseStates.valueOf(status));
                  } else
                  {
                     expression = or(expression, eq(templateFor(Status.Data.class).status(), CaseStates.valueOf(status)));
                  }
                  count++;
               }

               builder = builder.where(expression);


            } else if (search.hasName("label"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String label : search.getValue().split(","))
               {
                  Label labelEntity = null;
                  try
                  {
                     labelEntity = module.unitOfWorkFactory().currentUnitOfWork().get(Label.class, label);
                  } catch (NoSuchEntityException e)
                  {
                     // do nothing
                  }
                  if (labelEntity == null)
                     continue;

                  if (count == 0)
                  {
                     expression = contains(templateFor(Labelable.Data.class).labels(), labelEntity);
                  } else
                  {
                     expression = or(expression, contains(templateFor(Labelable.Data.class).labels(), labelEntity));
                  }

                  count++;

               }

               if (expression != null)
                  builder = builder.where(expression);

            } else if (search.hasName("caseType"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String caseType : search.getValue().split(","))
               {
                  CaseType caseTypeEntity = null;
                  try
                  {
                     caseTypeEntity = module.unitOfWorkFactory().currentUnitOfWork().get(CaseType.class, caseType);
                  } catch (NoSuchEntityException e)
                  {
                     // do nothing
                  }
                  if (caseTypeEntity == null)
                     continue;

                  if (count == 0)
                  {
                     expression = eq(templateFor(TypedCase.Data.class).caseType(), caseTypeEntity);
                  } else
                  {
                     expression = or(expression, eq(templateFor(TypedCase.Data.class).caseType(), caseTypeEntity));
                  }

                  count++;
               }

               if (expression != null)
                  builder = builder.where(expression);

            } else if (search.hasName("project"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String project : search.getValue().split(","))
               {
                  Project projectEntity = null;
                  try
                  {
                     projectEntity = module.unitOfWorkFactory().currentUnitOfWork().get(Project.class, project);
                  } catch (NoSuchEntityException e)
                  {
                     // do nothing
                  }
                  if (projectEntity == null)
                     continue;

                  if (count == 0)
                  {
                     expression = eq(templateFor(Ownable.Data.class).owner(), projectEntity);
                  } else
                  {
                     expression = or(expression, eq(templateFor(Ownable.Data.class).owner(), projectEntity));
                  }

                  count++;
               }

               if (expression != null)
                  builder = builder.where(expression);

            } else if (search.hasName("createdBy"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String user : search.getValue().split(","))
               {
                  User userEntity = null;
                  try
                  {
                     userEntity = module.unitOfWorkFactory().currentUnitOfWork().get(User.class, user);
                  } catch (NoSuchEntityException e)
                  {
                     // do nothing
                  }
                  if (userEntity == null)
                     continue;

                  if (count == 0)
                  {
                     expression = eq(templateFor(CreatedOn.class).createdBy(), userEntity);
                  } else
                  {
                     expression = or(expression, eq(templateFor(CreatedOn.class).createdBy(), userEntity));
                  }

                  count++;
               }

               if (expression != null)
                  builder = builder.where(expression);


            } else if (search.hasName("createdOn") || search.hasName("dueOn"))
            {
               String value = search.getValue();
               DateMidnight lowerDate = null;
               DateMidnight upperDate = null;
               DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

               if (value.indexOf("-") == -1)
               {
                  lowerDate = new DateMidnight(formatter.parseDateTime(value));
               } else
               {
                  lowerDate = new DateMidnight(formatter.parseDateTime(value.substring(0, value.indexOf("-"))));
                  upperDate = new DateMidnight(formatter.parseDateTime(value.substring(value.indexOf("-") + 1)));
               }

               if (search.hasName("createdOn"))
               {
                  if (upperDate == null)
                  {
                     builder = builder.where(and(
                             ge(templateFor(CreatedOn.class).createdOn(), lowerDate.toDate()),
                             le(templateFor(CreatedOn.class).createdOn(), lowerDate.plusDays(1).toDate())));
                  } else
                  {
                     builder = builder.where(and(
                             ge(templateFor(CreatedOn.class).createdOn(), lowerDate.toDate()),
                             le(templateFor(CreatedOn.class).createdOn(), upperDate.plusDays(1).toDate())));
                  }
               } else
               {
                  if (upperDate == null)
                  {
                     builder = builder.where(and(
                             ge(templateFor(DueOn.Data.class).dueOn(), lowerDate.toDate()),
                             le(templateFor(DueOn.Data.class).dueOn(), lowerDate.plusDays(1).toDate())));
                  } else
                  {
                     builder = builder.where(and(
                             ge(templateFor(DueOn.Data.class).dueOn(), lowerDate.toDate()),
                             le(templateFor(DueOn.Data.class).dueOn(), upperDate.plusDays(1).toDate())));
                  }
               }
            } else if (search.hasName("assignedTo"))
            {
               int count = 0;
               BooleanExpression expression = null;
               for (String user : search.getValue().split(","))
               {
                  UserEntity userEntity = null;
                  try
                  {
                     userEntity = module.unitOfWorkFactory().currentUnitOfWork().get(UserEntity.class, user);
                  } catch (NoSuchEntityException e)
                  {
                     // do nothing
                  }
                  if (userEntity == null)
                     continue;

                  if (count == 0)
                  {
                     expression = eq(templateFor(Assignable.Data.class).assignedTo(), userEntity);
                  } else
                  {
                     expression = or(expression, eq(templateFor(Assignable.Data.class).assignedTo(), userEntity));
                  }

                  count++;
               }

               if (expression != null)
                  builder = builder.where(expression);
            }
         }
         return builder;
      }


      protected List<SubQuery> extractSubQueries(String query)
      {
         List<SubQuery> subQueries = new ArrayList<SubQuery>();
         // TODO: Extract regular expression to resource file.
         String regExp = "((\\w+)\\:)?((\\\"([^\\\"]*)\\\")|([^\\s]+))"; // old "(?:\\w+\\:)?(?:\\\"[^\\\"]*?\\\")|(?:[^\\s]+)";
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
            String value = m.group(5);
            if (value == null)
               value = m.group(3);
            subQueries.add(new SubQuery(m.group(2), value));
         }

         if (subQueries.isEmpty())
         {
            if (query.length() > 0)
               subQueries.add(new SubQuery(null, query));
         }
         return subQueries;
      }

      class SubQuery
      {
         String name;

         String value;

         public SubQuery(String name, String value)
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

         public boolean hasName(String... names)
         {
            return name == null ? false : Arrays.asList(names).contains(name);
         }

      }
   }
}
