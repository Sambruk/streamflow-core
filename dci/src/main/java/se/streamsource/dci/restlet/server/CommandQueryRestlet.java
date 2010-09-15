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

package se.streamsource.dci.restlet.server;

import org.json.JSONException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.ContextValue;

import javax.security.auth.Subject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URLDecoder;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle requests to command/query resources.
 * <p/>
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
public class CommandQueryRestlet
      extends Restlet
{
   private
   @Structure
   UnitOfWorkFactory uowf;

   private
   @Structure
   Qi4jSPI spi;

   @Structure
   private ValueBuilderFactory vbf;

   @Structure
   private ModuleSPI module;

   @Service
   private InteractionConstraints constraints;

   @Service
   private ResponseWriterFactory responseWriterFactory;

   @Service
   private RootContextFactory rootContextFactory;

   @Service
   private CommandResult commandResult;

   private Map<Class, List<Method>> contextClassMethods = new ConcurrentHashMap<Class, List<Method>>();

   Logger logger = LoggerFactory.getLogger( getClass().getName() );

   public CommandQueryRestlet(@Uses org.restlet.Context context)
   {
      super(context);
   }

   @Override
   public void handle( Request request, Response response )
   {
      super.handle( request, response );

      // Find roleMap
      try
      {
         Reference ref = request.getResourceRef();
         List<String> segments = ref.getScheme().equals( "riap" ) ? ref.getRelativeRef( new Reference( "riap://application/" ) ).getSegments() : ref.getRelativeRef().getSegments();

         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getUsecaseName( request ) ) );

         RoleMap roleMap = new RoleMap();
         uow.metaInfo().set( roleMap );
         initContext( request, roleMap );

         // Find the context first
         Object context = null;
         try
         {
            context = getContext( rootContextFactory.getRoot( roleMap ), segments );
            roleMap = uow.metaInfo().get( RoleMap.class ); // Get current roleMap for this context
         } catch (Exception e)
         {
            uow.discard();
            logger.error( e.getMessage() );
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
         }

         if (context == null)
         {
            uow.discard();
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
            return;
         }

         // What HTTP method do we want to do
         org.restlet.data.Method method = request.getMethod();
         if (method.equals( org.restlet.data.Method.GET ))
         {
            get( request, response, context, roleMap, segments );
         } else if (method.equals( org.restlet.data.Method.DELETE ))
         {
            delete( request, response, context, roleMap, segments );
         } else if (method.equals( org.restlet.data.Method.POST) || method.equals( org.restlet.data.Method.PUT) )
         {
            // When doing POST/PUT we should try several times if there is a conflict when committing
            int retries = 0;
            while (retries < 10)
            {
               try
               {
                  post( request, response, context, roleMap, segments );
                  return;
               } catch (UnitOfWorkCompletionException e)
               {
                  // Retry
                  uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getUsecaseName( request ) ) );

                  roleMap = new RoleMap();
                  initContext( request, roleMap );

                  // Find the roleMap again in the new UoW
                  context = null;
                  try
                  {
                     context = getContext( rootContextFactory.getRoot( roleMap ), segments );
                  } catch (Exception ex)
                  {
                     uow.discard();
                     logger.error( e.getMessage() );
                     response.setStatus( Status.SERVER_ERROR_INTERNAL );
                     return;
                  }
               }
            }

            // Give up
            response.setStatus( Status.CLIENT_ERROR_CONFLICT );
            response.setEntity( new StringRepresentation( "Could not complete command due to conflicts" ) );

         } /*else if (request.getMethod().equals( org.restlet.data.Method.HEAD ))
         { TODO
            head( request, response, roleMap, interactionContext );
         }*/
      } finally
      {
         request.release(); // Release request explicitly to avoid Tomcat bug
      }
   }

   private String getUsecaseName( Request request )
   {
      if (request.getMethod().equals( org.restlet.data.Method.DELETE ))
         return "delete";
      else
         return request.getResourceRef().getLastSegment();
   }

   private void get( Request request, Response response, Object context, RoleMap roleMap, List<String> segments )
   {
      try
      {
         String lastSegment = segments.get( segments.size() - 1 );

         if (lastSegment.equals( "" ) || lastSegment.equals( "." ))
         {
            // Show context info
            ResponseWriter writer = responseWriterFactory.createWriter( segments, ContextValue.class, roleMap, getVariant( request ) );

            contextInfo( request, response, context, roleMap, writer );
         } else if (lastSegment.equals( "context" ))
         {
            String contextId = request.getResourceRef().getQueryAsForm().getFirstValue( "context" );
            if (contextId == null)
            {
               ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, Context.class, roleMap, getVariant( request ) );

               responseWriter.write( null, request, response );
            } else
            {
               // Redirect to the subcontext
               Reference userRef = request.getResourceRef().getParentRef().clone().addSegment( contextId ).addSegment( "" );
               response.redirectPermanent( userRef );
            }
         } else
         {
            Method method = getInteractionMethod( context, lastSegment );
            if (!constraints.isValid( method, roleMap ))
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Interaction not valid" );

            // Check whether it's a command or query
            if (isCommandMethod( method ))
            {
               // The method is shown in the form, so let's change it to POST
               request.setMethod( org.restlet.data.Method.POST );
               ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, roleMap, getVariant( request ) );

               Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];
               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

               responseWriter.write( valueDescriptor, request, response );
            } else
            {
               query( request, response, context, segments, roleMap, method );
            }
         }
      } catch (ResourceException ex)
      {
         logger.error( ex.getMessage(), ex );
         response.setStatus( ex.getStatus() );
         response.setEntity( new StringRepresentation(ex.getMessage()) );
      } catch (Throwable ex)
      {
         logger.error( ex.getMessage(), ex );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      } finally
      {
         module.unitOfWorkFactory().currentUnitOfWork().discard();
      }
   }

   private void post( Request request, Response response, Object context, RoleMap roleMap, List<String> segments )
         throws UnitOfWorkCompletionException
   {
      // POST on command -> perform interaction
      String lastSegment = segments.get( segments.size() - 1 );

      UnitOfWork unitOfWork = uowf.currentUnitOfWork();

      try
      {
         Method method = getInteractionMethod( context, lastSegment );

         if (method.getParameterTypes().length == 0)
         {
            // Invoke command
            invoke( request, context, method, null );
         } else
         {
            Class valueType = method.getParameterTypes()[0];
            // Invoke command with parameters
            if (request.getEntity().getAvailableSize() == 0 && valueType != Response.class )
            {
               if (request.getResourceRef().hasQuery())
               {
                  // Get POST parameters from the URL query parameters
                  Object[] args = getQueryArguments( request, response, method );
                  invoke(request, context, method, args);
               } else
               {
                  // No entity and no query was used -> show input form
                  ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, roleMap, getVariant( request ) );

                  ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
                  responseWriter.write( valueDescriptor, request, response );

                  unitOfWork.discard();

                  return;
               }
            } else
            {
               Object[] args = getCommandArguments( request, response, method );
               invoke( request, context, method, args );
            }
         }

         unitOfWork.complete();

         // Show some result
         Object result = commandResult.getResult();

         ResponseWriter responseWriter;
         Variant variant = getVariant( request );

         try
         {
            responseWriter = responseWriterFactory.createWriter( segments, result == null ? null : result.getClass(), roleMap, variant );
         } catch (Exception e)
         {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            return;
         }
         responseWriter.write( result, request, response );

      } catch (UnitOfWorkCompletionException e)
      {
         unitOfWork.discard();
         throw e; // Let the wrapping code handle retries
      } catch (Exception e)
      {
         unitOfWork.discard();
         LoggerFactory.getLogger( getClass() ).warn( "Could not complete UnitOfWork " + unitOfWork.usecase().name(), e );
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         if ( e instanceof ResourceException)
         {
            response.setStatus(((ResourceException) e).getStatus());
         } else
         {
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
         }
      }
   }

   private void delete( Request request, Response response, Object context, RoleMap roleMap, List<String> segments  )
   {
      DeleteContext deleteContext = (DeleteContext) context;

      try
      {
         deleteContext.delete();

         uowf.currentUnitOfWork().complete();

         // Show some result
         Object result = commandResult.getResult();

         ResponseWriter responseWriter;
         Variant variant = getVariant( request );

         try
         {
            responseWriter = responseWriterFactory.createWriter( segments, result.getClass(), roleMap, variant );
         } catch (Exception e)
         {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            return;
         }
         responseWriter.write( result, request, response );

      } catch (Exception e)
      {
         uowf.currentUnitOfWork().discard();

         logger.error( e.getMessage() );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      }
   }

   private void initContext( Request request, RoleMap roleMap )
   {
      roleMap.set( resolveRequestLocale( request ), Locale.class );
      roleMap.set( request.getResourceRef(), Reference.class );
      roleMap.set( getApplication(), Application.class );

      Subject subject = new Subject();
      subject.getPrincipals().addAll( request.getClientInfo().getPrincipals() );

      User user = request.getClientInfo().getUser();
      if (user != null)
      {
         subject.getPrivateCredentials().add( user.getSecret() );
      }

      roleMap.set( subject );
   }

   protected Locale resolveRequestLocale( Request request )
   {
      List<Preference<Language>> preferenceList = request.getClientInfo().getAcceptedLanguages();

      if (preferenceList.isEmpty())
         return Locale.getDefault();

      Language language = preferenceList
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

   private Object getContext( Object context, List<String> segments ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ResourceException
   {
      for (int i = 0; i < segments.size() - 1; i++)
      {
         String segment = segments.get( i );

         if (context instanceof SubContexts)
         {
            try
            {
               context = ((SubContexts) context).context( URLDecoder.decode( segment, "UTF-8" ) );
               segments.set( i, "roleMap" );
            } catch (ContextNotFoundException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            } catch (NoSuchEntityException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            } catch (UnsupportedEncodingException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
         } else
         {
            try
            {
               Method subContextMethod = context.getClass().getMethod( segment );
               context = subContextMethod.invoke( context );
            } catch (NoSuchMethodException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
         }
      }

      return context;
   }

   private void contextInfo( Request request, Response response, final Object resource, RoleMap roleMap, ResponseWriter writer ) throws IOException, ResourceException
   {
      Iterable<Method> methods = getContextMethods( resource );
      final List<Method> queries = new ArrayList<Method>();
      final List<Method> commands = new ArrayList<Method>();
      final List<Method> subResources = new ArrayList<Method>();

      InteractionConstraints methodConstraints = constraints;
      if (resource instanceof InteractionConstraints)
         methodConstraints = (InteractionConstraints) resource;

      for (Method method : methods)
      {
         if (!(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
         {
            if (methodConstraints.isValid( method, roleMap ))
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

      final Value index = resource instanceof IndexContext ? ((IndexContext) resource).index() : null;

      ValueBuilder<ContextValue> builder = vbf.newValueBuilder( ContextValue.class );

      if (queries.size() > 0)
      {
         List<String> queriesProperty = builder.prototype().queries().get();
         for (Method query : queries)
         {
            queriesProperty.add( query.getName() );
         }
      }

      if (commands.size() > 0)
      {
         List<String> commandsProperty = builder.prototype().commands().get();
         for (Method command : commands)
         {
            commandsProperty.add( command.getName() );
         }
      }

      if (subResources.size() > 0)
      {
         List<String> contextsProperty = builder.prototype().contexts().get();
         for (Method subResource : subResources)
         {
            contextsProperty.add( subResource.getName());
         }
      }

      if (index != null)
      {
         builder.prototype().index().set( (ValueComposite) index );
      }

      writer.write( builder.newInstance(), request, response );
   }

   private Method getInteractionMethod( Object context, String lastSegment ) throws ResourceException
   {
      for (Method method : getContextMethods( context ))
      {
         if (method.getName().equals( lastSegment ))
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   private Iterable<Method> getContextMethods( Object resource )
   {
      List<Method> methods = contextClassMethods.get( resource.getClass() );

      if (methods == null)
      {
         methods = new ArrayList<Method>();
         Method[] allMethods = resource instanceof Context ? resource.getClass().getInterfaces()[0].getMethods() : resource.getClass().getDeclaredMethods();
         for (Method allMethod : allMethods)
         {
            if (!allMethod.isSynthetic())
               methods.add( allMethod );
         }
         contextClassMethods.put( resource.getClass(), methods );
      }

      return methods;
   }

   private boolean isCommandMethod( Method method )
   {
      if (!method.getReturnType().equals( Void.TYPE ))
         return false;

      if (method.getParameterTypes().length == 0 ||
          (method.getParameterTypes().length == 1 && (Value.class.isAssignableFrom( method.getParameterTypes()[0] ) || Response.class.isAssignableFrom( method.getParameterTypes()[0] ))))
         return true;

      return false;
   }

   private void query( Request request, Response response, Object resource, List<String> segments, RoleMap roleMap, Method queryMethod ) throws ResourceException
   {
      // Check conditions (ETag/LastModified)
      // TODO Check annotations on method

      // Find response MediaType
      // TODO Deduce this from what views are available for this query
      Variant variant = getVariant( request );

      ResponseWriter responseWriter;
      try
      {
         Class returnType;
         Type genericType = queryMethod.getGenericReturnType();
         if (genericType instanceof TypeVariable)
         {
            returnType = (Class) resolveTypeVariable( (TypeVariable) genericType, queryMethod.getDeclaringClass(), resource.getClass() );
         } else
         {
            returnType = (Class) genericType;
         }

         responseWriter = responseWriterFactory.createWriter( segments, returnType, roleMap, variant );
      } catch (Exception e)
      {
         response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
         return;
      }

      if (queryMethod.getParameterTypes().length == 0)
      {
         // Invoke query
         try
         {
            Object queryResult = queryMethod.invoke( resource );
            responseWriter.write( queryResult, request, response );
         } catch (Exception e)
         {
            throw new ResourceException( e );
         }
      } else
      {
         Form form = request.getResourceRef().getQueryAsForm();
         Class valueType = queryMethod.getParameterTypes()[0];
         if (form.size() == 0 && valueType != Response.class )
         {
            // Show form
            try
            {
               String formName = segments.get( segments.size()-1 )+"_form";
               segments.set(segments.size()-1, formName);
               responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, roleMap, variant );

               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

               responseWriter.write( valueDescriptor, request, response );
            } catch (Exception e)
            {
               throw new ResourceException( e );
            }

         } else
         {
            // Invoke form with parameters
            Object[] args = getQueryArguments( request, response, queryMethod );
            Object queryResult = invoke( request, resource, queryMethod, args );
            responseWriter.write( queryResult, request, response );
         }
      }
   }

   private Variant getVariant( Request request )
   {
      List<Language> possibleLanguages = Arrays.asList( Language.ENGLISH );
      Language language = request.getClientInfo().getPreferredLanguage( possibleLanguages );

      if (language == null)
         language = Language.ENGLISH;

      List<MediaType> possibleMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );
      MediaType responseType = request.getClientInfo().getPreferredMediaType( possibleMediaTypes );

      if (responseType == null)
         responseType = MediaType.TEXT_HTML;
      
      Variant variant = new Variant( responseType, language );
      variant.setCharacterSet( CharacterSet.UTF_8 );

      return variant;
   }

   private Object invoke( Request request, final Object resource, final Method method, final Object[] args )
         throws ResourceException
   {
      try
      {
         Subject subject = new Subject();
         subject.getPrincipals().addAll( request.getClientInfo().getPrincipals() );
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

         throw new ResourceException( e.getTargetException() );
      } catch (Throwable e)
      {
         throw new ResourceException( e );
      }
   }

   private Object[] getQueryArguments( Request request, Response response, Method method )
         throws ResourceException
   {
      Object[] args = new Object[method.getParameterTypes().length];
      int idx = 0;

      Representation representation = request.getEntity();
      if (representation != null && MediaType.APPLICATION_JSON.equals( representation.getMediaType() ))
      {
         Class<?> valueType = method.getParameterTypes()[0];

         String json = request.getEntityAsText();
         if (json == null)
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );

         Object requestValue = vbf.newValueFromJSON( valueType, json );
         args[0] = requestValue;
      } else
      {
         Form asForm = request.getResourceRef().getQueryAsForm();
         if (args.length == 1)
         {
            if (ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0] ))
            {
               Class<?> valueType = method.getParameterTypes()[0];

               args[0] = getValueFromForm( (Class<ValueComposite>) valueType, asForm );
            } else if (Form.class.equals( method.getParameterTypes()[0] ))
            {
               args[0] = asForm;
            } else if (Response.class.equals( method.getParameterTypes()[0] ))
            {
               args[0] = response;
            }
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

   private ValueComposite getValueFromForm( Class<? extends ValueComposite> valueType, final Form asForm )
   {
      ValueBuilder<? extends ValueComposite> builder = vbf.newValueBuilder( valueType );
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

   private Iterable<Method> getContextInteractions( Object context )
   {
      List<Method> methods = contextClassMethods.get( context.getClass() );

      if (methods == null)
      {
         methods = new ArrayList<Method>();
         Method[] allMethods = context instanceof Context ? context.getClass().getInterfaces()[0].getMethods() : context.getClass().getDeclaredMethods();
         for (Method allMethod : allMethods)
         {
            if (!allMethod.isSynthetic())
               methods.add( allMethod );
         }
         contextClassMethods.put( context.getClass(), methods );
      }

      return methods;
   }

   private Object[] getCommandArguments( Request request, Response response, Method method ) throws ResourceException
   {
      if (method.getParameterTypes().length > 0)
      {
         Object[] args = new Object[method.getParameterTypes().length];

         Class<? extends ValueComposite> commandType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];

         if (method.getParameterTypes()[0].equals( Response.class ))
         {
            return new Object[]{response};
         }
         MediaType type = request.getEntity().getMediaType();
         if (type == null)
         {
            Form form = request.getResourceRef().getQueryAsForm( CharacterSet.UTF_8 );
            args[0] = getValueFromForm( commandType, form );
            return args;
         } else
         {
            if (method.getParameterTypes()[0].equals( Representation.class ))
            {
               // Command method takes Representation as input
               return new Object[]{request.getEntity()};
            } else if (method.getParameterTypes()[0].equals( Form.class ))
            {
               // Command method takes Form as input
               return new Object[]{new Form(request.getEntity())};
            } else
            {
               // Need to parse input into ValueComposite
               if (type.equals( MediaType.APPLICATION_JSON ))
               {
                  String json = request.getEntityAsText();
                  if (json == null)
                     throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );

                  Object command = vbf.newValueFromJSON( commandType, json );
                  args[0] = command;
                  return args;
               } else if (type.equals( MediaType.TEXT_PLAIN ))
               {
                  String text = request.getEntityAsText();
                  if (text == null)
                     throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );
                  args[0] = text;
                  return args;
               } else if (type.equals( (MediaType.APPLICATION_WWW_FORM) ))
               {
                  Form asForm = new Form(request.getEntity());
                  Class<?> valueType = method.getParameterTypes()[0];
                  args[0] = getValueFromForm( (Class<ValueComposite>) valueType, asForm );
                  return args;
               } else
                  throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format" );
            }
         }
      } else
      {
         return new Object[0];
      }
   }

   public static Type resolveTypeVariable( TypeVariable name, Class declaringClass, Class topClass )
   {
      Type type = resolveTypeVariable( name, declaringClass, new HashMap<TypeVariable, Type>(), topClass );
      if (type == null)
         type = Object.class;

      return type;
   }

   private static Type resolveTypeVariable( TypeVariable name,
                                            Class declaringClass,
                                            Map<TypeVariable, Type> mappings,
                                            Class current
   )
   {
      if (current.equals( declaringClass ))
      {
         Type resolvedType = name;
         while (resolvedType instanceof TypeVariable)
         {
            resolvedType = mappings.get( resolvedType );
         }
         return resolvedType;
      }

      List<Type> types = new ArrayList<Type>();
      for (Type type : current.getGenericInterfaces())
      {
         for (Type type1 : Classes.genericInterfacesOf( type ))
         {
            if (!types.contains( type1 ))
               types.add(type1);

            types.add( type );
         }
      }

      for (Type type : types)
      {
         Class subClass;
         if (type instanceof ParameterizedType)
         {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] args = pt.getActualTypeArguments();
            Class clazz = (Class) pt.getRawType();
            TypeVariable[] vars = clazz.getTypeParameters();
            for (int i = 0; i < vars.length; i++)
            {
               TypeVariable var = vars[i];
               Type mappedType = args[i];
               mappings.put( var, mappedType );
            }
            subClass = (Class) pt.getRawType();
         } else
         {
            subClass = (Class) type;
         }

         Type resolvedType = resolveTypeVariable( name, declaringClass, mappings, subClass );
         if (resolvedType != null)
         {
            return resolvedType;
         }
      }

      return null;
   }
}