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
package se.streamsource.streamflow.web.domain.entity.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.note.NotesTimeLineEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
@Mixins(SearchCaseQueries.Mixin.class)
public interface
      SearchCaseQueries
{
   Query<Case> search( String query, boolean includeNotesInSearch );

   abstract class Mixin
         implements SearchCaseQueries
   {
      @Structure
      Module module;

      @This
      UserAuthentication.Data user;

      public Query<Case> search( String query, boolean includeNotesInSearch )
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
                  queryBuilder.append( " status:(" );
                  int count = 0;
                  for (String status : Arrays.asList(search.getValue().split(",")))
                  {
                     if (count == 0)
                     {
                        queryBuilder.append(status);
                     } else
                     {
                        queryBuilder.append(" OR ").append(status);
                     }

                     count++;
                  }
                  queryBuilder.append(")");
                  
               } else if (search.hasName( "label" ))
               {
                  List<LabelEntity> labels = new ArrayList<LabelEntity>();
                  for (String label : search.getValue().split(","))
                  {
                     try
                     {
                        labels.add( module.unitOfWorkFactory().currentUnitOfWork().get( LabelEntity.class, label.replace( "\\", "" ) ) );

                     } catch (NoSuchEntityException e)
                     {
                        StringBuilder labelQueryBuilder = new StringBuilder(
                              "type:se.streamsource.streamflow.web.domain.entity.label.LabelEntity" );
                        labelQueryBuilder.append( " (description:" ).append( label );
                        labelQueryBuilder.append( " OR ntext:" ).append( label ).append( ")" );

                        Iterables.addAll( labels, module.queryBuilderFactory()
                              .newNamedQuery( LabelEntity.class, uow, "solrquery" )
                              .setVariable( "query", labelQueryBuilder.toString() ) );
                     }
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
                           .newQuery( Collections.<Case>emptyList() );
                  }
               } else if (search.hasName( "caseType" ))
               {
                  List<CaseTypeEntity> caseTypes = new ArrayList<CaseTypeEntity>();
                  for (String caseType : search.getValue().split(","))
                  {
                     try
                     {
                        caseTypes.add( module.unitOfWorkFactory().currentUnitOfWork().get( CaseTypeEntity.class, caseType.replace( "\\", "" ) ) );
                     } catch ( NoSuchEntityException e )
                     {
                        StringBuilder caseTypeQueryBuilder = new StringBuilder(
                              "type:se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity" );
                        caseTypeQueryBuilder.append( " (description:" ).append( caseType );
                        caseTypeQueryBuilder.append( " OR ntext:" ).append( caseType ).append( ")" );

                        Iterables.addAll( caseTypes,
                              module.queryBuilderFactory().newNamedQuery( CaseTypeEntity.class, uow, "solrquery" )
                                    .setVariable( "query", caseTypeQueryBuilder.toString() ) );
                     }
                  }
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
                  List<ProjectEntity> projects = new ArrayList<ProjectEntity>();
                  for (String project : search.getValue().split(","))
                  {
                     try
                     {
                        projects.add( module.unitOfWorkFactory().currentUnitOfWork().get( ProjectEntity.class, project.replace( "\\", "" ) ) );
                     } catch ( NoSuchEntityException e )
                     {
                        StringBuilder projectQueryBuilder = new StringBuilder(
                              "type:se.streamsource.streamflow.web.domain.entity.project.ProjectEntity" );
                        projectQueryBuilder.append( " ( description:" ).append( project );
                        projectQueryBuilder.append( " OR ntext:" ).append( project ).append( ")" );

                        Iterables.addAll( projects,
                              module.queryBuilderFactory().newNamedQuery( ProjectEntity.class, uow, "solrquery" )
                                    .setVariable( "query", projectQueryBuilder.toString() ) );
                     }
                  }
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
               } else if (search.hasName( "notes" ))
               {
                  List<NotesTimeLineEntity> notes = new ArrayList<NotesTimeLineEntity>();
                  for (String note : search.getValue().split(","))
                  {

                        StringBuilder projectQueryBuilder = new StringBuilder(
                              "type:se.streamsource.streamflow.web.domain.entity.note.NotesTimeLineEntity" );
                        projectQueryBuilder.append( " ( note:" ).append( note ).append( ")" );

                        Iterables.addAll( notes,
                              module.queryBuilderFactory().newNamedQuery( NotesTimeLineEntity.class, uow, "solrquery" )
                                    .setVariable( "query", projectQueryBuilder.toString() ) );
                  }
                  if (notes.iterator().hasNext())
                  {
                     queryBuilder.append( " notes:(" );
                     int count = 0;
                     for (NotesTimeLineEntity note : notes)
                     {
                        if (count == 0)
                        {
                           queryBuilder.append( note.identity().get() );
                        } else
                        {
                           queryBuilder.append( " OR " ).append( note.identity().get() );
                        }

                        count++;
                     }
                     queryBuilder.append( ")" );
                  } else
                  {
                     // dismiss search - no notes for given name exists. Return empty search
                     return module.queryBuilderFactory().newQueryBuilder( Case.class ).newQuery( Collections.<Case>emptyList() );
                  }
               } else if (search.hasName( "createdBy" ))
               {
                  List<UserEntity> users = new ArrayList<UserEntity>();
                  String userName = user.userName().get();
                  for (String user : search.getValue().split(","))
                  {
                     try
                     {
                        users.add( module.unitOfWorkFactory().currentUnitOfWork().get( UserEntity.class, user.replace( "\\", "" ) ) );
                     } catch ( NoSuchEntityException e )
                     {
                        StringBuilder creatorQueryBuilder = new StringBuilder( "type:se.streamsource.streamflow.web.domain.entity.user.UserEntity" );
                        creatorQueryBuilder.append( " (id:" ).append( getUserInSearch( user, userName ) )
                              .append( " OR " ).append( " description:" ).append( getUserInSearch( user, userName ) )
                              .append( " OR " ).append( " ntext:" ).append( getUserInSearch( user, userName ) ).append( ")" );

                        Iterables.addAll( users, module.queryBuilderFactory()
                              .newNamedQuery( UserEntity.class, uow, "solrquery" ).setVariable( "query", creatorQueryBuilder.toString() ) );
                     }
                  }
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

               } else if (search.hasName( "createdOn" ) || search.hasName( "dueOn" ) )
               {
                  buildDateQuery( queryBuilder, search );
               } else if (search.hasName( "assignedTo" ))
               {
                  List<UserEntity> users = new ArrayList<UserEntity>();
                  String userName = user.userName().get();
                  for (String user : search.getValue().split(","))
                  {
                     try
                     {
                        users.add( module.unitOfWorkFactory().currentUnitOfWork().get( UserEntity.class, user.replace( "\\", "" ) ) );
                     } catch ( NoSuchEntityException e )
                     {
                        StringBuilder creatorQueryBuilder = new StringBuilder(
                              "type:se.streamsource.streamflow.web.domain.entity.user.UserEntity" );
                        creatorQueryBuilder.append( " (id:" ).append( getUserInSearch( user, userName ) ).append( " OR " )
                              .append( " description:" ).append( getUserInSearch( user, userName ) ).append( " OR " )
                              .append( " ntext:" ).append( getUserInSearch( user, userName ) ).append( ")" );

                        Iterables.addAll(
                              users,
                              module.queryBuilderFactory().newNamedQuery( UserEntity.class, uow, "solrquery" )
                                    .setVariable( "query", creatorQueryBuilder.toString() ) );
                     }
                  }
                  int count = 0;
                  for (UserEntity userItem : users)
                  {
                     if (count == 0)
                     {
                        queryBuilder.append( " assignedTo:(" ).append( userItem.identity().get() );
                     } else
                     {
                        queryBuilder.append( " OR " ).append( userItem.identity().get() );
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
                  {
                     queryBuilder.append( " " );
                  }

                  if( includeNotesInSearch )
                  {
                     List<NotesTimeLineEntity> notes = new ArrayList<NotesTimeLineEntity>();


                     StringBuilder notesQueryBuilder = new StringBuilder(
                           "type:se.streamsource.streamflow.web.domain.entity.note.NotesTimeLineEntity" );
                     notesQueryBuilder.append( " ( note:" ).append( search.getValue() ).append( ")" );

                     Iterables.addAll( notes,
                           module.queryBuilderFactory().newNamedQuery( NotesTimeLineEntity.class, uow, "solrquery" )
                                 .setVariable( "query", notesQueryBuilder.toString() ) );

                     if (notes.iterator().hasNext())
                     {
                        queryBuilder.append( " ( notes:(" );
                        int count = 0;
                        for (NotesTimeLineEntity note : notes)
                        {
                           if (count == 0)
                           {
                              queryBuilder.append( note.identity().get() );
                           } else
                           {
                              queryBuilder.append( " OR " ).append( note.identity().get() );
                           }

                           count++;
                        }
                        queryBuilder.append( ") OR text:(" + search.getValue() + ") )" );
                     } else
                     {

                        queryBuilder.append( search.getValue() );
                     }
                  } else
                  {

                     queryBuilder.append( search.getValue() );
                  }
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
                     if( user.isAdministrator() )
                        return true;
                     else
                        return !((Removable.Data)item).removed().get() && item.hasPermission( user.userName().get(), PermissionType.read.name() );
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
            // Substring and remove escape chars
            searchDateFrom = searchDateFrom.substring( 0, searchDateFrom.indexOf( "-" ) ).replace( "\\", "" );
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
       * @param 
       * @return
       */
      protected String getUserInSearch( String userName, String user )
      {
         if (UserSearchKeyword.ME.toString().equalsIgnoreCase( userName ))
         {
            return user;
         } else
         {
            return "\"" + userName + "\"";
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
            return escapeLuceneCharacters( value );
         }

         public String getQuotedValue()
         {
            return "\""+escapeLuceneCharacters( value )+"\"";
         }

         public boolean hasName( String... names )
         {
            return name == null ? false : Arrays.asList( names ).contains( name );
         }

         private String escapeLuceneCharacters( String query )
         {
            // DO NOT escape wildcard characters!!  "*", "?"
            List<String> specialChars =
                  Arrays.asList( "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", ":" );

            for ( String str : specialChars )
            {
               if( query.contains( str ) )
               {
                  char[] escaped = new char[str.length()*2];
                  int count = 0;
                  for( Character c : str.toCharArray() )
                  {
                     escaped[count] = '\\';
                     count++;
                     escaped[count] = c;
                     count++;
                  }
                  query = query.replace( str, new String( escaped ) );
               }
            }
            return query;
         }
      }
   }
}
