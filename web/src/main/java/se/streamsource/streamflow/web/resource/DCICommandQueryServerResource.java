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
import se.streamsource.streamflow.web.context.RootContext;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.infrastructure.web.TemplateUtil;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.IndexContext;
import se.streamsource.streamflow.web.infrastructure.web.context.InteractionContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContext;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

import javax.security.auth.Subject;
import java.io.IOException;
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
import java.util.List;
import java.util.Locale;

/**
 * Handle requests to command/query resources.
 * <p/>
 * GET:
 * If the URL has does not end with "/" then show XHTML form
 * for invoking either query orcommand with name "name". If the query
 * does not need a form, then invoke it directly.
 * <p/>
 * If the URL ends with "/", show listing of available
 * commands, queries, and subresources.
 * <p/>
 * POST/PUT: post of form must be to an URL which does not end with "/".
 * The last segment is the name of the method to invoke and result is an EmptyRepresentation.
 * <p/>
 * DELETE: this translates into a call on DeleteContext.delete() on the given resource.
 */
public class DCICommandQueryServerResource
      extends ServerResource
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

   public DCICommandQueryServerResource()
   {
      getVariants().addAll( Arrays.asList( new Variant( MediaType.TEXT_HTML ), new Variant( MediaType.APPLICATION_JSON ) ) );

      setNegotiated( true );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      List<String> segments = getRequest().getResourceRef().getRelativeRef().getSegments();

      String lastSegment = segments.get( segments.size() - 1 );

      if (lastSegment.equals( "" ) || lastSegment.equals( "." ))
      {
         // Show information about this resource
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getRequest().getResourceRef().getRemainingPart() ) );
         try
         {
            Object resource = getRoot();

            // Find the resource first
            try
            {
               resource = getResource( resource, segments );
               getResponse().getAttributes().put( "segments", segments );

               return resourceInfo( resource );
            } catch (Exception e)
            {
               throw new ResourceException( e );
            }
         } catch (ResourceException e)
         {
            uow.discard();
         }
      }

      if (lastSegment.equals("context" ))
      {
         String resource = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "resource" );
         if (resource == null)
         {
            try
            {
               String template = TemplateUtil.getTemplate( "resources/selectresource.html", getClass() );

               // Show resource selection form
               return new StringRepresentation( template, MediaType.TEXT_HTML );
            } catch (IOException e)
            {
               throw new ResourceException( e );
            }
         } else
         {
            Reference userRef = getRequest().getResourceRef().getParentRef().clone().addSegment( resource ).addSegment( "" );
            getResponse().redirectPermanent( userRef );

            return new EmptyRepresentation();
         }
      }

      UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( lastSegment ) );

      // GET on operation
      try
      {
         Object resource = getRoot();

         // Find the resource first
         try
         {
            resource = getResource( resource, segments );
            getResponse().getAttributes().put( "segments", segments );

            Method method = getMethod( resource, lastSegment );

            if (isCommandMethod( method ))
            {
               return commandForm( resource, lastSegment );
            } else
            {
               return query( resource, lastSegment, variant );
            }

         } catch (ResourceException e)
         {
            throw e;
         }catch (Exception e)
         {
            throw new ResourceException( e );
         }
      } finally
      {
         uow.discard();
      }
   }

   private Method getMethod( Object resource, String lastSegment ) throws ResourceException
   {
      for (Method method : resource.getClass().getMethods())
      {
         if (method.getName().equals( lastSegment ))
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   private RootContext getRoot()
   {
      InteractionContext interactionContext = new InteractionContext();
      interactionContext.playRoles( getActor(), Actor.class );
      interactionContext.playRoles( resolveRequestLocale(), Locale.class );
      interactionContext.playRoles( getRequest().getResourceRef(), Reference.class );
      return tbf.newTransientBuilder( RootContext.class ).use( interactionContext ).newInstance();
   }

   private Representation resourceInfo( final Object resource ) throws IOException
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
            if (method.getAnnotation( SubContext.class ) != null || SubContexts.class.equals( method.getDeclaringClass() ))
            {
               subResources.add( method );
            } else if (method.getReturnType().equals( Void.TYPE ))
            {
               commands.add( method );
            } else
            {
               queries.add( method );
            }
         }
      }

      final Value index = resource instanceof IndexContext ? ((IndexContext)resource).index() : null;

      // JSON
      return new WriterRepresentation( MediaType.APPLICATION_JSON )
      {
         @Override
         public void write( Writer writer ) throws IOException
         {
            try
            {
               JSONWriter info = new JSONWriter( writer );
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
                  JSONWriter jsonWriter = info.key( "contexts" ).array();
                  for (Method subResource : subResources)
                  {
                     if (subResource.getDeclaringClass().equals(SubContexts.class))
                        jsonWriter.value( subResource.getName() );
                     else
                        jsonWriter.value( subResource.getName()+"/" );
                  }
                  jsonWriter.endArray();
               }

               if (index != null)
               {
                  JSONWriter jsonWriter = info.key( "index" );

                  ValueDescriptor valueDescriptor = spi.getValueDescriptor( (ValueComposite) index );
                  valueDescriptor.valueType().toJSON( index, jsonWriter );
               }

               info.endObject();

            } catch (JSONException e)
            {
               throw (IOException) new IOException( "Could not write JSON" ).initCause( e );
            }
         }
      };
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
         if (form.size() == 0)
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
                  propertyDescriptor.qualifiedName().name() + "\" value=\"" + value + "\"/><br/>";

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
      for (int i = 0; i < segments.size()-1; i++)
      {
         String segment = segments.get( i );

         if (resource instanceof SubContexts)
         {
            resource = ((SubContexts) resource).context( segment );
            segments.set( i, "context" );
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
      getRequest().getResourceRef().addSegment( "delete" );
      
      return post( null, variant );
   }

   @Override
   final protected Representation post( Representation entity, Variant variant ) throws ResourceException
   {
      // POST on command -> perform command

      List<String> segments = getRequest().getResourceRef().getRelativeRef().getSegments();
      String lastSegment = segments.get( segments.size() - 1 );
      getResponse().getAttributes().put( "segments", segments );

      int tries = 0;
      while (tries < 10)
      {
         tries++;

         UnitOfWork unitOfWork = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( lastSegment ) );

         // Find the resource first
         try
         {
            Object resource = getRoot();

            resource = getResource( resource, segments );
            getResponse().getAttributes().put( "segments", segments );

            Method method = getMethod( resource, lastSegment );

            if (method.getParameterTypes().length == 0)
            {
               // Invoke command
               invoke( resource, method, null );
            } else
            {
               // Invoke command with parameters
               Object[] args = getCommandArguments( method );
               invoke( resource, method, args );
            }

            unitOfWork.complete();

            setStatus( Status.INFO_CONTINUE );

            return new EmptyRepresentation();

         } catch (ConcurrentEntityModificationException e)
         {
            // Try again
            unitOfWork.discard();
         } catch (Exception e)
         {
            unitOfWork.discard();
            LoggerFactory.getLogger( getClass() ).warn( "Could not complete UnitOfWork", e );
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, "Could not perform command" );
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
      if (!(method.getReturnType().equals( Void.TYPE ) || Context.class.isAssignableFrom( method.getReturnType())))
         return false;

      if (method.getParameterTypes().length == 0 || (method.getParameterTypes().length == 1 && Value.class.isAssignableFrom( method.getParameterTypes()[0])))
         return true;
      
      return false;
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