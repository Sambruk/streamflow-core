package se.streamsource.streamflow.web.infrastructure.index;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.entity.EntityStateSerializer;

public class EmbeddedSolrAssembler implements Assembler
{

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      Application.Mode mode = module.layerAssembly().applicationAssembly().mode();
      if (!mode.equals( Application.Mode.test ))
      {
         module.addServices( EmbeddedSolrService.class ).visibleIn( Visibility.application ).instantiateOnStartup();
         module.addServices( SolrQueryService.class ).visibleIn( Visibility.application ).identifiedBy( "solr" ).instantiateOnStartup();

         module.addObjects( EntityStateSerializer.class );
      }
   }
}
