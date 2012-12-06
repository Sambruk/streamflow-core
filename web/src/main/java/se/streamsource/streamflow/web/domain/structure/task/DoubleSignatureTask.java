package se.streamsource.streamflow.web.domain.structure.task;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;

/**
 * 
 *
 */
@Mixins(DoubleSignatureTask.Mixin.class)
public interface DoubleSignatureTask
{

   interface Data
   {
      Association<Case> caze();

      Property<SubmittedFormValue> submittedForm();

      Association<FormDraft> formDraft();
   }

   abstract public class Mixin implements Data
   {

   }

}
