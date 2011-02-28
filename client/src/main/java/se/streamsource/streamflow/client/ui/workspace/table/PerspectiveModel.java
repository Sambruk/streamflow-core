package se.streamsource.streamflow.client.ui.workspace.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.resource.user.profile.PerspectiveValue;
import ca.odell.glazedlists.BasicEventList;

public class PerspectiveModel extends Observable implements Refreshable
{
   @Uses
   CommandQueryClient client;
   
   @Structure
   ValueBuilderFactory vbf;
   
   BasicEventList<LinkValue> possibleLabels = new BasicEventList<LinkValue>();
   List<String> selectedLabels = new ArrayList<String>();

   List<String> selectedStatuses = new ArrayList<String>();
   
   SortBy sortBy = SortBy.none;
   GroupBy groupBy = GroupBy.none;
   
   public void refresh()
   {
      LinksValue listValue = client.query( "possiblelabels",
            LinksValue.class );
      possibleLabels.addAll(listValue.links().get());
   }

   public BasicEventList<LinkValue> getPossibleLabels()
   {
      return possibleLabels;
   }
   
   public List<String> getSelectedLabels()
   {
      return selectedLabels;
   }
   
   public List<String> getSelectedStatuses()
   {
      return selectedStatuses;
   }
   
   public void setSelectedLabels(List<String> selectedLabels)
   {
      this.selectedLabels.clear();
      this.selectedLabels.addAll(selectedLabels);
   }

   public void setSelectedStatuses(List<String> selectedStatuses)
   {
      this.selectedStatuses.clear();
      this.selectedStatuses.addAll(selectedStatuses);
   }
   
   @Override
   public void notifyObservers()
   {
      setChanged();
      super.notifyObservers();
   }

   public PerspectiveValue createPerspective(String name)
   {
      ValueBuilder<PerspectiveValue> builder = vbf.newValueBuilder(PerspectiveValue.class);
      builder.prototype().query().set("Test");
      builder.prototype().name().set(name);
      builder.prototype().labels().set(getSelectedLabels());
      builder.prototype().statuses().set(getSelectedStatuses());
      
      return builder.newInstance();
   }
   
   public void savePerspective(String name)
   {
      client.postCommand("createperspective", createPerspective(name));
   }

   public GroupBy getGroupBy()
   {
      return groupBy;
   }

   public void setGroupBy(GroupBy groupBy)
   {
      this.groupBy = groupBy;
   }

   public SortBy getSortBy()
   {
      return sortBy;
   }

   public void setSortBy(SortBy sortBy)
   {
      this.sortBy = sortBy;
   }
}
