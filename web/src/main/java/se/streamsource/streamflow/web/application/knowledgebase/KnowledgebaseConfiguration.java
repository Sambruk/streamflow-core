package se.streamsource.streamflow.web.application.knowledgebase;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * Configuration of knowledgebase integration
 */
public interface KnowledgebaseConfiguration
      extends ConfigurationComposite, Enabled
{
   @UseDefaults
   Property<String> caseTypeTemplate();

   @UseDefaults
   Property<String> labelTemplate();
}
