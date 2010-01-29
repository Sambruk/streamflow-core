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

import org.qi4j.api.value.Value;
import org.restlet.resource.ServerResource;

import java.lang.reflect.Method;

/**
 * Handle requests to command/query resources.
 * <p/>
 * GET:
 * If the request has ?command=name then show XHTML form
 * for invoking command with name "name".
 * If the request has ?query=name then , if parameters are
 * required then show XHTML form for it, otherwise perform query.
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
public class CompositeCommandQueryServerResource
      extends ServerResource
//      implements TransactionVisitor
{
   /**
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

   public CompositeCommandQueryServerResource()
   {
      getVariants().addAll( Arrays.asList( new Variant( MediaType.TEXT_HTML ), new Variant( MediaType.APPLICATION_JSON ) ) );

      setNegotiated( true );
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      String command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
      if (command != null)
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( command ) );

         // GET on command -> show XHTML form
         try
         {
            Object resource = tbf.newTransient( UsersComposite.class );

            // Find the resource first
            Reference relative = getRequest().getResourceRef().getRelativeRef();

            try
            {
               resource = getResource(resource, relative.getSegments());

               return commandForm(resource, command);
            } catch (Exception e)
            {
               throw new ResourceException(e);
            }
         } finally
         {
            uow.discard();
         }
      }

      String query = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "query" );
      if (query != null)
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( query ) );

         // GET on query -> perform query or show XHTML form
         try
         {
            Object resource = tbf.newTransient( UsersComposite.class );

            // Find the resource first
            Reference relative = getRequest().getResourceRef().getRelativeRef();

            try
            {
               resource = getResource(resource, relative.getSegments());

               return query(resource, query);
            } catch (Exception e)
            {
               throw new ResourceException(e);
            }
         } finally
         {
            uow.discard();
         }
      }

      // Show information about this resource
      UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( getRequest().getResourceRef().getRemainingPart( )) );
      Object resource = tbf.newTransient( UsersComposite.class );

      // Find the resource first
      Reference relative = getRequest().getResourceRef().getRelativeRef();

      try
      {
         resource = getResource(resource, relative.getSegments());

         return resourceInfo(resource);
      } catch (Exception e)
      {
         throw new ResourceException(e);
      }

      else
      {
         UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( command ) );

         try
         {
            Method method = getResourceMethod( command, resource );

            if (isCommandMethod( method ))
            {
               // Show command form
               String form = "";

               Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];
               ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

               for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
               {
                  String propertyInput = propertyDescriptor.qualifiedName().name() +
                        ":<input type=\"text\" name=\"" +
                        propertyDescriptor.qualifiedName().name() + "\"/><br/>";

                  form += propertyInput;
               }

               try
               {
                  String template = TemplateUtil.getTemplate( "resources/command.html", getClass() );
                  String html = TemplateUtil.eval( template,
                        "$name", method.getName(),
                        "$content", form );

                  // Show query form
                  return new StringRepresentation( html, MediaType.TEXT_HTML );
               } catch (IOException e)
               {
                  throw new ResourceException( e );
               }
            } else
            {
               if (getRequest().getResourceRef().hasQuery() && (getRequest().getResourceRef().getQueryAsForm().size() > 1 || method.getParameterTypes().length == 0))
               {
                  // Invoke query
                  Object[] args = getQueryArguments( method );
                  return returnRepresentation( invoke( resource, method, args ), variant );
               } else
               {
                  String form = "";

                  Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];
                  ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

                  for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
                  {
                     String propertyInput = propertyDescriptor.qualifiedName().name() +
                           ":<input type=\"text\" name=\"" +
                           propertyDescriptor.qualifiedName().name() + "\"/><br/>";

                     form += propertyInput;
                  }

                  try
                  {
                     String template = TemplateUtil.getTemplate( "resources/query.html", getClass() );
                     String html = TemplateUtil.eval( template,
                           "$name", method.getName(),
                           "$content", form );

                     // Show query form
                     return new StringRepresentation( html, MediaType.TEXT_HTML );
                  } catch (IOException e)
                  {
                     throw new ResourceException( e );
                  }
               }
            }
         } finally
         {
            uow.discard();
         }
      }
   }

   private Representation query(Object resource, String query)
   {
      Method queryMethod = getQueryMethod(resource.getClass(), query );

      if (queryMethod.getParameterTypes().length == 0)
      {
         // Invoke query
         Object result = queryMethod.invoke( resource );
      } else
      {

      }

      String form = "";

      Class<? extends ValueComposite> valueType = (Class<? extends ValueComposite>) queryMethod.getParameterTypes()[0];
      ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

      for (PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties())
      {
         String propertyInput = propertyDescriptor.qualifiedName().name() +
               ":<input type=\"text\" name=\"" +
               propertyDescriptor.qualifiedName().name() + "\"/><br/>";

         form += propertyInput;
      }

      try
      {
         String template = TemplateUtil.getTemplate( "resources/query.html", getClass() );
         String html = TemplateUtil.eval( template,
               "$name", method.getName(),
               "$content", form );

         // Show query form
         return new StringRepresentation( html, MediaType.TEXT_HTML );
      } catch (IOException e)
      {
         throw new ResourceException( e );
      }

   }

   private Method getQueryMethod( Class aClass, String query )
   {
      for (Method method : aClass.getMethods())
      {
         if (method.getName().equals(query))
            return method;
      }
      return null;
   }

   private Representation commandForm( Object resource, String command )
   {
      return new StringRepresentation("<html>TODO</html>");
   }

   private Object getResource( Object resource, List<String> segments ) throws IllegalAccessException, InvocationTargetException
   {
      for (String segment : segments)
      {
         Method resourceMethod = getSubResourceMethod( resource.getClass() );
         resource = resourceMethod.invoke( resource, segment );
      }

      return resource;
   }

   private Method getSubResourceMethod(Class resourceClass)
   {
      Method[] methods = resourceClass.getMethods();
      for (Method method : methods)
      {
         if (method.getAnnotation( SubResource.class ) != null)
               return method;
      }
      return null;
   }

/x
   protected String getCommand()
   {
      String command = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
      return command;
   }
x/

   protected Representation listOperations(Object resource) throws ResourceException
   {
      // List methods
      Method[] methods = resource.getClass().getMethods();
      StringBuilder queries = new StringBuilder( "" );
      for (Method method : methods)
      {
         if (isQueryMethod( method ))
            queries.append( "<li><a href=\"?query=" ).append(
                  method.getName() ).append( "\" rel=\"" ).append(
                  method.getName() ).append( "\">" )
                  .append( method.getName() ).append( "</a></li>\n" );
      }

      StringBuilder commands = new StringBuilder( "" );
      for (Method method : methods)
      {
         if (isCommandMethod( method ))
            commands.append( "<li><a href=\"?command=" ).append(
                  method.getName() ).append( "\" rel=\"" ).append(
                  method.getName() ).append( "\">" )
                  .append( method.getName() ).append( "</a></li>\n" );
      }

      StringBuilder links = new StringBuilder( "" );
      for (Class mixinType : Classes.interfacesOf( resource.getClass().getInterfaces()[0] ))
      {
         Path path = (Path) mixinType.getAnnotation( Path.class );
         if (path != null)
         {
            links.append( "<li><a href=\"" ).append(
                  path.value() ).append( "/\" rel=\"" ).append(
                  path.value() ).append( "\">" )
                  .append( path.value() ).append( "</a></li>\n" );

         }
      }

      try
      {
         String template = TemplateUtil.getTemplate( "resources/links.html",
               CompositeCommandQueryServerResource.class );
         String content = TemplateUtil.eval( template,
               "$queries", queries.toString(),
               "$commands", commands.toString(),
               "$links", links.toString(),
               "$title", getRequest().getResourceRef().getLastSegment()
                     + " operations" );
         return new StringRepresentation( content, MediaType.TEXT_HTML );
      } catch (IOException e)
      {
         throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
      }
   }

   @Override
   final protected Representation delete( Variant variant ) throws ResourceException
   {
      return post( null, variant );
   }

   @Override
   final protected Representation post( Representation entity, Variant variant ) throws ResourceException
   {
      String operation = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "command" );
      UnitOfWork uow = null;
      Object resource = null;
      source.registerListener( this );
      try
      {
         Method method = getResourceMethod( operation, resource );
         Object[] args = getCommandArguments( method );

         int tries = 0;
         while (tries < 10)
         {
            uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( operation ) );

            getCommandRoles( uow, method, args );

            // Invoke command
            invoke( resource, method, args );

            try
            {
               uow.complete();

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
            } catch (Exception ex)
            {
               throw new ResourceException( Status.SERVER_ERROR_INTERNAL, ex );
            }
         }

         throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Could not invoke command" );

      } catch (ResourceException ex)
      {
         if (uow != null)
            uow.discard();

         throw ex;
      } catch (Exception ex)
      {
         setStatus( Status.SERVER_ERROR_INTERNAL );
         return new ObjectRepresentation<Exception>( ex, MediaType.APPLICATION_JAVA_OBJECT );
      } finally
      {
         source.unregisterListener( this );
      }
   }

   private void getCommandRoles( UnitOfWork uow, Method method, Object[] args )
   {
      // Try to satisfy role parameters in method
      for (int i = 1; i < args.length; i++)
      {
         Class roleType = method.getParameterTypes()[i];
         Name nameAnnotation = Annotations.getAnnotationOfType( method.getParameterAnnotations()[i], Name.class );
         String id = (String) getRequestAttributes().get( nameAnnotation.value() );
         args[i] = uow.get( roleType, id );
      }
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
               try
               {
                  String template = TemplateUtil.getTemplate( "resources/value.html", CompositeCommandQueryServerResource.class );
                  String content = TemplateUtil.eval( template, "$content", ((Value) returnValue).toJSON() );
                  return new StringRepresentation( content, MediaType.TEXT_HTML );
               } catch (IOException e)
               {
                  throw new ResourceException( e );
               }
            } else
            {
               return new EmptyRepresentation();
            }
         } else
         {
            Logger.getLogger( getClass().getName() ).warning( "Unknown result type:" + returnValue.getClass().getName() );
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
    *x
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
   */
}