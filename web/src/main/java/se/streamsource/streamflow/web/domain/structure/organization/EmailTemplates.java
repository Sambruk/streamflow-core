package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Map;

/**
 * TODO
 */
public interface EmailTemplates
{
   void updateTemplate(String key, String template);

   Map<String, String> getTemplates();

   interface Data
   {
      Property<Map<String, String>> templates();

      void updatedTemplate(@Optional DomainEvent event, String key, String template);
   }

   abstract class Mixin
      implements EmailTemplates, Data
   {
      @This
      Data data;

      public void updateTemplate(String key, String template)
      {
         data.updatedTemplate(null, key, template);
      }

      public void updatedTemplate(@Optional DomainEvent event, String key, String template)
      {
         Map<String, String> templates = data.templates().get();
         templates.put(key, template);
         data.templates().set(templates);
      }

      public Map<String, String> getTemplates()
      {
         return data.templates().get();
      }
   }
}
