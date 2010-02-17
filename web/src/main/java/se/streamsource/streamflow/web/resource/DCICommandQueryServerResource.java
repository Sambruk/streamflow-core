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

package se.streamsource.streamflow.web.resource;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventFilter;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.infrastructure.web.TemplateUtil;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.InteractionContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

import javax.security.auth.Subject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Handle requests to command/query resources.
 * <p/>
 * GET:
 * If the request has ?command=name then show XHTML form
 * for invoking command with name "name".
 * If the request has ?query=name then , if parameters are
 * required then show XHTML form for it, otherwise perform query.
 * 
 * If neither query parameter is available, show listing of available
 * commands, queries, and subresources.
 * <p/>
 * POST: post of form must include query parameter "command" which is
 * the name of the method to invoke and the body is the JSON-serialized command value.
 * <p/>
 * PUT: put of form must include query parameter "command" which is
 * the name of the method to invoke and the body is the JSON-serialized command value.
 * <p/>
 * DELETE: resources implement this on their own.
 */
public class DCICommandQueryServerResource
      extends ServerResource
      implements TransactionVisitor
{
   protected
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   TransientBuilderFactory tbf;

   protected
   @Structure
   Qi4jSPI spi;

   @Structure
   protected ValueBuilderFactory vbf;

   @Structure
   protected ModuleSPI module;

   @Service
   EventSource source;
   public Iterable<TransactionEvents> transactions;

   public DCICommandQueryServerResource()
   {
      getVariants().addAll( Arrays.asList( new Variant( MediaType.TEXT_HTML ), new Variant( MediaType.APPLICATION_JSON ) ) );

      setNegotiated( true );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      String query = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "query" );
      if (query != null)
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( query ) );

         // GET on query -> perform query or show XHTML form
         try
         {
            Object resource = getRoot();

            // Find the resource first
            Reference relative = getRequest().getResourceRef().getRelativeRef();

            try
            {
               resource = getResource( resource, relative.getSegments() );

               return query( resource, query, variant );
            } catch (Exception e)
            {
               throw new ResourceException( e );
            }
         } finally
         {
            uow.discard();
         }
      }

      String command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
      if (command != null)
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( command ) );

         // GET on command -> show XHTML form
         try
         {
            Object resource = getRoot();

            // Find the resource first
            Reference relative = getRequest().getResourceRef().getRelativeRef();

            try
            {
               resource = getResource( resource, relative.getSegments() );

               return commandForm( resource, command );
            } catch (Exception e)
            {
               throw new ResourceException( e );
            }
         } finally
         {
            uow.discard();
         }
      }

      Parameter resourceParam = getRequest().getResourceRef().getQueryAsForm().getFirst( "resource" );
      if (resourceParam != null)
      {
         if (resourceParam.getValue() == null)
         {
            try
            {
               String template = TemplateUtil.getTemplate( "resources/selectresource.html", getClass() );
               /*
                                    String html = TemplateUtil.eval( template,
                                    "$name", method.getName(),
                                    "$content", form );
                           */

               // Show query form
               return new StringRepresentation( template, MediaType.TEXT_HTML );
            } catch (IOException e)
            {
               throw new ResourceException(e);
            }
         } else
         {
            Reference userRef = getRequest().getResourceRef().clone().addSegment( resourceParam.getValue() ).addSegment( "" );
            userRef.setQuery( "" );
            getResponse().redirectPermanent( userRef );

            return new EmptyRepresentation();
         }
      }

      // Show information about this resource
      UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getRequest().getResourceRef().getRemainingPart() ) );
      Object resource = getRoot();

      // Find the resource first
      Reference relative = getRequest().getResourceRef().getRelativeRef();

      try
      {
         resource = getResource( resource, relative.getSegments() );

         return resourceInfo( resource );
      } catch (Exception e)
      {
         throw new ResourceException( e );
      }
   }

   private RootContext getRoot()
   {
      InteractionContext interactionContext = new InteractionContext();
      interactionContext.playRoles( getActor(), Actor.class );
      interactionContext.playRoles( resolveRequestLocale(), Locale.class );
      interactionContext.playRoles(getRequest().getResourceRef(), Reference.class);
      return tbf.newTransientBuilder( RootContext.class ).use( interactionContext ).newInstance();
   }

   private Representation resourceInfo( Object resource ) throws IOException
   {
      MediaType responseType = getRequest().getClientInfo().getPreferredMediaType( Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_HTML ) );

      Method[] methods = resource.getClass().getInterfaces()[0].getMethods();
      final List<Method> queries = new ArrayList<Method>();
      final List<Method> commands = new ArrayList<Method>();
      final List<Method> subResources = new ArrayList<Method>();

      for (Method method : methods)
      {
         if (!method.getDeclaringClass().isAssignableFrom( Context.class ))
         {
            if (method.getAnnotation( SubContext.class ) != null)
            {
               subResources.add( method );
            } else if (method.getReturnType().equals( Void.TYPE ))
            {
               commands.add( method );
            } else if (!SubContexts.class.equals( method.getDeclaringClass() ))
            {
               queries.add( method );
            }
         }
      }

      if (responseType.equals( MediaType.TEXT_HTML ))
      {
         StringBuilder info = new StringBuilder();
         // Header
         info.append( "<html><body>" );

         StringBuilder queriesHtml = new StringBuilder();
         // List queries
         if (queries.size() > 0)
         {
            queriesHtml.append( "<h2>Queries</h2>" );
            queriesHtml.append( "<ul>" );
            for (Method query : queries)
            {
               queriesHtml.append( "<li><a rel=\"" )
                     .append( query.getName() )
                     .append( "\" href=\"?query=" )
                     .append( query.getName() )
                     .append( "\">" )
                     .append( query.getName() )
                     .append( "</a></li>" );
            }
            queriesHtml.append( "</ul>" );
         }

         // List commands
         StringBuilder commandsHtml = new StringBuilder();
         if (commands.size() > 0)
         {
            commandsHtml.append( "<h2>Commands</h2>" );
            commandsHtml.append( "<ul>" );
            for (Method command : commands)
            {
               commandsHtml.append( "<li><a rel=\"" )
                     .append( command.getName() )
                     .append( "\" href=\"?command=" )
                     .append( command.getName() )
                     .append( "\">" )
                     .append( command.getName() )
                     .append( "</a></li>" );
            }
            commandsHtml.append( "</ul>" );
         }

         // List subresources
         StringBuilder subResourcesHtml = new StringBuilder();
         if (resource instanceof SubContexts)
         {
            subResourcesHtml.append( "<a href=\"?resource\">Select resource</a>" );
         } else
         {

            if (subResources.size() > 0)
            {
               subResourcesHtml.append( "<h2>Sub-resources</h2>" );
               subResourcesHtml.append( "<ul>" );
               for (Method subResource : subResources)
               {
                  subResourcesHtml.append( "<li><a rel=\"" )
                        .append( subResource.getName() )
                        .append( "\" href=\"" )
                        .append( subResource.getName() )
                        .append( "/\">" )
                        .append( subResource.getName() )
                        .append( "</a></li>" );
               }
               subResourcesHtml.append( "</ul>" );
            }
         }

         String template = TemplateUtil.getTemplate( "resources/resource.html", getClass() );
         String html = TemplateUtil.eval( template,
               "$name", getRequest().getResourceRef().getRemainingPart(),
               "$queries", queriesHtml.toString(),
               "$commands", commandsHtml.toString(),
               "$subresources", subResourcesHtml.toString() );


         info.append( "</body></html>" );

         return new StringRepresentation( html, MediaType.TEXT_HTML );
      } else
      {
         // JSON
         return new WriterRepresentation(MediaType.APPLICATION_JSON)
         {
            @Override
            public void write( Writer writer ) throws IOException
            {
               try
               {
                  JSONWriter info = new JSONWriter(writer);
                  info = info.object();
                  if (queries.size() > 0)
                  {
                     JSONWriter jsonWriter = info.key( "queries" ).array();
                     for (Method query : queries)
                     {
                        jsonWriter.value( query.getName() );
                     }
                     jsonWriter.endArray();
                  }

                  if (commands.size() > 0)
                  {
                     JSONWriter jsonWriter = info.key( "commands" ).array();
                     for (Method command : commands)
                     {
                        jsonWriter.value( command.getName() );
                     }
                     jsonWriter.endArray();
                  }

                  if (subResources.size() > 0)
                  {
                     JSONWriter jsonWriter = info.key( "subresources" ).array();
                     for (Method subResource : subResources)
                     {
                        jsonWriter.value( subResource.getName() );
                     }
                     jsonWriter.endArray();
                  }
                  info.endObject();
               } catch (JSONException e)
               {
                  throw (IOException) new IOException("Could not write JSON").initCause( e );
               }
            }
         };
      }
   }

   private Representation query( Object resource, String query, Variant variant ) throws ResourceException
   {
      Method queryMethod = getQueryMethod( resource.getClass(), query );

      if (queryMethod.getParameterTypes().length == 0)
      {
         // Invoke query
         try
         {
            return returnRepresentation( queryMethod.invoke( resource ), variant );
         } catch (Exception e)
         {
            throw new ResourceException( e );
         }
      } else
      {
         Form form = getRequest().getResourceRef().getQueryAsForm();
         if (form.size() == 1)
         {
            // Show form
            String formHtml = "";

            Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) queryMethod.getParameterTypes()[0];
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

            for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
            {
               String propertyInput = propertyDescriptor.qualifiedName().name() +
                     ":<input type=\"text\" name=\"" +
                     propertyDescriptor.qualifiedName().name() + "\"/><br/>";

               formHtml += propertyInput;
            }

            try
            {
               String template = TemplateUtil.getTemplate( "resources/query.html", getClass() );
               String html = TemplateUtil.eval( template,
                     "$name", queryMethod.getName(),
                     "$content", formHtml );

               // Show query form
               return new StringRepresentation( html, MediaType.TEXT_HTML );
            } catch (IOException e)
            {
               throw new ResourceException( e );
            }
         } else
         {
            // Invoke form with parameters
            Object[] args = getQueryArguments( queryMethod );
            return returnRepresentation( invoke( resource, queryMethod, args ), variant );
         }
      }
   }

   private Method getQueryMethod( Class aClass, String query ) throws ResourceException
   {
      for (Method method : aClass.getMethods())
      {
         if (method.getName().equals( query ))
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   private Method getCommandMethod( Class aClass, String command ) throws ResourceException
   {
      for (Method method : aClass.getMethods())
      {
         if (method.getName().equals( command ))
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   private Representation commandForm( Object resource, String command ) throws ResourceException
   {
      // Show form
      String formHtml = "";

      Method commandMethod = getCommandMethod( resource.getClass(), command );

      if (commandMethod.getParameterTypes().length == 1)
      {
         Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) commandMethod.getParameterTypes()[0];
         ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

         for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
         {
            String value = getRequest().getResourceRef().getQueryAsForm().getFirstValue( propertyDescriptor.qualifiedName().name(), "" );
            String propertyInput = propertyDescriptor.qualifiedName().name() +
                  ":<input type=\"text\" name=\"" +
                  propertyDescriptor.qualifiedName().name() + "\" value=\""+value+"\"/><br/>";

            formHtml += propertyInput;
         }
      }

      try
      {
         String template = TemplateUtil.getTemplate( "resources/command.html", getClass() );
         String html = TemplateUtil.eval( template,
               "$name", commandMethod.getName(),
               "$content", formHtml );

         // Show query form
         return new StringRepresentation( html, MediaType.TEXT_HTML );
      } catch (IOException e)
      {
         throw new ResourceException( e );
      }
   }

   private Object getResource( Object resource, List<String> segments ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ResourceException
   {
      for (String segment : segments)
      {
         if (segment.equals( "." ) || segment.equals( "" ))
            return resource;

         if (resource instanceof SubContexts)
         {
            resource = ((SubContexts) resource).context( segment );
         } else
         {
            try
            {
               Method resourceMethod = resource.getClass().getMethod( segment );
               resource = resourceMethod.invoke( resource );
            } catch (NoSuchMethodException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
         }
      }

      return resource;
   }

   /**
    * protected String getCommand()
    * {
    * String command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
    * return command;
    * }
    */

   @Override
   final protected Representation delete( Variant variant ) throws ResourceException
   {
      return post( null, variant );
   }

   @Override
   final protected Representation post( Representation entity, Variant variant ) throws ResourceException
   {
      String command;
      if (getRequest().getMethod().getName().equals("DELETE"))
         command = "delete";
      else
         command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );

      if (command == null)
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No command given");

      int tries = 0;
      while (tries < 10)
      {
         tries++;

         UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( command ) );

         // POST on command -> perform command
         Object resource = getRoot();

         // Find the resource first
         Reference relative = getRequest().getResourceRef().getRelativeRef();

         try
         {
            resource = getResource( resource, relative.getSegments() );
         } catch (Exception e)
         {
            unitOfWork.discard();
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
         }

         try
         {
            source.registerListener( this );

            try
            {
               Method commandMethod = getCommandMethod( resource.getClass(), command );

               if (commandMethod.getParameterTypes().length == 0)
               {
                  // Invoke command
                  invoke( resource, commandMethod, null );
               } else
               {
                  // Invoke command with parameters
                  Object[] args = getCommandArguments( commandMethod );
                  invoke( resource, commandMethod, args );
               }

               // Create representation from events
               try
               {
                  unitOfWork.complete();

                  MediaType responseType = getRequest().getClientInfo().getPreferredMediaType( Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML ) );

                  Representation rep;
                  final EventFilter filter = new EventFilter( AllEventsSpecification.INSTANCE );
                  if (responseType == null || (responseType.equals( MediaType.TEXT_PLAIN )))
                  {
                     rep = new WriterRepresentation( MediaType.TEXT_PLAIN )
                     {
                        public void write( Writer writer ) throws IOException
                        {
                           writer.write( transactions.iterator().next().toJSON() );
                        }
                     };
                  } else if (responseType.equals( MediaType.TEXT_HTML ))
                  {
                     rep = new WriterRepresentation( MediaType.TEXT_HTML )
                     {
                        public void write( Writer writer ) throws IOException
                        {
                           String template = TemplateUtil.getTemplate( "resources/events.html", getClass() );
                           StringWriter string = new StringWriter();
                           for (DomainEvent event : filter.events( transactions ))
                           {
                              string.write( "<tr>" +
                                    "<td>" + event.usecase().get() + "</td>" +
                                    "<td>" + event.name().get() + "</td>" +
                                    "<td>" + event.on().get() + "</td>" +
                                    "<td>" + event.entity().get() + "</td>" +
                                    "<td>" + event.parameters().get() + "</td>" +
                                    "<td>" + event.by().get() + "</td></tr>" );
                           }

                           writer.write( TemplateUtil.eval( template, "$events", string.toString() ) );
                        }
                     };
                  } else
                  {
                     rep = new WriterRepresentation( MediaType.APPLICATION_JSON )
                     {
                        public void write( Writer writer ) throws IOException
                        {
                           writer.write( transactions.iterator().next().toJSON() );
                        }
                     };
                  }

                  return rep;
               } catch (ConcurrentEntityModificationException e)
               {
                  // Try again
                  throw e;
               } catch (Exception ex)
               {
                  throw new ResourceException( Status.SERVER_ERROR_INTERNAL, ex );
               }

            } finally
            {
               source.unregisterListener( this );
            }
         } catch (ConcurrentEntityModificationException e)
         {
            // Try again
            unitOfWork.discard();
         }
      }

      throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, "Could not perform command" );
   }

   private Representation returnRepresentation( Object returnValue, Variant variant ) throws ResourceException
   {
      if (returnValue != null)
      {
         if (returnValue instanceof ValueComposite)
         {
            if (variant.getMediaType().equals( MediaType.APPLICATION_JSON ))
            {
               return new StringRepresentation( ((Value) returnValue).toJSON(), MediaType.APPLICATION_JSON );
            } else if (variant.getMediaType().equals( MediaType.TEXT_HTML ))
            {
               if (returnValue instanceof LinksValue)
               {
                  try
                  {
                     String template = TemplateUtil.getTemplate( "resources/links.html", DCICommandQueryServerResource.class );

                     LinksValue links = (LinksValue) returnValue;
                     StringBuilder linksHtml = new StringBuilder();
                     for (LinkValue linkValue : links.links().get())
                     {
                        linksHtml.append( "<li><a " );

                        if (linkValue.rel().get() != null)
                        {
                           linksHtml.append( "rel=\"" ).
                                 append( linkValue.rel().get() ).
                                 append( "\" " );
                        }
                        linksHtml.append( "href=\"" ).
                              append( linkValue.href().get() ).
                              append( "\">" ).
                              append( linkValue.text().get() ).
                              append( "</a>" );
                        linksHtml.append( "</li>" );
                     }

                     String content = TemplateUtil.eval( template,
                           "$content", linksHtml.toString(),
                           "$title", getRequest().getResourceRef().getRemainingPart() );
                     return new StringRepresentation( content, MediaType.TEXT_HTML );
                  } catch (IOException e)
                  {
                     throw new ResourceException( e );
                  }

               } else
               {
                  try
                  {
                     String template = TemplateUtil.getTemplate( "resources/value.html", DCICommandQueryServerResource.class );
                     String content = TemplateUtil.eval( template, "$content", ((Value) returnValue).toJSON() );
                     return new StringRepresentation( content, MediaType.TEXT_HTML );
                  } catch (IOException e)
                  {
                     throw new ResourceException( e );
                  }
               }
            } else
            {
               return new EmptyRepresentation();
            }
         } else if (returnValue instanceof EntityReference)
         {
            // Redirect to same URL + id;
            EntityReference ref = (EntityReference) returnValue;
            Reference userRef = getRequest().getResourceRef().clone().addSegment( ref.identity() ).addSegment( "" );
            userRef.setQuery( "" );
            getResponse().redirectPermanent( userRef );

            return new EmptyRepresentation();
         } else
         {
            LoggerFactory.getLogger( getClass().getName() ).warn( "Unknown result type:" + returnValue.getClass().getName() );
            return new EmptyRepresentation();
         }
      } else
         return new EmptyRepresentation();
   }

   @Override
   final protected Representation put( Representation representation, Variant variant ) throws ResourceException
   {
      return post( representation, variant );
   }

   public boolean visit( TransactionEvents transaction )
   {
      transactions = Collections.singletonList( transaction );

      return true;
   }

   private Method getResourceMethod( String operation, Object resource )
         throws ResourceException
   {
      for (Method method : resource.getClass().getInterfaces()[0].getMethods())
      {
         if (method.getName().equals( operation ))
         {
            return method;
         }
      }
      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   /**
    * A query method has the following attributes
    * - Returns a Value
    *
    * @param method
    * @return
    */
   private boolean isQueryMethod( Method method )
   {
      return Value.class.isAssignableFrom( method.getReturnType() );
   }

   /**
    * A command method has the following attributes:
    * - Returns void
    * - Has a Value as the first parameter
    *
    * @param method
    * @return
    */
   private boolean isCommandMethod( Method method )
   {
      return method.getReturnType().equals( Void.TYPE ) && method.getParameterTypes().length > 0 && Value.class.isAssignableFrom( method.getParameterTypes()[0] );
   }

   private Object[] getCommandArguments( Method method ) throws ResourceException
   {
      if (method.getParameterTypes().length > 0)
      {
         Object[] args = new Object[method.getParameterTypes().length];

         Class<? extends Value> commandType = (Class<? extends Value>) method.getParameterTypes()[0];

         if (getRequest().getEntity().getMediaType().equals( MediaType.APPLICATION_JSON ))
         {
            String json = getRequest().getEntityAsText();

            Object command = vbf.newValueFromJSON( commandType, json );
            args[0] = command;
            return args;
         } else if (getRequest().getEntity().getMediaType().equals( MediaType.TEXT_PLAIN ))
         {
            String text = getRequest().getEntityAsText();
            if (text == null)
               throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );
            args[0] = text;
            return args;
         } else if (getRequest().getEntity().getMediaType().equals( (MediaType.APPLICATION_WWW_FORM) ))
         {
            Form asForm = getRequest().getEntityAsForm();
            Class<?> valueType = method.getParameterTypes()[0];
            args[0] = getValueFromForm( (Class<ValueComposite>) valueType, asForm );
            return args;
         } else
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format" );
      } else
      {
         return new Object[0];
      }
   }

   private Object[] getQueryArguments( Method method )
         throws ResourceException
   {
      Object[] args = new Object[method.getParameterTypes().length];
      int idx = 0;

      Representation representation = getRequest().getEntity();
      if (representation != null && MediaType.APPLICATION_JSON.equals( representation.getMediaType() ))
      {
         Class<?> valueType = method.getParameterTypes()[0];
         Object requestValue = vbf.newValueFromJSON( valueType, getRequest().getEntityAsText() );
         args[0] = requestValue;
      } else
      {
         Form asForm = getRequest().getResourceRef().getQueryAsForm();
         if (args.length == 1 && ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0] ))
         {
            Class<?> valueType = method.getParameterTypes()[0];

            args[0] = getValueFromForm( (Class<ValueComposite>) valueType, asForm );
         } else
         {
            for (Annotation[] annotations : method.getParameterAnnotations())
            {
               Name name = Annotations.getAnnotationOfType( annotations, Name.class );
               Object arg = asForm.getFirstValue( name.value() );

               // Parameter conversion
               if (method.getParameterTypes()[idx].equals( EntityReference.class ))
               {
                  arg = EntityReference.parseEntityReference( arg.toString() );
               }

               args[idx++] = arg;
            }
         }
      }

      return args;
   }

   private Object invoke( final Object resource, final Method method, final Object[] args )
         throws ResourceException
   {
      try
      {
         Subject subject = new Subject();
         subject.getPrincipals().addAll( getRequest().getClientInfo().getPrincipals() );
         try
         {
            Object returnValue = Subject.doAs( subject, new PrivilegedExceptionAction()
            {
               public Object run() throws Exception
               {
                  return method.invoke( resource, args );
               }
            } );

            return returnValue;
         } catch (PrivilegedActionException e)
         {
            throw e.getCause();
         }
      } catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof ResourceException)
         {
            throw (ResourceException) e.getTargetException();
         } else if (e.getTargetException() instanceof AccessControlException)
         {
            // Operation not allowed - return 403
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
         }

         getResponse().setEntity( new ObjectRepresentation<InvocationTargetException>( e ) );

         throw new ResourceException( e.getTargetException() );
      } catch (Throwable e)
      {
         throw new ResourceException( e );
      }
   }

   private ValueComposite getValueFromForm( Class<ValueComposite> valueType, final Form asForm )
   {
      ValueBuilder<ValueComposite> builder = vbf.newValueBuilder( valueType );
      final ValueDescriptor descriptor = spi.getValueDescriptor( builder.prototype() );
      builder.withState( new StateHolder()
      {
         public <T> Property<T> getProperty( QualifiedName name )
         {
            return null;
         }

         public <T> Property<T> getProperty( Method propertyMethod )
         {
            return null;
         }

         public void visitProperties( StateVisitor visitor )
         {
            for (PropertyType propertyType : descriptor.valueType().types())
            {
               Parameter param = asForm.getFirst( propertyType.qualifiedName().name() );
               if (param != null)
               {
                  String value = param.getValue();
                  if (value == null)
                     value = "";
                  try
                  {
                     Object valueObject = propertyType.type().fromQueryParameter( value, module );
                     visitor.visitProperty( propertyType.qualifiedName(), valueObject );
                  } catch (JSONException e)
                  {
                     throw new IllegalArgumentException( "Query parameter has invalid JSON format", e );
                  }
               }
            }
         }
      } );
      return builder.newInstance();
   }

   private Actor getActor()
   {
      List<Principal> principals = getRequest().getClientInfo().getPrincipals();
      if (!principals.isEmpty())
      {
         String userName = principals.get( 0 ).getName();
         return uowf.currentUnitOfWork().get( Actor.class, userName );
      } else
         return null;
   }

   private Locale resolveRequestLocale()
   {
      Language language = getRequest().getClientInfo().getAcceptedLanguages()
            .get( 0 ).getMetadata();
      String[] localeStr = language.getName().split( "_" );

      Locale locale;
      switch (localeStr.length)
      {
         case 1:
            locale = new Locale( localeStr[0] );
            break;
         case 2:
            locale = new Locale( localeStr[0], localeStr[1] );
            break;
         case 3:
            locale = new Locale( localeStr[0], localeStr[1], localeStr[2] );
            break;
         default:
            locale = Locale.getDefault();
      }
      return locale;
   }
}