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
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.ResourceValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.qi4j.spi.util.Annotations.*;

/**
 * JAVADOC
 */
public class CommandQueryResource
      extends Restlet
{
   private Map<Class, List<Method>> contextClassMethods = new ConcurrentHashMap<Class, List<Method>>();

   private
   @Structure
   UnitOfWorkFactory uowf;

   private
   @Structure
   Qi4jSPI spi;

   @Structure
   private ValueBuilderFactory vbf;

   protected
   @Structure
   ModuleSPI module;

   private
   @Service
   ResponseWriterFactory responseWriterFactory;

   private
   @Service
   InteractionConstraints constraints;

   protected Request request;
   protected Response response;

   private Class[] contextClasses;

   public CommandQueryResource( Class... contextClasses )
   {
      this.contextClasses = contextClasses;
   }

   @Override
   public final void handle( Request request, Response response )
   {
      this.request = request;
      this.response = response;

      // Find remaining segments
      List<String> segments = (List<String>) request.getAttributes().get( "segments" );

      super.handle( request, response );

      if (segments.size() > 0)
      {
         String segment = segments.remove( 0 );

         if (segments.size() > 0)
         {
            invokeResource( segment, request, response );
         } else
         {
            invokeCommandQuery( segment, request, response );
         }
      }
   }

   private void invokeResource( String segment, Request request, Response response )
   {
      if (this instanceof SubResources)
      {
         SubResources subResources = (SubResources) this;
         subResources.resource( segment, request, response );
      } else
      {
         // Find @SubResource annotated method
         try
         {
            Method method = getSubResourceMethod( segment );
            method.invoke( this, request, response );
         } catch (Throwable e)
         {
            handleException( response, e );
         }
      }
   }

   protected Method getSubResourceMethod( String resourceName )
   {
      for (Method method : getContextMethods( this.getClass() ))
      {
         if (method.getName().equals( resourceName ) && method.getAnnotation( SubResource.class ) != null)
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   public void resource( Request request, Response response )
   {
      if (request.getMethod().equals( org.restlet.data.Method.GET ))
      {
         RoleMap roleMap = getRoleMap( request );

         final List<Method> queries = new ArrayList<Method>();
         final List<Method> commands = new ArrayList<Method>();
         final List<Method> subResources = new ArrayList<Method>();

         // Add commands+queries from the context classes
         InteractionConstraints methodConstraints = constraints;
         for (Class contextClass : contextClasses)
         {

            Iterable<Method> methods = Arrays.asList( contextClass.getMethods() );
            // TODO Handle custom constraints        if (context instanceof InteractionConstraints)
            //            methodConstraints = (InteractionConstraints) context;

            for (Method method : methods)
            {
               if (!(method.getDeclaringClass().isAssignableFrom( ContextMixin.class )))
               {
                  if (methodConstraints.isValid( method, roleMap ))
                     if (method.getReturnType().equals( Void.TYPE ))
                     {
                        commands.add( method );
                     } else
                     {
                        queries.add( method );
                     }
               }
            }
         }

         // Add subresources available from this resource
         if (SubResources.class.isAssignableFrom( getClass() ))
         {
            try
            {
               Method resourceMethod = getClass().getMethod( "resource", String.class, Request.class, Response.class );
               subResources.add( resourceMethod );
            } catch (NoSuchMethodException e)
            {
               e.printStackTrace();
            }
         } else
         {
            Iterable<Method> methods = Arrays.asList( getClass().getMethods() );

            for (Method method : methods)
            {
               if (methodConstraints.isValid( method, roleMap ))
                  if (method.getAnnotation( SubResource.class ) != null)
                  {
                     subResources.add( method );
                  }
            }
         }

         Value index = null;
         try
         {
            index = (Value) invoke( "index", request, response );

         } catch (Throwable e)
         {
            // Ignore
         }

         ValueBuilder<ResourceValue> builder = vbf.newValueBuilder( ResourceValue.class );

         if (queries.size() > 0)
         {
            List<String> queriesProperty = builder.prototype().queries().get();
            for (Method query : queries)
            {
               queriesProperty.add( query.getName().toLowerCase() );
            }
         }

         if (commands.size() > 0)
         {
            List<String> commandsProperty = builder.prototype().commands().get();
            for (Method command : commands)
            {
               commandsProperty.add( command.getName().toLowerCase() );
            }
         }

         if (subResources.size() > 0)
         {
            List<String> resourcesProperty = builder.prototype().resources().get();
            for (Method subResource : subResources)
            {
               resourcesProperty.add( subResource.getName().toLowerCase() );
            }
         }

         if (index != null)
         {
            builder.prototype().index().set( (ValueComposite) index );
         }

         try
         {
            ResponseWriter writer = responseWriterFactory.createWriter( request.getResourceRef().getRelativeRef().getSegments(), ResourceValue.class, roleMap, getVariant( request ) );
            writer.write( builder.newInstance(), request, response );
         } catch (Throwable e)
         {
            handleException( response, e );
         }
      } else
      {
         response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
      }

   }

   protected Object createContext( String queryName, RoleMap roleMap )
   {
      Class contextClass = getInteractionMethod( queryName ).getDeclaringClass();

      if (TransientComposite.class.isAssignableFrom( contextClass ))
      {
         return module.transientBuilderFactory().newTransientBuilder( contextClass ).use( roleMap ).newInstance();
      } else
      {
         return module.objectBuilderFactory().newObjectBuilder( contextClass ).use( roleMap ).newInstance();
      }
   }

   protected void result( Object resultValue ) throws Exception
   {
      if (resultValue == null)
      {
         if (response.getEntity() == null)
         {
            // TODO This should not be necessary
            String json = "{timestamp:0,events:[]}";
            StringRepresentation rep = new StringRepresentation( json );
            response.setEntity( rep );
            response.setStatus( Status.SUCCESS_OK );
         }
      } else
      {

         ResponseWriter writer = responseWriterFactory.createWriter( request.getResourceRef().getRelativeRef().getSegments(), resultValue.getClass(), getRoleMap( request ), getVariant( request ) );
         writer.write( resultValue, request, response );
      }
   }

   protected Object invoke( String interactionName, Request request, Response response ) throws Throwable
   {
      Object context = createContext( interactionName, getRoleMap( request ) );

      Method method = getInteractionMethod( interactionName );

      if (method.getReturnType().equals( Void.TYPE ))
      {
         // Command

         // Create argument
         Object[] arguments = getCommandArguments( request, response, method );

         // Invoke method
         try
         {
            method.invoke( context, arguments );
            return null; // TODO Get events here
         } catch (IllegalAccessException e)
         {
            throw e;
         } catch (IllegalArgumentException e)
         {
            throw e;
         } catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      } else
      {
         // Query

         // Create argument
         Object[] arguments;
         if (method.getParameterTypes().length > 0)
         {
            arguments = getQueryArguments( request, response, method );

            if (arguments == null)
            {
               // Show form
               Class valueType = method.getParameterTypes()[0];
               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
               return valueDescriptor;
            }
         } else
         {
            // No arguments to this query
            arguments = new Object[0];
         }

         // Invoke method
         try
         {
            return method.invoke( context, arguments );
         } catch (IllegalAccessException e)
         {
            throw e;
         } catch (IllegalArgumentException e)
         {
            throw e;
         } catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
   }

   protected void subResource( Class<? extends CommandQueryResource> subContextClass, Request request, Response response )
   {
      module.objectBuilderFactory().newObjectBuilder( subContextClass ).use( getContext() ).newInstance().handle( request, response );
   }

   protected Method getInteractionMethod( String methodName ) throws ResourceException
   {
      for (Class contextClass : contextClasses)
      {
         for (Method method : getContextMethods( contextClass ))
         {
            if (method.getName().toLowerCase().equals( methodName ))
               return method;
         }
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   protected RoleMap getRoleMap( Request request )
   {
      return (RoleMap) request.getAttributes().get( "roleMap" );
   }

   protected List<String> getSegments( Request request )
   {
      return (List<String>) request.getAttributes().get( "segments" );
   }

   private void invokeCommandQuery( String segment, Request request, Response response )
   {
      if (segment.equals( "" ) || segment.equals( "." ))
      {
         // Index for this resource
         resource( request, response );
      } else
      {
         Method contextMethod = getInteractionMethod( segment );

         // Check if this is a request to show the form for this interaction
         if ((request.getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && request.getResourceRef().getQuery() == null) ||
               (!request.getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && request.getEntity() == null))
         {
            // Show form
            try
            {
               Class valueType = contextMethod.getParameterTypes()[0];
               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

               result( valueDescriptor );
            } catch (Throwable ex)
            {
               handleException( response, ex );
            }
         } else
         {

            // We have input data - do either command or query
            try
            {
               Method method = getClass().getMethod( segment, Request.class, Response.class );

               if (contextMethod.getReturnType().equals( Void.TYPE ))
               {
                  // Command
                  try
                  {
                     method.invoke( this, request, response );
                  } catch (IllegalAccessException e)
                  {
                     response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );

                  } catch (InvocationTargetException e)
                  {
                     handleException( response, e );
                  }
               } else
               {
                  // Query
                  try
                  {
                     method.invoke( this, request, response );
                  } catch (IllegalAccessException e)
                  {
                     response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );

                  } catch (InvocationTargetException e)
                  {
                     handleException( response, e );
                  }
               }

            } catch (NoSuchMethodException e)
            {
               try
               {
                  result( invoke( segment, request, response ) );
               } catch (Throwable throwable)
               {
                  handleException( response, throwable );
               }
            } catch (Exception ex)
            {
               handleException( response, ex );
            }
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

   protected Object[] getQueryArguments( Request request, Response response, Method method )
         throws ResourceException
   {
      Object[] args = new Object[method.getParameterTypes().length];
      int idx = 0;

      Form asForm;
      if (request.getMethod().isSafe())
      {
         // GET
         asForm = request.getResourceRef().getQueryAsForm();
      } else
      {
         // POST - allowed for queries if submitted entry is very large
         asForm = new Form( request.getEntity() );
      }

      if (asForm.getNames().size() == 0)
      {
         // Nothing submitted yet - show form
         return null;
      }

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
            Name name = first( isType( Name.class ), annotations );
            Object arg = asForm.getFirstValue( name.value() );

            // Parameter conversion
            if (method.getParameterTypes()[idx].equals( EntityReference.class ))
            {
               arg = EntityReference.parseEntityReference( arg.toString() );
            }

            args[idx++] = arg;
         }
      }

      return args;
   }

   protected Object[] getCommandArguments( Request request, Response response, Method method ) throws ResourceException
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
               return new Object[]{new Form( request.getEntity() )};
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
                  Form asForm = new Form( request.getEntity() );
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

         public <ThrowableType extends Throwable> void visitProperties( StateVisitor<ThrowableType> visitor )
               throws ThrowableType
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

   private Iterable<Method> getContextMethods( Class resourceClass )
   {
      List<Method> methods = contextClassMethods.get( resourceClass );

      if (methods == null)
      {
         methods = new ArrayList<Method>();
         Method[] allMethods = Context.class.isAssignableFrom( resourceClass ) ? resourceClass.getInterfaces()[0].getMethods() : resourceClass.getDeclaredMethods();
         for (Method allMethod : allMethods)
         {
            if (!allMethod.isSynthetic())
               methods.add( allMethod );
         }
         contextClassMethods.put( resourceClass, methods );
      }

      return methods;
   }

   private void handleException( Response response, Throwable ex )
   {
      while (ex instanceof InvocationTargetException)
      {
         ex = ex.getCause();
      }

      try
      {
         throw ex;
      } catch (ResourceException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( e.getStatus() );
      } catch (IllegalArgumentException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
      } catch (RuntimeException e)
      {
         // RuntimeExceptions are considered server faults
         LoggerFactory.getLogger( getClass() ).warn( "Exception thrown during processing", e );
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      } catch (Exception e)
      {
         // Checked exceptions are considered client faults
         String s = e.getMessage();
         if (s == null)
            s = e.getClass().getSimpleName();
         response.setEntity( new StringRepresentation( s ) );
         response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
      } catch (Throwable e)
      {
         // Anything else are considered server faults
         LoggerFactory.getLogger( getClass() ).error( "Exception thrown during processing", e );
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      }
   }
}
