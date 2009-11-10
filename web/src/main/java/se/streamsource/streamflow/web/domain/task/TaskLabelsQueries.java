package se.streamsource.streamflow.web.domain.task;

import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.structure.ModuleSPI;

import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.label.LabelEntity;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.label.Labels;

@Mixins(TaskLabelsQueries.Mixin.class)
public interface TaskLabelsQueries
{
	ListValue ownerLabels();
	
	ListValue organizationLabels(String prefix);
	
	class Mixin
	implements TaskLabelsQueries 
	{
		@This Ownable.Data ownable;
		
		@This Labelable.Data labelable;
		
		@Structure QueryBuilderFactory qbf;
		
		@Structure ValueBuilderFactory vbf;
		
		@Structure UnitOfWorkFactory uowf;
		
	    @Structure
	    protected ModuleSPI module;

		public ListValue organizationLabels(String prefix)
		{
	        UnitOfWork uow = uowf.currentUnitOfWork();

	        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
	        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

	        if (prefix.length() > 0)
	        {
	            QueryBuilder<LabelEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(LabelEntity.class);
	            Query<LabelEntity> labels = queryBuilder.where(matches(
	                    templateFor(LabelEntity.class).description(), "^" + prefix)).
	                    newQuery(uow);

	            try
	            {
	                for (Label label : labels)
	                {
	                    builder.prototype().entity().set(EntityReference.getEntityReference(label));
	                    listBuilder.addListItem(label.getDescription(), builder.newInstance().entity().get());
	                }
	            } catch (Exception e)
	            {
	                e.printStackTrace();
	            }
	        }
			return listBuilder.newList();
		}

		public ListValue ownerLabels()
		{
			ListValueBuilder listBuilder = new ListValueBuilder(vbf);
			if(ownable.owner().get() instanceof Labels)
			{
				for (Label label : ((Labels.Data)ownable.owner().get()).labels()) 
				{
					if(!labelable.labels().contains((LabelEntity)label)) 
					{
						listBuilder.addDescribable(label);
					}
				}
			}
			return listBuilder.newList();
		}
		
	}
}
