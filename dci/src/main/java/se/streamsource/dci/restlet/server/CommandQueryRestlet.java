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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.IndexValue;

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
 * Implement the getRoot method to use this class
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
 * DELETE: this translates into a call on DeleteInteraction.delete() on the given resource.
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
   private RootInteractionsFactory rootInteractionsFactory;

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

      // Find context
      Reference ref = request.getResourceRef();
      List<String> segments = ref.getScheme().equals( "riap" ) ? ref.getRelativeRef( new Reference( "riap://application/" ) ).getSegments() : ref.getRelativeRef().getSegments();

      UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getUsecaseName( request ) ) );

      Context context = new Context();
      uow.metaInfo().set( context );
      initContext( request, context );

      // Find the interactions first
      Object interactions = null;
      try
      {
         interactions = getInteractions( rootInteractionsFactory.getRoot( context ), segments );
         context = uow.metaInfo().get( Context.class ); // Get current context for this interaction
      } catch (Exception e)
      {
         uow.discard();
         logger.error( e.getMessage() );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      }

      if (interactions == null)
      {
         uow.discard();
         response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
         return;
      }

      // What HTTP method do we want to do
      org.restlet.data.Method method = request.getMethod();
      if (method.equals( org.restlet.data.Method.GET ))
      {
         get( request, response, interactions, context, segments );
      } else if (method.equals( org.restlet.data.Method.DELETE ))
      {
         delete( request, response, interactions, context, segments );
      } else if (method.equals( org.restlet.data.Method.POST) || method.equals( org.restlet.data.Method.PUT) )
      {
         // When doing POST/PUT we should try several times if there is a coflict when committing
         int retries = 0;
         while (retries < 10)
         {
            try
            {
               post( request, response, interactions, context, segments );
               return;
            } catch (UnitOfWorkCompletionException e)
            {
               // Retry
               uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getUsecaseName( request ) ) );

               context = new Context();
               initContext( request, context );

               // Find the context again in the new UoW
               interactions = null;
               try
               {
                  interactions = getInteractions( rootInteractionsFactory.getRoot( context ), segments );
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
         head( request, response, context, interactionContext );
      }*/
   }

   private String getUsecaseName( Request request )
   {
      if (request.getMethod().equals( org.restlet.data.Method.DELETE ))
         return "delete";
      else
         return request.getResourceRef().getLastSegment();
   }

   private void get( Request request, Response response, Object interactions, Context context, List<String> segments )
   {
      try
      {
         String lastSegment = segments.get( segments.size() - 1 );

         if (lastSegment.equals( "" ) || lastSegment.equals( "." ))
         {
            // Show interactions info
            ResponseWriter writer = responseWriterFactory.createWriter( segments, IndexValue.class, context, getVariant( request ) );

            contextInfo( request, response, interactions, context, writer );
         } else if (lastSegment.equals( "context" ))
         {
            String contextId = request.getResourceRef().getQueryAsForm().getFirstValue( "context" );
            if (contextId == null)
            {
               ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, Interactions.class, context, getVariant( request ) );

               responseWriter.write( null, request, response );
            } else
            {
               // Redirect to the subcontext
               Reference userRef = request.getResourceRef().getParentRef().clone().addSegment( contextId ).addSegment( "" );
               response.redirectPermanent( userRef );
            }
         } else
         {
            Method method = getInteractionMethod( interactions, lastSegment );
            if (!constraints.isValid( method, context ))
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Interaction not valid" );

            // Check whether it's a command or query
            if (isCommandMethod( method ))
            {
               // The method is shown in the form, so let's change it to POST
               request.setMethod( org.restlet.data.Method.POST );
               ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, context, getVariant( request ) );

               Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];
               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

               responseWriter.write( valueDescriptor, request, response );
            } else
            {
               query( request, response, interactions, segments, context, method );
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

   private void post( Request request, Response response, Object interactions, Context context, List<String> segments )
         throws UnitOfWorkCompletionException
   {
      // POST on command -> perform interaction
      String lastSegment = segments.get( segments.size() - 1 );

      UnitOfWork unitOfWork = uowf.currentUnitOfWork();

      try
      {
         Method method = getInteractionMethod( interactions, lastSegment );

         if (method.getParameterTypes().length == 0)
         {
            // Invoke command
            invoke( request, interactions, method, null );
         } else
         {
            Class valueType = method.getParameterTypes()[0];
            // Invoke command with parameters
            if (request.getEntity().getAvailableSize() == 0 && valueType != Response.class )
            {
               ResponseWriter responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, context, getVariant( request ) );

               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
               responseWriter.write( valueDescriptor, request, response );

               unitOfWork.discard();

               return;

            } else
            {
               Object[] args = getCommandArguments( request, response, method );
               invoke( request, interactions, method, args );
            }
         }

         unitOfWork.complete();

         // Show some result
         Object result = commandResult.getResult();

         ResponseWriter responseWriter;
         Variant variant = getVariant( request );

         try
         {
            responseWriter = responseWriterFactory.createWriter( segments, result.getClass(), context, variant );
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

   private void delete( Request request, Response response, Object interactions, Context context, List<String> segments  )
   {
      DeleteInteraction deleteInteraction = (DeleteInteraction) interactions;

      try
      {
         deleteInteraction.delete();

         uowf.currentUnitOfWork().complete();

         // Show some result
         Object result = commandResult.getResult();

         ResponseWriter responseWriter;
         Variant variant = getVariant( request );

         try
         {
            responseWriter = responseWriterFactory.createWriter( segments, result.getClass(), context, variant );
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

   private void initContext( Request request, Context context )
   {
      context.set( resolveRequestLocale( request ), Locale.class );
      context.set( request.getResourceRef(), Reference.class );
      context.set( getApplication(), Application.class );

      Subject subject = new Subject();
      subject.getPrincipals().addAll( request.getClientInfo().getPrincipals() );


      User user = request.getClientInfo().getUser();
      if (user != null)
      {
         subject.getPrivateCredentials().add( user.getSecret() );
      }
      subject.setReadOnly();

      context.set( subject );
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

   private Object getInteractions( Object interactions, List<String> segments ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ResourceException
   {
      for (int i = 0; i < segments.size() - 1; i++)
      {
         String segment = segments.get( i );

         if (interactions instanceof SubContexts)
         {
            try
            {
               interactions = ((SubContexts) interactions).context( URLDecoder.decode( segment, "UTF-8" ) );
               segments.set( i, "context" );
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
               Method subContextMethod = interactions.getClass().getMethod( segment );
               interactions = subContextMethod.invoke( interactions );
            } catch (NoSuchMethodException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
         }
      }

      return interactions;
   }

   private void contextInfo( Request request, Response response, final Object resource, Context context, ResponseWriter writer ) throws IOException, ResourceException
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
            if (methodConstraints.isValid( method, context ))
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

      final Value index = resource instanceof IndexInteraction ? ((IndexInteraction) resource).index() : null;

      ValueBuilder<IndexValue> builder = vbf.newValueBuilder( IndexValue.class );

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
            if (subResource.getDeclaringClass().equals( SubContexts.class ))
               contextsProperty.add( subResource.getName() );
            else
               contextsProperty.add( subResource.getName() + "/" );
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
         Method[] allMethods = resource instanceof Interactions ? resource.getClass().getInterfaces()[0].getMethods() : resource.getClass().getDeclaredMethods();
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

      if (method.getParameterTypes().length == 0 || (method.getParameterTypes().length == 1 && Value.class.isAssignableFrom( method.getParameterTypes()[0] )))
         return true;

      return false;
   }

   private void query( Request request, Response response, Object resource, List<String> segments, Context context, Method queryMethod ) throws ResourceException
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

         responseWriter = responseWriterFactory.createWriter( segments, returnType, context, variant );
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
               responseWriter = responseWriterFactory.createWriter( segments, ValueDescriptor.class, context, variant );

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
         Object requestValue = vbf.newValueFromJSON( valueType, request.getEntityAsText() );
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
         Method[] allMethods = context instanceof Interactions ? context.getClass().getInterfaces()[0].getMethods() : context.getClass().getDeclaredMethods();
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
               // Command method takes Representation as input
               return new Object[]{request.getEntityAsForm()};
            } else
            {
               // Need to parse input into ValueComposite
               if (type.equals( MediaType.APPLICATION_JSON ))
               {
                  String json = request.getEntityAsText();

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
                  Form asForm = request.getEntityAsForm();
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

   /*
      @Override
      protected Representation get( Variant variant ) throws ResourceException
      {
         Reference ref = getRequest().getResourceRef();
         List<String> segments = ref.getScheme().equals("riap") ? ref.getRelativeRef(new Reference("riap://application/")).getSegments() : ref.getRelativeRef().getSegments();

         String lastSegment = segments.get( segments.size() - 1 );

         if (lastSegment.equals( "" ) || lastSegment.equals( "." ))
         {
            // Show information about this resource
            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getRequest().getResourceRef().getRemainingPart() ) );
            try
            {
               Context interactionContext = new Context();
               initContext( interactionContext );
               Object resource = getRoot(interactionContext);

               // Find the resource first
               try
               {
                  resource = getResource( resource, segments );
                  getResponse().getAttributes().put( "segments", segments );

                  return resourceInfo( resource, interactionContext );
               } catch (Exception e)
               {
                  throw new ResourceException( e );
               }
            } catch (ResourceException e)
            {
               uow.discard();
               throw e;
            }
         }

         if (lastSegment.equals("context" ))
         {
            String resource = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "resource" );
            if (resource == null)
            {
               // Show resource selection form
               Representation rep = new WriterRepresentation(MediaType.TEXT_HTML)
               {
                  @Override
                  public void write( Writer writer ) throws IOException
                  {
                     try
                     {
                        selectResourceTemplate.merge( new VelocityContext(), writer );
                     } catch (Exception e)
                     {
                        throw (IOException) new IOException().initCause( e );
                     }
                  }
               };
               rep.setCharacterSet( CharacterSet.UTF_8 );
               return rep;
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
            Context context = new Context();
            initContext( context );
            Object resource = getRoot(context);

            // Find the resource first
            try
            {
               resource = getResource( resource, segments );
               getResponse().getAttributes().put( "segments", segments );

               Method method = getMethod( resource, lastSegment );

               if (!constraints.isValid( method, context ))
                  throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Interaction not valid");

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
   */

/*
   private void initContext( Context context )
   {
      context.playRoles( resolveRequestLocale(), Locale.class );
      context.playRoles( getRequest().getResourceRef(), Reference.class );
      context.playRoles( getApplication(), Application.class );

      Subject subject = new Subject();
      subject.getPrincipals().addAll( getRequest().getClientInfo().getPrincipals() );


      User user = getRequest().getClientInfo().getUser();
      if (user != null)
      {
         subject.getPrivateCredentials().add( user.getSecret() );
      }
      subject.setReadOnly();

      context.playRoles( subject );
   }

   private Method getMethod( Object resource, String lastSegment ) throws ResourceException
   {
      for (Method method : getResourceMethods( resource ))
      {
         if (method.getName().equals( lastSegment ))
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }
*/

/*
   protected abstract Object getRoot( Context context);

   private Representation resourceInfo( final Object resource, Context interactionContext ) throws IOException
   {
      MediaType responseType = getRequest().getClientInfo().getPreferredMediaType( Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_HTML ) );

      Iterable<Method> methods = getResourceMethods( resource );
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
            if (methodConstraints.isValid( method, interactionContext ))
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

      final Value index = resource instanceof IndexInteraction ? ((IndexInteraction)resource).index() : null;

      // JSON
      Representation rep = new WriterRepresentation( MediaType.APPLICATION_JSON )
      {
         @Override
         public void write( Writer writer ) throws IOException
         {
            ValueBuilder<IndexValue> builder = vbf.newValueBuilder( IndexValue.class );

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
                  if (subResource.getDeclaringClass().equals(SubContexts.class))
                     contextsProperty.add( subResource.getName() );
                  else
                     contextsProperty.add( subResource.getName()+"/" );
               }
            }

            if (index != null)
            {
               builder.prototype().index().set( (ValueComposite) index );
            }

            writer.write( builder.newInstance().toJSON() );
         }
      };
      rep.setCharacterSet( CharacterSet.UTF_8 );
      return rep;
   }
*/

/*
   private Iterable<Method> getResourceMethods( Object resource )
   {
      List<Method> methods = contextClassMethods.get( resource.getClass() );

      if (methods == null)
      {
         methods = new ArrayList<Method>( );
         Method[] allMethods = resource instanceof Interactions ? resource.getClass().getInterfaces()[0].getMethods() : resource.getClass().getDeclaredMethods();
         for (Method allMethod : allMethods)
         {
            if (!allMethod.isSynthetic())
               methods.add( allMethod );
         }
         contextClassMethods.put( resource.getClass(), methods );
      }

      return methods;
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

            final VelocityContext context = new VelocityContext();
            context.put( "name", queryMethod.getName() );
            context.put( "properties", valueDescriptor.state().properties() );

            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML)
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  queryTemplate.merge( context, writer );
               }
            };
            rep.setCharacterSet( CharacterSet.UTF_8 );
            return rep;
         } else
         {
            // Invoke form with parameters
            Object[] args = getQueryArguments( queryMethod );
            return returnRepresentation( invoke( resource, queryMethod, args ), variant );
         }
      }
   }
*/

/*   private Method getQueryMethod( Class aClass, String query ) throws ResourceException
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

      final VelocityContext context = new VelocityContext( );
      context.put( "name", commandMethod.getName() );
      context.put( "content", formHtml );
      Representation rep = new WriterRepresentation(MediaType.TEXT_HTML)
      {
         @Override
         public void write( Writer writer ) throws IOException
         {
            commandTemplate.merge(context, writer);
         }
      };
      rep.setCharacterSet( CharacterSet.UTF_8 );
      return rep;
   }

   private Object getResource( Object resource, List<String> segments ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ResourceException
   {
      for (int i = 0; i < segments.size()-1; i++)
      {
         String segment = segments.get( i );

         if (resource instanceof SubContexts)
         {
            try
            {
               resource = ((SubContexts) resource).context( URLDecoder.decode( segment, "UTF-8") );
               segments.set( i, "context" );
            } catch (ContextNotFoundException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            } catch (NoSuchEntityException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }catch (UnsupportedEncodingException e)
            {
               throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
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

   *//**
 * protected String getCommand()
 * {
 * String command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
 * return command;
 * }
 *//*

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
            Context context = new Context();
            initContext( context );
            Object resource = getRoot(context);

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
               if (getRequest().getEntity().getAvailableSize() == 0)
               {
                  return commandForm( resource, lastSegment );
               } else
               {
                  Object[] args = getCommandArguments( method );
                  invoke( resource, method, args );
               }
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

            if (e instanceof ResourceException)
               throw (ResourceException) e;
            else
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
               return new StringRepresentation( ((Value) returnValue).toJSON(), MediaType.APPLICATION_JSON, getRequest().getClientInfo().getAcceptedLanguages().get(0).getMetadata(), CharacterSet.UTF_8 );
            } else if (variant.getMediaType().equals( MediaType.TEXT_HTML ))
            {
               if (returnValue instanceof LinksValue)
               {
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

                  final VelocityContext context = new VelocityContext( );
                  context.put("content", linksHtml.toString());
                  context.put("title", getRequest().getResourceRef().getRemainingPart());

                  Representation rep = new WriterRepresentation(MediaType.TEXT_HTML)
                  {
                     @Override
                     public void write( Writer writer ) throws IOException
                     {
                        linksTemplate.merge(context, writer);
                     }
                  };
                  rep.setCharacterSet( CharacterSet.UTF_8 );
                  return rep;

               } else
               {
                  final VelocityContext context = new VelocityContext( );
                  context.put("content", ((Value) returnValue).toJSON());
                  context.put("title", getRequest().getResourceRef().getRemainingPart());

                  Representation rep = new WriterRepresentation(MediaType.TEXT_HTML)
                  {
                     @Override
                     public void write( Writer writer ) throws IOException
                     {
                        valueTemplate.merge(context, writer);
                     }
                  };
                  rep.setCharacterSet( CharacterSet.UTF_8 );
                  return rep;
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
         } else if (returnValue instanceof Representation)
         {
            return (Representation) returnValue;
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

   *//**
 * A command method has the following attributes:
 * - Returns void
 * - Has a Value as the first parameter
 *
 * @param method
 * @return
 *//*
   private boolean isCommandMethod( Method method )
   {
      if (!method.getReturnType().equals( Void.TYPE ))
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

         Class<? extends ValueComposite> commandType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];

         if (getRequest().getEntity().getMediaType() == null)
         {
            Form form = getRequest().getResourceRef().getQueryAsForm( CharacterSet.UTF_8 );
            args[0] = getValueFromForm( commandType, form );
            return args;
         } else
         {
            if (method.getParameterTypes()[0].equals( Representation.class ))
            {
               // Command method takes Representation as input
               return new Object[]{getRequest().getEntity()};
            } else if (method.getParameterTypes()[0].equals( Form.class ))
            {
               // Command method takes Representation as input
               return new Object[]{getRequest().getEntityAsForm()};
            } else
            {
               // Need to parse input into ValueComposite
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
            }
         }
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
         if (args.length == 1)
         {
            if (ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0] ))
            {
               Class<?> valueType = method.getParameterTypes()[0];

               args[0] = getValueFromForm( (Class<ValueComposite>) valueType, asForm );
            } else if (Form.class.equals(method.getParameterTypes()[0] ))
            {
               args[0] = asForm;
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

   protected Locale resolveRequestLocale()
   {
      List<Preference<Language>> preferenceList = getRequest().getClientInfo().getAcceptedLanguages();

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
   }*/
}