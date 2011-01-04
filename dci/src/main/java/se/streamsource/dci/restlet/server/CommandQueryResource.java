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
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.api.ResourceValidity;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.api.util.Iterables.*;

/**
 * JAVADOC
 */
public class CommandQueryResource
      implements Uniform
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
   ResultWriter resultWriter;

   private
   @Service
   InteractionConstraints constraints;

   private
   @Optional
   @Service
   ResultConverter converter;

   protected Request request;
   protected Response response;

   private Class[] contextClasses;
   private Object[] arguments;

   public CommandQueryResource( @Uses Class... contextClasses )
   {
      this.contextClasses = contextClasses;
   }

   public final void handle( Request request, Response response )
   {
      RoleMap roleMap = RoleMap.current();

      // Check constraints for this resource
      if (!constraints.isValid( getClass(), roleMap ))
      {
         throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
      }

      RoleMap.setCurrentRoleMap( new RoleMap( roleMap ) );

      this.request = request;
      this.response = response;

      // Find remaining segments
      List<String> segments = getSegments( request );

      if (segments.size() > 0)
      {
         String segment = segments.remove( 0 );

         if (segments.size() > 0)
         {
            handleSubResource( segment );
         } else
         {
            handleResource( segment );
         }
      }
   }

   private void handleSubResource( String segment )
   {
      if (this instanceof SubResources)
      {
         SubResources subResources = (SubResources) this;
         try
         {
            StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
            template.append( "resource/" );
            subResources.resource( URLDecoder.decode( segment, "UTF-8" ) );
         } catch (UnsupportedEncodingException e)
         {
            subResources.resource( segment );
         }
      } else
      {
         // Find @SubResource annotated method
         try
         {
            Method method = getSubResourceMethod( segment );

            StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
            template.append( segment ).append( "/" );

            method.invoke( this );
         } catch (Throwable e)
         {
            handleException( response, e );
         }
      }
   }

   protected Method getSubResourceMethod( String resourceName )
   {
      for (Method method : getClass().getMethods())
      {
         if (method.getName().equalsIgnoreCase( resourceName ) && method.getAnnotation( SubResource.class ) != null)
            return method;
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   public void resource()
   {
      if (request.getMethod().equals( org.restlet.data.Method.GET ))
      {
         RoleMap roleMap = RoleMap.current();

         final List<Method> queries = new ArrayList<Method>();
         final List<Method> commands = new ArrayList<Method>();
         final List<Method> subResources = new ArrayList<Method>();

         // Add commands+queries from the context classes
         InteractionConstraints methodConstraints = constraints;
         for (Class contextClass : contextClasses)
         {
            // Check context class constraints
            if (!constraints.isValid( contextClass, roleMap ))
               continue; // Skip this class entirely

            Method[] methods = contextClass.getMethods();

            for (Method method : methods)
            {
               if (!method.isSynthetic() && !(method.getDeclaringClass().isAssignableFrom( TransientComposite.class )))
                  if (methodConstraints.isValid( method, roleMap ))
                     if (isCommand( method ))
                     {
                        commands.add( method );
                     } else
                     {
                        queries.add( method );
                     }
            }
         }

         // Add subresources available from this resource
         if (!SubResources.class.isAssignableFrom( getClass() ))
         {
            Iterable<Method> methods = Arrays.asList( getClass().getMethods() );

            for (Method method : methods)
            {
               if (method.getAnnotation( SubResource.class ) != null && methodConstraints.isValid( method, roleMap ))
                  subResources.add( method );
            }
         }

         Value index = null;
         try
         {
            index = (Value) convert( invoke( "index" ) );

         } catch (Throwable e)
         {
            // Ignore
         }

         ValueBuilder<ResourceValue> builder = vbf.newValueBuilder( ResourceValue.class );
         ValueBuilder<LinkValue> linkBuilder = vbf.newValueBuilder( LinkValue.class );
         LinkValue prototype = linkBuilder.prototype();

         if (queries.size() > 0)
         {
            List<LinkValue> queriesProperty = builder.prototype().queries().get();
            prototype.classes().set( "query" );
            for (Method query : queries)
            {
               prototype.text().set( humanReadable( query.getName() ) );
               prototype.href().set( query.getName().toLowerCase() );
               prototype.rel().set( query.getName().toLowerCase() );
               prototype.id().set( query.getName().toLowerCase() );
               queriesProperty.add( linkBuilder.newInstance() );
            }
         }

         if (commands.size() > 0)
         {
            List<LinkValue> commandsProperty = builder.prototype().commands().get();
            prototype.classes().set( "command" );
            for (Method command : commands)
            {
               prototype.text().set( humanReadable( command.getName() ) );
               prototype.href().set( command.getName().toLowerCase() );
               prototype.rel().set( command.getName().toLowerCase() );
               prototype.id().set( command.getName().toLowerCase() );
               commandsProperty.add( linkBuilder.newInstance() );
            }
         }

         if (subResources.size() > 0)
         {
            List<LinkValue> resourcesProperty = builder.prototype().resources().get();
            prototype.classes().set( "resource" );
            for (Method subResource : subResources)
            {
               prototype.text().set( humanReadable( subResource.getName() ) );
               prototype.href().set( subResource.getName().toLowerCase() + "/" );
               prototype.rel().set( subResource.getName().toLowerCase() );
               prototype.id().set( subResource.getName().toLowerCase() );
               resourcesProperty.add( linkBuilder.newInstance() );
            }
         }

         if (index != null)
         {
            builder.prototype().index().set( (ValueComposite) index );
         }

         try
         {
            resultWriter.write( builder.newInstance(), response );
         } catch (Throwable e)
         {
            handleException( response, e );
         }
      } else
      {
         response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
      }

   }

   protected void setResourceValidity( EntityComposite entity )
   {
      ResourceValidity validity = new ResourceValidity( entity, spi );
      RoleMap.current().set( validity );
   }

   private boolean isCommand( Method method )
   {
      return method.getReturnType().equals( Void.TYPE );
   }

   /**
    * Transform a Java name to a human readable string by replacing uppercase characters
    * with space+toLowerCase(char)
    * Example:
    * changeDescription -> Change description
    * doStuffNow -> Do stuff now
    *
    * @param name
    * @return
    */
   private String humanReadable( String name )
   {
      StringBuilder humanReadableString = new StringBuilder();

      for (int i = 0; i < name.length(); i++)
      {
         char character = name.charAt( i );
         if (i == 0)
         {
            // Capitalize first character
            humanReadableString.append( Character.toUpperCase( character ) );
         } else if (Character.isLowerCase( character ))
         {
            humanReadableString.append( character );
         } else
         {
            humanReadableString.append( ' ' ).append( Character.toLowerCase( character ) );
         }
      }

      return humanReadableString.toString();
   }

   protected Object createContext( String queryName )
   {
      for (Class contextClass : contextClasses)
      {
         for (Method method : getContextMethods( contextClass ))
         {
            if (method.getName().equalsIgnoreCase( queryName ))
            {
               if (TransientComposite.class.isAssignableFrom( contextClass ))
               {
                  return module.transientBuilderFactory().newTransient( contextClass );
               } else
               {
                  return module.objectBuilderFactory().newObject( contextClass );
               }
            }
         }
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   protected void result( Object resultValue ) throws Exception
   {
      if (resultValue != null)
      {
         if (!resultWriter.write( resultValue, response ))
         {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "No result writer for type " + resultValue.getClass().getName() );
         }
      }
   }

   protected Object invoke() throws Throwable
   {
      return invoke( request.getResourceRef().getLastSegment() );
   }

   protected Object invoke( String interactionName ) throws Throwable
   {
      Object context = createContext( interactionName );

      Method method = getInteractionMethod( interactionName );

      if (isCommand( method ))
      {
         // Command

         // Create argument
         arguments = getCommandArguments( method );

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
         if (method.getParameterTypes().length > 0)
         {
            try
            {
               arguments = getQueryArguments( method );

               if (arguments == null)
               {
                  // Show form
                  return formForMethod( method );
               }
            } catch (IllegalArgumentException e)
            {
               // Still missing some values - show form
               return formForMethod(method);
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

   protected void subResource( Class<? extends CommandQueryResource> subResourceClass )
   {
      module.objectBuilderFactory().newObject( subResourceClass ).handle( request, response );
   }

   protected void subResourceContexts( Class<?>... contextClasses )
   {
      module.objectBuilderFactory().newObjectBuilder( DefaultCommandQueryResource.class ).use( new Object[]{contextClasses} ).newInstance().handle( request, response );
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

   protected List<String> getSegments( Request request )
   {
      return (List<String>) request.getAttributes().get( "segments" );
   }

   protected <T> T setRole( Class<T> entityClass, String id, Class... roleClasses )
         throws ResourceException
   {
      try
      {
         T composite = module.unitOfWorkFactory().currentUnitOfWork().get( entityClass, id );
         RoleMap.current().set( composite, roleClasses );
         return composite;
      } catch (EntityTypeNotFoundException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
      }
   }

   protected <T> T findManyAssociation( ManyAssociation<T> manyAssociation, String id )
         throws ResourceException
   {
      for (T entity : manyAssociation)
      {
         if (entity.toString().equals( id ))
         {
            RoleMap.current().set( entity );
            return entity;
         }
      }

      throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
   }

   protected void findList( List<?> list, String indexString )
   {
      Integer index = Integer.decode( indexString );

      if (index < 0 || index >= list.size())
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

      RoleMap.current().set( index, Integer.class );

      Object value = list.get( index );
      RoleMap.current().set( value );

   }

   private void handleResource( String segment )
   {
      if (segment.equals( "" ) || segment.equals( "." ))
      {
         StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
         template.append( "resource" );

         // Index for this resource
         resource();
      } else
      {
         StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
         template.append( segment );

         Method contextMethod = getInteractionMethod( segment );

         if (isCommand( contextMethod ))
         {
            handleCommand( contextMethod );
         } else
         {
            handleQuery( contextMethod );
         }
      }
   }

   private void handleCommand( Method contextMethod )
   {
      // Check if this is a request to show the form for this command
      if (shouldShowCommandForm(contextMethod))
      {
         // Show form
         request.setMethod( org.restlet.data.Method.POST );

         try
         {
            result( formForMethod( contextMethod ) );
         } catch (Exception e)
         {
            handleException( response, e );
         }
      } else
      {
         try
         {
            // Check timestamps
            ResourceValidity validity = RoleMap.role( ResourceValidity.class );
            validity.checkRequest( request );
         } catch (IllegalArgumentException e)
         {
            // Ignore
         }

         // We have input data - do command
         try
         {
            Method method = getClass().getMethod( contextMethod.getName().toLowerCase() );

            // Command
            try
            {
               method.invoke( this );
            } catch (IllegalArgumentException e)
            {
               response.setStatus( Status.SERVER_ERROR_INTERNAL );

            } catch (IllegalAccessException e)
            {
               response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            } catch (InvocationTargetException e)
            {
               handleException( response, e );
            }
         } catch (NoSuchMethodException e)
         {
            try
            {
               Object result = invoke( contextMethod.getName().toLowerCase() );
               if (result instanceof Representation)
               {
                  response.setEntity( (Representation) result );
               } else
                  result( convert( result ) );
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

   private boolean shouldShowCommandForm(Method contextMethod)
   {
      // Show form on GET/HEAD
      if (request.getMethod().isSafe())
         return true;

      if (contextMethod.getParameterTypes().length > 0)
      {
         return !(contextMethod.getParameterTypes()[0].equals( Response.class ) || request.getEntity().isAvailable() || request.getEntityAsText() != null || request.getResourceRef().getQuery() != null);
      }

      return false;
   }

   private void handleQuery( Method contextMethod )
   {
      // Query
      // Check if this is a request to show the form for this interaction
      if ((request.getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && request.getResourceRef().getQuery() == null) ||
            (!request.getMethod().isSafe() && contextMethod.getParameterTypes().length != 0 && !(request.getEntity().isAvailable() || request.getResourceRef().getQuery() != null || contextMethod.getParameterTypes()[0].equals( Response.class ))))
      {
         // Show form
         try
         {
            result( formForMethod( contextMethod ) );
         } catch (Exception e)
         {
            handleException( response, e );
         }
      } else
      {
         try
         {
            // Check timestamps
            ResourceValidity validity = RoleMap.role( ResourceValidity.class );
            validity.checkRequest( request );
         } catch (IllegalArgumentException e)
         {
            // Ignore
         }

         // We have input data - do query
         try
         {
            Method method = getClass().getMethod( contextMethod.getName().toLowerCase() );

            // Query
            try
            {
               method.invoke( this );
            } catch (IllegalAccessException e)
            {
               response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            } catch (InvocationTargetException e)
            {
               handleException( response, e );
            }
         } catch (NoSuchMethodException e)
         {
            try
            {
               Object result = invoke( contextMethod.getName().toLowerCase() );
               if (result instanceof Representation)
               {
                  response.setEntity( (Representation) result );
               } else
                  result( convert( result ) );
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

   private Form formForMethod( Method contextMethod )
   {
      Form form = new Form();

      Form queryAsForm = request.getResourceRef().getQueryAsForm();
      Form entityAsForm = null;
      Representation representation = request.getEntity();
      if (representation != null && !EmptyRepresentation.class.isInstance( representation ))
      {
         entityAsForm = new Form( representation );
      } else
         entityAsForm = new Form();

      Class valueType = contextMethod.getParameterTypes()[0];
      if (ValueComposite.class.isAssignableFrom( valueType))
      {
         ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

         for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
         {
            String value = getValue( propertyDescriptor.qualifiedName().name(), queryAsForm, entityAsForm );

            if (value == null && propertyDescriptor.initialValue() != null)
              value = propertyDescriptor.initialValue().toString();

            form.add( propertyDescriptor.qualifiedName().name(), value );
         }
      } else
      {
         // Construct form out of individual parameters instead
         int idx = 0;
         for (Annotation[] annotations : contextMethod.getParameterAnnotations())
         {
            Name name = (Name) first( filter( isType( Name.class ), iterable( annotations ) ) );

            String value = getValue( name.value(), queryAsForm, entityAsForm );

            String paramName;
            if (name != null)
            {
               paramName = name.value();
            } else
            {
               paramName = "param"+idx;
            }

            form.add( paramName, value );

            idx++;
         }
      }

      return form;
   }

   private Object convert( Object result )
   {
      if (converter != null)
         result = converter.convert( result, request, arguments );

      return result;
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

   protected Object[] getQueryArguments( Method method )
         throws ResourceException
   {
      Object[] args = new Object[method.getParameterTypes().length];

      Form queryAsForm = request.getResourceRef().getQueryAsForm();
      Form entityAsForm = null;
      Representation representation = request.getEntity();
      if (representation != null && !EmptyRepresentation.class.isInstance( representation ))
      {
         entityAsForm = new Form( representation );
      } else
         entityAsForm = new Form();

      if (queryAsForm.isEmpty() && entityAsForm.isEmpty())
      {
         // Nothing submitted yet - show form
         return null;
      }

      if (args.length == 1)
      {
         if (ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0] ))
         {
            Class<?> valueType = method.getParameterTypes()[0];

            args[0] = getValueFromForm( (Class<ValueComposite>) valueType, queryAsForm, entityAsForm );
            return args;
         } else if (Form.class.equals( method.getParameterTypes()[0] ))
         {
            args[0] = queryAsForm.isEmpty() ? entityAsForm : queryAsForm;
            return args;
         } else if (Response.class.equals( method.getParameterTypes()[0] ))
         {
            args[0] = response;
            return args;
         }
      }
      parseMethodArguments( method, args, queryAsForm, entityAsForm );


      return args;
   }

   protected Object[] getCommandArguments( Method method ) throws ResourceException
   {
      if (method.getParameterTypes().length > 0)
      {
         Object[] args = new Object[method.getParameterTypes().length];

         Class<? extends ValueComposite> commandType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];

         if (method.getParameterTypes()[0].equals( Response.class ))
         {
            return new Object[]{response};
         }
         Representation representation = request.getEntity();
         MediaType type = representation.getMediaType();
         if (type == null)
         {
            Form queryAsForm = request.getResourceRef().getQueryAsForm( CharacterSet.UTF_8 );
            if (ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0]))
            {
               args[0] = getValueFromForm( commandType, queryAsForm, new Form() );
            } else
            {
               parseMethodArguments( method, args, queryAsForm, new Form() );
            }
            return args;
         } else
         {
            if (method.getParameterTypes()[0].equals( Representation.class ))
            {
               // Command method takes Representation as input
               return new Object[]{representation};
            } else if (method.getParameterTypes()[0].equals( Form.class ))
            {
               // Command method takes Form as input
               return new Object[]{new Form( representation )};
            } else if (ValueComposite.class.isAssignableFrom( method.getParameterTypes()[0]))
            {
               // Need to parse input into ValueComposite
               if (type.equals( MediaType.APPLICATION_JSON ))
               {
                  String json = request.getEntityAsText();
                  if (json == null)
                  {
                     LoggerFactory.getLogger( getClass() ).error( "Restlet bugg http://restlet.tigris.org/issues/show_bug.cgi?id=843 detected. Notify developers!" );
                     throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );
                  }

                  Object command = vbf.newValueFromJSON( commandType, json );
                  args[0] = command;
                  return args;
               } else if (type.equals( MediaType.TEXT_PLAIN ))
               {
                  String text = request.getEntityAsText();
                  if (text == null)
                  {
                     LoggerFactory.getLogger( getClass() ).error( "Restlet bugg http://restlet.tigris.org/issues/show_bug.cgi?id=843 detected. Notify developers!" );
                     throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!" );
                  }
                  args[0] = text;
                  return args;
               } else if (type.equals( (MediaType.APPLICATION_WWW_FORM) ))
               {

                  Form queryAsForm = request.getResourceRef().getQueryAsForm();
                  Form entityAsForm;
                  if (representation != null && !EmptyRepresentation.class.isInstance( representation ) && representation.isAvailable())
                  {
                     entityAsForm = new Form( representation );
                  } else
                     entityAsForm = new Form();

                  Class<?> valueType = method.getParameterTypes()[0];
                  args[0] = getValueFromForm( (Class<ValueComposite>) valueType, queryAsForm, entityAsForm );
                  return args;
               } else
                  throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format" );
            } else
            {
               Form queryAsForm = request.getResourceRef().getQueryAsForm();
               Form entityAsForm;
               if (representation != null && !EmptyRepresentation.class.isInstance( representation ) && representation.isAvailable())
               {
                  entityAsForm = new Form( representation );
               } else
                  entityAsForm = new Form();

               parseMethodArguments( method, args,  queryAsForm, entityAsForm);

               return args;
            }
         }
      } else
      {
         return new Object[0];
      }
   }

   private void parseMethodArguments( Method method, Object[] args, Form queryAsForm, Form entityAsForm )
   {
      // Parse each argument separately using the @Name annotation as help
      int idx = 0;
      for (Annotation[] annotations : method.getParameterAnnotations())
      {
         Name name = (Name) first( filter( isType( Name.class ), iterable( annotations ) ) );
         String argString = getValue( name.value(), queryAsForm, entityAsForm );

         // Parameter conversion
         Class<?> parameterType = method.getParameterTypes()[idx];
         Object arg;
         if (parameterType.equals( EntityReference.class ))
         {
            arg = EntityReference.parseEntityReference( argString );
         } else if (parameterType.isEnum())
         {
            arg = Enum.valueOf( (Class<Enum>) parameterType, argString );
         } else if (Integer.class.isAssignableFrom( parameterType ))
         {
            arg = Integer.valueOf( argString );
         } else if (parameterType.isInterface())
         {
            arg = uowf.currentUnitOfWork().get( parameterType, argString );
         } else
         {
            arg = argString;
         }

         args[idx++] = arg;
      }
   }

   private ValueComposite getValueFromForm( Class<? extends ValueComposite> valueType, final Form queryAsForm, final Form entityAsForm )
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
               Parameter param = queryAsForm.getFirst( propertyType.qualifiedName().name() );

               if (param == null)
                  param = entityAsForm.getFirst( propertyType.qualifiedName().name() );

               if (param != null)
               {
                  String value = param.getValue();
                  if (value != null)
                  {
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
         Method[] allMethods = resourceClass.getMethods();
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

   private String getValue( String name, Form queryAsForm, Form entityAsForm )
   {
      String value = queryAsForm.getFirstValue( name );
      if (value == null)
         value = entityAsForm.getFirstValue( name );
      return value;
   }
}
