package se.streamsource.streamflow.web.context.workspace.cases;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.CaseSearchResult;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.SubCases;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;

/**
 * Created by dmizem from Ubrainians for imCode on 15.05.18.
 */
public class SubCasesContext implements IndexContext<LinksValue> {
    @Structure
    Module module;

    public CaseSearchResult cases() {
        return new CaseSearchResult(RoleMap.role(SubCases.Data.class).subCases().toList());
    }


    public LinksValue index() {
        LinksBuilder links = new LinksBuilder(module.valueBuilderFactory());
//        ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder(MessageDTO.class);
//        ConversationEntity conversation = RoleMap.role(ConversationEntity.class);
//
//        ResourceBundle bundle = ResourceBundle.getBundle(se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext.class.getName(), RoleMap.role(Locale.class));
//        Map<String, String> translations = new HashMap<String, String>();
//        for (String key : bundle.keySet()) {
//            translations.put(key, bundle.getString(key));
//        }
//
//        for (Message message : conversation.messages()) {
//            Contactable contact = module.unitOfWorkFactory().currentUnitOfWork().get(Contactable.class, EntityReference.getEntityReference(((MessageEntity) message).sender().get()).identity());
//            String sender = contact.getContact().name().get();
//            builder.prototype().sender().set(!"".equals(sender)
//                    ? sender
//                    : EntityReference.getEntityReference(((MessageEntity) message).sender().get()).identity());
//            builder.prototype().createdOn().set(((MessageEntity) message).createdOn().get());
//
//            String text = message.translateBody(translations);
//
//            if (MessageType.HTML.equals(((MessageEntity) message).messageType().get())) {
//                text = Translator.htmlToText(text);
//            }
//
//            builder.prototype().text().set(text);
//            builder.prototype().href().set(EntityReference.getEntityReference(message).identity());
//            builder.prototype().id().set(EntityReference.getEntityReference(message).identity());
//            builder.prototype().hasAttachments().set(message.hasAttachments());
//            builder.prototype().unread().set(message.isUnread());
//
//            links.addLink(builder.newInstance());
//        }
        return links.newLinks();

    }

    @RequiresStatus(OPEN)
    @RequiresPermission(PermissionType.write)
    public void createsubcase() {
        RoleMap.role(SubCases.class).createSubCase();
    }

    @RequiresPermission(PermissionType.write)
    public void removesubCase(Case subCase) {
        RoleMap.role(SubCases.class).removeSubCase(subCase);
    }
}