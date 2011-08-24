package se.streamsource.dci.restlet.server.requestreader;

import org.json.JSONException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.server.RequestReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.api.util.Iterables.*;

/**
 * Convert request into method arguments.
 *
 * TODO: This should be split into many classes to handle the different cases.
 */
public class DefaultRequestReader
      implements RequestReader
{
   @Structure
   Qi4jSPI spi;

   @Structure
   ModuleSPI module;

   public Object[] readRequest(Request request, Method method)
   {
      if (request.getMethod().equals(org.restlet.data.Method.GET))
      {
         Object[] args = new Object[method.getParameterTypes().length];

         Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
         Form entityAsForm = null;
         Representation representation = Request.getCurrent().getEntity();
         if (representation != null && !EmptyRepresentation.class.isInstance(representation))
         {
            entityAsForm = new Form(representation);
         } else
            entityAsForm = new Form();

         if (queryAsForm.isEmpty() && entityAsForm.isEmpty())
         {
            // Nothing submitted yet - show form
            return null;
         }

         if (args.length == 1)
         {
            if (ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
               Class<?> valueType = method.getParameterTypes()[0];

               args[0] = getValueFromForm((Class<ValueComposite>) valueType, queryAsForm, entityAsForm);
               return args;
            } else if (Form.class.equals(method.getParameterTypes()[0]))
            {
               args[0] = queryAsForm.isEmpty() ? entityAsForm : queryAsForm;
               return args;
            } else if (Response.class.equals(method.getParameterTypes()[0]))
            {
               args[0] = Response.getCurrent();
               return args;
            }
         }
         parseMethodArguments(method, args, queryAsForm, entityAsForm);

         return args;
      } else
      {

         Object[] args = new Object[method.getParameterTypes().length];

         Class<? extends ValueComposite> commandType = (Class<? extends ValueComposite>) method.getParameterTypes()[0];

         if (method.getParameterTypes()[0].equals(Response.class))
         {
            return new Object[]{Response.getCurrent()};
         }
         Representation representation = Request.getCurrent().getEntity();
         MediaType type = representation.getMediaType();
         if (type == null)
         {
            Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm(CharacterSet.UTF_8);
            if (ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
               args[0] = getValueFromForm(commandType, queryAsForm, new Form());
            } else
            {
               parseMethodArguments(method, args, queryAsForm, new Form());
            }
            return args;
         } else
         {
            if (method.getParameterTypes()[0].equals(Representation.class))
            {
               // Command method takes Representation as input
               return new Object[]{representation};
            } else if (method.getParameterTypes()[0].equals(Form.class))
            {
               // Command method takes Form as input
               return new Object[]{new Form(representation)};
            } else if (ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
               // Need to parse input into ValueComposite
               if (type.equals(MediaType.APPLICATION_JSON))
               {
                  String json = Request.getCurrent().getEntityAsText();
                  if (json == null)
                  {
                     LoggerFactory.getLogger(getClass()).error("Restlet bugg http://restlet.tigris.org/issues/show_bug.cgi?id=843 detected. Notify developers!");
                     throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
                  }

                  Object command = module.valueBuilderFactory().newValueFromJSON(commandType, json);
                  args[0] = command;
                  return args;
               } else if (type.equals(MediaType.TEXT_PLAIN))
               {
                  String text = Request.getCurrent().getEntityAsText();
                  if (text == null)
                  {
                     LoggerFactory.getLogger(getClass()).error("Restlet bugg http://restlet.tigris.org/issues/show_bug.cgi?id=843 detected. Notify developers!");
                     throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
                  }
                  args[0] = text;
                  return args;
               } else if (type.equals((MediaType.APPLICATION_WWW_FORM)))
               {

                  Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
                  Form entityAsForm;
                  if (representation != null && !EmptyRepresentation.class.isInstance(representation) && representation.isAvailable())
                  {
                     entityAsForm = new Form(representation);
                  } else
                     entityAsForm = new Form();

                  Class<?> valueType = method.getParameterTypes()[0];
                  args[0] = getValueFromForm((Class<ValueComposite>) valueType, queryAsForm, entityAsForm);
                  return args;
               } else
                  throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format");
            } else if (method.getParameterTypes()[0].isInterface() && method.getParameterTypes().length == 1)
            {
               Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
               Form entityAsForm;
               if (representation != null && !EmptyRepresentation.class.isInstance(representation) && representation.isAvailable())
               {
                  entityAsForm = new Form(representation);
               } else
                  entityAsForm = new Form();

               args[0] = module.unitOfWorkFactory().currentUnitOfWork().get(method.getParameterTypes()[0], getValue("entity", queryAsForm, entityAsForm));

               return args;
            } else
            {
               Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
               Form entityAsForm;
               if (representation != null && !EmptyRepresentation.class.isInstance(representation) && representation.isAvailable())
               {
                  entityAsForm = new Form(representation);
               } else
                  entityAsForm = new Form();

               parseMethodArguments(method, args, queryAsForm, entityAsForm);

               return args;
            }
         }
      }
   }

   private ValueComposite getValueFromForm(Class<? extends ValueComposite> valueType, final Form queryAsForm, final Form entityAsForm)
   {
      ValueBuilder<? extends ValueComposite> builder = module.valueBuilderFactory().newValueBuilder(valueType);
      final ValueDescriptor descriptor = spi.getValueDescriptor(builder.prototype());
      builder.withState(new StateHolder()
      {
         public <T> Property<T> getProperty(QualifiedName name)
         {
            return null;
         }

         public <T> Property<T> getProperty(Method propertyMethod)
         {
            return null;
         }

         public <ThrowableType extends Throwable> void visitProperties(StateVisitor<ThrowableType> visitor)
               throws ThrowableType
         {
            for (PropertyType propertyType : descriptor.valueType().types())
            {
               Parameter param = queryAsForm.getFirst(propertyType.qualifiedName().name());

               if (param == null)
                  param = entityAsForm.getFirst(propertyType.qualifiedName().name());

               if (param != null)
               {
                  String value = param.getValue();
                  if (value != null)
                  {
                     try
                     {
                        Object valueObject = propertyType.type().fromQueryParameter(value, module);
                        visitor.visitProperty(propertyType.qualifiedName(), valueObject);
                     } catch (JSONException e)
                     {
                        throw new IllegalArgumentException("Query parameter has invalid JSON format", e);
                     }
                  }
               }
            }
         }
      });
      return builder.newInstance();
   }

   private void parseMethodArguments(Method method, Object[] args, Form queryAsForm, Form entityAsForm)
   {
      // Parse each argument separately using the @Name annotation as help
      int idx = 0;
      for (Annotation[] annotations : method.getParameterAnnotations())
      {
         Name name = (Name) first(filter(isType(Name.class), iterable(annotations)));

         if (name == null)
            throw new IllegalStateException("No @Name annotation found on parameter of method:"+method);

         String argString = getValue(name.value(), queryAsForm, entityAsForm);

         // Parameter conversion
         Class<?> parameterType = method.getParameterTypes()[idx];
         Object arg = null;
         if (parameterType.equals(String.class))
         {
            arg = argString;
         } else if (parameterType.equals(EntityReference.class))
         {
            arg = EntityReference.parseEntityReference(argString);
         } else if (parameterType.isEnum())
         {
            arg = Enum.valueOf((Class<Enum>) parameterType, argString);
         } else if (Integer.TYPE.isAssignableFrom(parameterType))
         {
            arg = Integer.valueOf(argString);
         } else if (Integer.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Integer.valueOf(argString);
         } else if (Long.TYPE.isAssignableFrom(parameterType))
         {
            arg = Long.valueOf(argString);
         } else if (Long.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Long.valueOf(argString);
         } else if (Short.TYPE.isAssignableFrom(parameterType))
         {
            arg = Short.valueOf(argString);
         } else if (Short.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Short.valueOf(argString);
         } else if (Double.TYPE.isAssignableFrom(parameterType))
         {
            arg = Double.valueOf(argString);
         } else if (Double.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Double.valueOf(argString);
         } else if (Float.TYPE.isAssignableFrom(parameterType))
         {
            arg = Float.valueOf(argString);
         } else if (Float.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Float.valueOf(argString);
         } else if (Character.TYPE.isAssignableFrom(parameterType))
         {
            arg = argString.charAt(0);
         } else if (Character.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = argString.charAt(0);
         } else if (Boolean.TYPE.isAssignableFrom(parameterType))
         {
            arg = Boolean.valueOf(argString);
         } else if (Boolean.class.isAssignableFrom(parameterType))
         {
            if (argString != null)
               arg = Boolean.valueOf(argString);
         } else if (Date.class.isAssignableFrom(parameterType))
         {
            arg = DateFunctions.fromString(argString);
         } else if (parameterType.isInterface())
         {
            arg = module.unitOfWorkFactory().currentUnitOfWork().get(parameterType, argString);
         } else
            throw new IllegalArgumentException("Don't know how to parse parameter " + name.value() + " of type " + parameterType.getName());

         if (arg == null && !matchesAny(isType(Optional.class), iterable(annotations)))
            throw new IllegalArgumentException("Parameter " + name.value() + " was not set");

         args[idx++] = arg;
      }
   }

   private String getValue(String name, Form queryAsForm, Form entityAsForm)
   {
      String value = queryAsForm.getFirstValue(name);
      if (value == null)
         value = entityAsForm.getFirstValue(name);
      return value;
   }
}
