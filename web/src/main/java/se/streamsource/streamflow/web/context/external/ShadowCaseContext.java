package se.streamsource.streamflow.web.context.external;


import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.external.ShadowCaseDTO;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCase;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCases;

import java.io.IOException;

import static se.streamsource.dci.api.RoleMap.*;

@Mixins( ShadowCaseContext.Mixin.class )
public interface ShadowCaseContext
   extends Context, DeleteContext, IndexContext<ShadowCaseDTO>
{
   public void update( ShadowCaseDTO shadowCaseDTO );

   abstract class Mixin
      implements ShadowCaseContext
   {
      @Structure
      Module module;

      public void delete() throws IOException
      {
         role( ShadowCases.class).removeCase( role( ShadowCase.class ) );
      }

      public void update( ShadowCaseDTO shadowCaseDTO )
      {
         ShadowCase shadowCase = role( ShadowCase.class );
         shadowCase.updateContent( shadowCaseDTO.content().get() );
         shadowCase.updateLog( shadowCaseDTO.log().get() );

      }

      public ShadowCaseDTO index()
      {
         ShadowCase caze = role(ShadowCase.class);

         ValueBuilder<ShadowCaseDTO> builder = module.valueBuilderFactory().newValueBuilder( ShadowCaseDTO.class );
         builder.prototype().content().set( ((ShadowCase.Data)caze).content().get() );
         builder.prototype().externalId().set( ((ShadowCase.Data)caze).externalId().get() );
         builder.prototype().systemName().set( ((ShadowCase.Data)caze).systemName().get() );
         builder.prototype().contactId().set( ((ShadowCase.Data)caze).contactId().get() );
         builder.prototype().creationDate().set( ((ShadowCase.Data)caze).creationDate().get() );
         builder.prototype().description().set( caze.getDescription() );
         //builder.prototype().log().set( ((ShadowCase.Data)caze).log().get().get( ((ShadowCase.Data)caze).log().get().size()-1 ) );

         return builder.newInstance();
      }
   }
}
