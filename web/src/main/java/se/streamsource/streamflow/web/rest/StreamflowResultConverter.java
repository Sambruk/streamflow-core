/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.rest;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Request;
import org.restlet.data.Form;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.server.ResultConverter;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;

import java.util.Collections;

import static se.streamsource.dci.value.table.TableValue.*;

/**
 * JAVADOC
 */
public class StreamflowResultConverter
      implements ResultConverter
{
   @Structure
   Module module;

   @Service
   ServiceReference<KnowledgebaseService> knowledgeBaseService;

   public Object convert(Object result, Request request, Object[] arguments)
   {
      if (result instanceof String)
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
         builder.prototype().string().set((String) result);
         return builder.newInstance();
      } else if (result instanceof Form)
      {
         return result;
      } else if (result instanceof Case)
      {
         if (arguments.length > 0 && arguments[0] instanceof TableQuery)
            return caseTable(Collections.singleton((CaseEntity) result), module, request, arguments);
         else
            //needs to be relative path in case there is a proxy in front of streamflow server
            return caseDTO((CaseEntity) result, module, request.getResourceRef().getBaseRef().getPath());
      } else if (result instanceof Query)
      {
         Query query = (Query) result;
         if (query.resultType().equals(Case.class))
         {
            if (arguments.length > 0 && arguments[0] instanceof TableQuery)
               return caseTable(query, module, request, arguments);
            else
               //same here relative path needed
               return buildCaseList(query, module, request.getResourceRef().getBaseRef().getPath());
         }
      }

      if (result instanceof Iterable)
      {
         Iterable iterable = (Iterable) result;
         return new LinksBuilder(module.valueBuilderFactory()).rel("resource").addDescribables(iterable).newLinks();
      }

      return result;
   }

   private LinksValue buildCaseList(Iterable<Case> query, Module module, String basePath)
   {
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory()).path(basePath);
      for (Case aCase : query)
      {
         try
         {
            linksBuilder.addLink(caseDTO((CaseEntity) aCase, module, basePath));
         } catch (Exception e)
         {
            LoggerFactory.getLogger(getClass()).error("Could not create link for case:" + ((Identity) aCase).identity().get(), e);
         }
      }
      return linksBuilder.newLinks();
   }

   private CaseDTO caseDTO(CaseEntity aCase, Module module, String basePath)
   {
      ValueBuilder<CaseDTO> builder = module.valueBuilderFactory().newValueBuilder(CaseDTO.class);

      CaseDTO prototype = builder.prototype();

      prototype.id().set(aCase.identity().get());
      prototype.creationDate().set(aCase.createdOn().get());
      if (aCase.createdBy().get() != null)
         prototype.createdBy().set(((Describable) aCase.createdBy().get()).getDescription());
      if (aCase.caseId().get() != null)
         prototype.caseId().set(aCase.caseId().get());
      prototype.href().set(basePath + "/workspace/cases/" + aCase.identity().get() + "/");
      prototype.rel().set("case");
      try
      {
         if (aCase.owner().get() != null)
            prototype.owner().set(((Describable) aCase.owner().get()).getDescription());
      } catch (Exception e)
      {
         // Ignore
      }
      prototype.status().set(aCase.status().get());
      prototype.text().set(aCase.description().get());

      if (aCase.caseType().get() != null)
      {
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
         linkBuilder.prototype().text().set(aCase.caseType().get().getDescription());
         linkBuilder.prototype().id().set(aCase.caseType().get().toString());

         if (knowledgeBaseService.isAvailable())
         {
            try
            {
               linkBuilder.prototype().href().set(knowledgeBaseService.get().createURL((EntityComposite) aCase.caseType().get()));
            } catch (Exception e)
            {
               LoggerFactory.getLogger(getClass()).error("Could not create link for case type:" + aCase.caseType().get().getDescription(), e);
            }
         } else
         {
            // TODO What to do here?
            linkBuilder.prototype().href().set("");
         }
         prototype.caseType().set(linkBuilder.newInstance());
      }

      if (aCase.isAssigned())
         prototype.assignedTo().set(((Describable) aCase.assignedTo().get()).getDescription());

      if (aCase.isStatus(CaseStates.CLOSED) && aCase.resolution().get() != null)
         prototype.resolution().set(aCase.resolution().get().getDescription());

      prototype.hasContacts().set(aCase.hasContacts());
      prototype.hasConversations().set(aCase.hasConversations());
      prototype.hasSubmittedForms().set(aCase.hasSubmittedForms());
      prototype.hasAttachments().set(aCase.hasAttachments());

      // Labels
      LinksBuilder labelsBuilder = new LinksBuilder(module.valueBuilderFactory()).command("delete");
      for (Label label : aCase.labels())
      {
         labelsBuilder.addDescribable(label);
      }
      prototype.labels().set(labelsBuilder.newLinks());

      // Subcases
      LinksBuilder subcasesBuilder = new LinksBuilder(module.valueBuilderFactory());
      subcasesBuilder.path(basePath + "/workspace/cases");
      try
      {
         for (Case subCase : aCase.subCases())
         {
            subcasesBuilder.classes(((Status.Data) subCase).status().get().name());
            subcasesBuilder.addDescribable(subCase);
         }
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      prototype.subcases().set(subcasesBuilder.newLinks());

      Case parentCase = aCase.parent().get();
      if (parentCase != null)
      {
         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
         linkBuilder.prototype().id().set(parentCase.toString());
         linkBuilder.prototype().rel().set("parent");
         linkBuilder.prototype().href().set("../" + parentCase.toString() + "/");
         linkBuilder.prototype().text().set(((CaseId.Data) parentCase).caseId().get());
         prototype.parentCase().set(linkBuilder.newInstance());
      }

      return builder.newInstance();
   }

   private TableValue caseTable(Iterable<CaseEntity> cases, final Module module, final Request request, Object[] arguments)
   {
      //needs relative path in case there is a proxy in front of streamflow server
      final String basePath = request.getResourceRef().getBaseRef().getPath();
      TableQuery query = (TableQuery) arguments[0];

      TableBuilderFactory tbf = new TableBuilderFactory(module.valueBuilderFactory());

      return tbf.column("description", "Description", STRING, new Function<CaseEntity, Object>()
      {
         public Object map(CaseEntity caseEntity)
         {
            return caseEntity.getDescription();
         }
      }).
            column("created", "Created", DATETIME, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.createdOn().get();
               }
            }).
            column("creator", "Creator", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return ((Describable) caseEntity.createdBy().get()).getDescription();
               }
            }).
            column("due", "Due on", DATETIME, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.dueOn().get();
               }
            }).
            column("caseid", "Case id", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.caseId().get();
               }
            }).
            column("href", "Location", STRING, new Function<CaseEntity, Object>()
                  {
                     public Object map(CaseEntity caseEntity)
                     {
                        return basePath + "/workspace/cases/" + caseEntity.identity().get() + "/";
                     }
                  }, new Function<CaseEntity, String>()
                  {
                     public String map(CaseEntity caseEntity)
                     {
                        return "View case";
                     }
                  }
            ).
            column("owner", "Owner", STRING, new Function<CaseEntity, Object>()
                  {
                     public Object map(CaseEntity caseEntity)
                     {
                        return caseEntity.owner().get();
                     }
                  }, new Function<CaseEntity, String>()
            {
               public String map(CaseEntity caseEntity)
               {
                  Owner owner = caseEntity.owner().get();
                  return owner == null ? null : ((Describable) owner).getDescription();
               }
            }
            ).
            column("status", "Status", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.status().get().name();
               }
            }).
            column("casetype", "Case type", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  CaseType caseType = caseEntity.caseType().get();
                  return caseType == null ? null : caseType.getDescription();
               }
            }).
            column("resolution", "Resolution", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  Resolution resolution = caseEntity.resolution().get();
                  return resolution == null ? null : resolution.getDescription();
               }
            }).
            column("assigned", "Assigned", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  Assignee assignee = caseEntity.assignedTo().get();
                  return assignee == null ? null : ((Describable) assignee).getDescription();
               }
            }).
            column("hascontacts", "Has contacts", BOOLEAN, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.hasContacts();
               }
            }).
            column("hasconversations", "Has conversations", BOOLEAN, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.hasConversations();
               }
            }).
            column("hasattachments", "Has attachments", BOOLEAN, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.hasAttachments();
               }
            }).
            column("hassubmittedforms", "Has submitted forms", BOOLEAN, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  return caseEntity.hasSubmittedForms();
               }
            }).
            column("labels", "Labels", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  LinksBuilder labelsBuilder = new LinksBuilder(module.valueBuilderFactory()).command("delete");
                  for (Label label : caseEntity.labels())
                  {
                     labelsBuilder.addDescribable(label);
                  }
                  LinksValue linksValue = labelsBuilder.newLinks();
                  return linksValue;
               }
            }).
            column("subcases", "Subcases", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  LinksBuilder subcasesBuilder = new LinksBuilder(module.valueBuilderFactory());
                  subcasesBuilder.path(basePath + "/workspace/cases");
                  try
                  {
                     for (Case subCase : caseEntity.subCases())
                     {
                        subcasesBuilder.classes(((Status.Data) subCase).status().get().name());
                        subcasesBuilder.addDescribable(subCase);
                     }
                  } catch (Exception e)
                  {
                     e.printStackTrace();
                  }
                  LinksValue linksValue = subcasesBuilder.newLinks();
                  return linksValue;
               }
            }).
            column("parent", "Parent case", STRING, new Function<CaseEntity, Object>()
            {
               public Object map(CaseEntity caseEntity)
               {
                  Case parentCase = caseEntity.parent().get();
                  if (parentCase != null)
                  {
                     ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
                     linkBuilder.prototype().id().set(parentCase.toString());
                     linkBuilder.prototype().rel().set("parent");
                     linkBuilder.prototype().href().set(parentCase.toString() + "/");
                     linkBuilder.prototype().text().set(((CaseId.Data) parentCase).caseId().get());
                     return linkBuilder.newInstance();
                  } else
                     return null;
               }
            }).newInstance(query).rows(cases).newTable();

/*      TableBuilder table = new TableBuilder(module.valueBuilderFactory(), columns, tableQuery);

      String select = "*".equals(query.select().trim()) ? "description,created,creator,due,caseid,href,owner,status,casetype,resolution,assigned,hascontacts,hasconversations,hasattachments,hassubmittedforms,labels,subcases,parent" : query.select();
      String[] columns = select.split("[, ]");

      // Columns
      for (String column : columns)
      {
         // TODO Make type setting smarter
         if ("created".equals(column))
            table.column(column, Strings.humanReadable(column), "date");
         else if ("due".equals(column))
            table.column(column, Strings.humanReadable(column), "date");
         else if ("hascontacts".equals(column))
            table.column(column, Strings.humanReadable(column), "boolean");
         else if ("hasconversations".equals(column))
            table.column(column, Strings.humanReadable(column), "boolean");
         else if ("hasattachments".equals(column))
            table.column(column, Strings.humanReadable(column), "boolean");
         else if ("hassubmittedforms".equals(column))
            table.column(column, Strings.humanReadable(column), "boolean");
         else
            table.column(column, Strings.humanReadable(column), "string");
      }

      // Data
      for (CaseEntity caseEntity : cases)
      {
         try
         {
            table.row();
// "description,created,creator,caseid,href,owner,status,casetype,resolution,assigned,hascontacts,hasconversations,hasattachments,hassubmittedforms,labels,subcases,parent"
            for (String column : columns)
            {
               if (column.equals("description"))
                  table.cell(caseEntity.identity().get(), caseEntity.description().get());
               else if (column.equals("created"))
                  table.cell(caseEntity.createdOn().get(), DateFunctions.toUtcString(caseEntity.createdOn().get()));
               else if (column.equals("due"))
               {
                  Date dueDate = ;
                  table.cell(dueDate, dueDate == null ? null : DateFunctions.toUtcString(dueDate));
               } else if (column.equals("creator"))
               {
                  Creator v = ;
                  table.cell(v, v == null ? "" : ((Describable) v).getDescription());
               } else if (column.equals("caseid"))
               {
                  table.cell(caseEntity.caseId().get(), caseEntity.caseId().get());
               } else if (column.equals("href"))
               {
                  String href = request.getResourceRef().getBaseRef().getPath() + "/workspace/cases/" + caseEntity.identity().get() + "/";
                  table.cell(href, href);
               } else if (column.equals("owner"))
               {
                  Owner owner = ;
                  table.cell(owner, owner == null ? null : ((Describable) owner).getDescription());
               } else if (column.equals("status"))
               {
                  table.cell(caseEntity.status().get().name(), Strings.humanReadable(caseEntity.status().get().name()));
               } else if (column.equals("casetype"))
               {
                  CaseType caseType = caseEntity.caseType().get();
                  table.cell(caseType, caseType == null ? null : caseType.getDescription());
               } else if (column.equals("resolution"))
               {
                  Resolution resolution = caseEntity.resolution().get();
                  table.cell(resolution, resolution == null ? null : resolution.getDescription());
               } else if (column.equals("assigned"))
               {
                  Assignee assignee = caseEntity.assignedTo().get();
                  table.cell(assignee, assignee == null ? null : ((Describable) assignee).getDescription());
               } else if (column.equals("hascontacts"))
               {
                  table.cell(caseEntity.hasContacts(), Boolean.toString(caseEntity.hasContacts()));
               } else if (column.equals("hasconversations"))
               {
                  table.cell(caseEntity.hasConversations(), Boolean.toString(caseEntity.hasConversations()));
               } else if (column.equals("hassubmittedforms"))
               {
                  table.cell(caseEntity.hasSubmittedForms(), Boolean.toString(caseEntity.hasSubmittedForms()));
               } else if (column.equals("hasattachments"))
               {
                  table.cell(caseEntity.hasAttachments(), Boolean.toString(caseEntity.hasAttachments()));
               } else if (column.equals("labels"))
               {
                  LinksBuilder labelsBuilder = new LinksBuilder(module.valueBuilderFactory()).command("delete");
                  for (Label label : caseEntity.labels())
                  {
                     labelsBuilder.addDescribable(label);
                  }
                  LinksValue linksValue = labelsBuilder.newLinks();
                  table.cell(linksValue, null);
               } else if (column.equals("subcases"))
               {
                  LinksBuilder subcasesBuilder = new LinksBuilder(module.valueBuilderFactory());
                  subcasesBuilder.path("..");
                  try
                  {
                     for (Case subCase : caseEntity.subCases())
                     {
                        subcasesBuilder.classes(((Status.Data) subCase).status().get().name());
                        subcasesBuilder.addDescribable(subCase);
                     }
                  } catch (Exception e)
                  {
                     e.printStackTrace();
                  }
                  LinksValue linksValue = subcasesBuilder.newLinks();
                  table.cell(linksValue, null);
               } else if (column.equals("parent"))
               {
                  Case parentCase = caseEntity.parent().get();
                  if (parentCase != null)
                  {
                     ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
                     linkBuilder.prototype().id().set(parentCase.toString());
                     linkBuilder.prototype().rel().set("parent");
                     linkBuilder.prototype().href().set("../" + parentCase.toString() + "/");
                     linkBuilder.prototype().text().set(((CaseId.Data) parentCase).caseId().get());
                     table.cell(linkBuilder.newInstance(), null);
                  } else
                  {
                     table.cell(null, null);
                  }
               } else
               {
                  throw new ResourceException(org.restlet.data.Status.SERVER_ERROR_INTERNAL, "Unhandled column name:" + column);
               }
            }
         } catch (Exception ex)
         {
            // Could not create row for this case, for some reason
            LoggerFactory.getLogger(getClass()).error("Could not create row for case:" + caseEntity.identity().get(), ex);
            table.abortRow();
         }
      }

      return table.newTable();*/
   }
}
