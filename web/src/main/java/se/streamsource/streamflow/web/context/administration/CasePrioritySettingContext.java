package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.FormValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CasePrioritySetting;

/**
 * Context for case priority settings.
 */
public class CasePrioritySettingContext
   implements IndexContext<FormValue>
{
   @Structure
   Module module;
   
   @Uses
   CasePrioritySetting prioritySetting;
   
   @Uses
   CasePrioritySetting.Data prioritySettingData;
   
   public FormValue index()
   {
      ValueBuilder<FormValue> builder = module.valueBuilderFactory().newValueBuilder( FormValue.class );
      builder.prototype().form().get().put( "visible", prioritySettingData.visible().get().toString() );
      builder.prototype().form().get().put( "mandatory", prioritySettingData.mandatory().get().toString() );
      return builder.newInstance();
   }

   public void update( @Name( "visible") Boolean visible, @Name( "mandatory" ) Boolean mandatory )
   {
      prioritySetting.changeCasePrioritySetting( visible, mandatory );
   }

}
