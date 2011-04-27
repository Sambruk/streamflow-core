package se.streamsource.streamflow.web.application.knowledgebase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Integration of external Knowledgebase.
 */
@Mixins(KnowledgebaseService.Mixin.class)
public interface KnowledgebaseService
      extends ServiceComposite, Activatable, Configuration
{
   /**
    * Given an entity, create a URL in the external website for it.
    *
    * Can be used with CaseTypes and Labels.
    *
    * @param entity
    * @return
    * @throws IllegalArgumentException
    */
   String createURL(EntityComposite entity)
         throws IllegalArgumentException;

   abstract class Mixin
         implements KnowledgebaseService
   {
      @This
      Configuration<KnowledgebaseConfiguration> config;

      @Service
      VelocityEngine velocity;

      public void activate() throws Exception
      {
      }

      public void passivate() throws Exception
      {

      }

      public String createURL(EntityComposite entity) throws IllegalArgumentException
      {
         try
         {
            if (entity instanceof CaseType)
            {
               VelocityContext context = new VelocityContext();
               context.put("id", entity.toString());
               context.put("description", URLEncoder.encode(((Describable) entity).getDescription(), "UTF-8"));
               return createURL(context, config.configuration().caseTypeTemplate().get());
            } else if (entity instanceof Label)
            {
               VelocityContext context = new VelocityContext();
               context.put("id", entity.toString());
               context.put("description", ((Describable) entity).getDescription());
               return createURL(context, config.configuration().labelTemplate().get());
            } else
               throw new IllegalArgumentException("Cannot create URL for this type:" + entity.type());
         } catch (UnsupportedEncodingException e)
         {
            throw new IllegalArgumentException("Cannot create URL", e);
         }
      }

      private String createURL(VelocityContext context, String template)
      {
         StringWriter writer = new StringWriter();
         try
         {
            velocity.evaluate(context, writer, "knowledgebase", template);

            return writer.toString();
         } catch (IOException e)
         {
            throw new IllegalArgumentException("Could not create URL", e);
         }
      }
   }
}
