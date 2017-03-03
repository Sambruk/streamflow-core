package se.streamsource.streamflow.web.application.entityexport;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.PropertyInfo;

/**
 * Created by ruslan on 03.03.17.
 */
public class EntityExportHelper
{

   private EntityInfo entityInfo;

   private EntityExportHelper()
   {
   }

   private EntityExportHelper( EntityInfo entityInfo )
   {
      this.entityInfo = entityInfo;
   }

   public static EntityExportHelper from( EntityInfo entityInfo) {
      return new EntityExportHelper( entityInfo );
   }



}
