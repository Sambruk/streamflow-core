/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.TreeList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.qi4j.api.util.Iterables.matchesAny;

/**
 * JAVADOC
 */
public class AdministrationModel
   extends ResourceModel<LinksValue>
      implements Refreshable, TransactionListener
{
   @Structure
   Module module;

   private EventList<LinkValue> links = new TransactionList<LinkValue>(new BasicEventList<LinkValue>());
   private TreeList<LinkValue> linkTree = new TreeList<LinkValue>(links, new LinkValueFormat(), TreeList.NODES_START_EXPANDED);

   public AdministrationModel()
   {
      relationModelMapping("organization", OrganizationModel.class);
      relationModelMapping("organizationalunit", OrganizationalUnitModel.class);
   }

   public void refresh()
   {
      super.refresh();

      LinksValue administration = getIndex();
      EventListSynch.synchronize(administration.links().get(), links);

      linkTree = new TreeList<LinkValue>(links, new LinkValueFormat(), TreeList.NODES_START_EXPANDED);
   }

   public TreeList<LinkValue> getLinkTree()
   {
      return linkTree;
   }

   public void changeDescription( Object node, String newDescription )
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();

      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
      builder.prototype().string().set( newDescription );
      client.getClient(link).postCommand( "changedescription", builder.newInstance() );
   }

   public void createOrganizationalUnit( LinkValue node, String name )
   {
      Form form = new Form();
      form.set( "name", name );
      client.getClient(node).postCommand( "create", form );
   }

   public void removeOrganizationalUnit( Object node )
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();
      try
      {
         client.getClient(link).delete();
      } catch (ResourceException e)
      {
         if (Status.SERVER_ERROR_INTERNAL.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_remove_organisation_with_open_projects, e);

         }
      }
   }

   public EventList<LinkValue> possibleMoveTo(Object node)
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();

      EventList<LinkValue> links = new BasicEventList<LinkValue>();
      EventListSynch.synchronize(client.getClient(link).query( "possiblemoveto", LinksValue.class ).links().get(), links);
      return links;
   }

   public void move(Object node, LinkValue moveTo)
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();

      client.getClient(link).postLink( moveTo );
   }

   public EventList<LinkValue> possibleMergeWith(Object node)
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();
      EventList<LinkValue> links = new BasicEventList<LinkValue>();
      EventListSynch.synchronize(client.getClient(link).query( "possiblemergewith", LinksValue.class ).links().get(), links);
      return links;
   }

   public void merge(Object node, LinkValue mergeWith)
   {
      TreeList.Node treeNode = (TreeList.Node) node;
      LinkValue link = (LinkValue) treeNode.getElement();
      client.getClient(link).postLink( link );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
// TODO       if (Events.matches( transactions, Events.onEntities( )))
      refresh();
   }

   public boolean isParticipantInGroup( String groupId, String participantId )
   {
      Form form = new Form( );
      form.add( "groupid", groupId );
      form.add( "participantid", participantId );
      return new Boolean( client.query( "isparticipantingroup", String.class, form ) );
   }

   private class LinkValueFormat implements TreeList.Format<LinkValue>
   {
      public void getPath(List<LinkValue> linkValues, LinkValue linkValue)
      {
         String classes = linkValue.classes().get();
         if (classes != null)
         {
            for (final LinkValue value : links)
            {
               if (matchesAny( new Specification<String>()
               {
                  public boolean satisfiedBy( String item )
                  {
                     return item.equals( value.id().get() );
                  }
               }, asList( classes.split( " " ) ) ))
               {
                  getPath(linkValues, value);
                  break;
               }
            }
         }
         linkValues.add(linkValue);
      }

      public boolean allowsChildren(LinkValue linkValue)
      {
         for ( final LinkValue link : links)
         {
            String classes = link.classes().get();
            if (classes != null && matchesAny( new Specification<String>()
            {
               public boolean satisfiedBy( String item )
               {
                  return item.equals( link.id().get() );
               }
            }, asList( classes.split( " " ) ) ))
               return true;
         }
         return false;
      }

      public Comparator<? extends LinkValue> getComparator(int i)
      {
         return null;
      }
   }
}
