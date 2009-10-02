/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.users.workspace.user.task.comments;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.comment.CommentDTO;
import se.streamsource.streamflow.resource.comment.CommentsDTO;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;
import se.streamsource.streamflow.web.domain.comment.CommentValue;
import se.streamsource.streamflow.web.domain.comment.Commentable;
import se.streamsource.streamflow.web.domain.comment.Commenter;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/workspace/user/{view}/{task}/comments
 */
public class TaskCommentsServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public TaskCommentsServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Get task comments"));
        ValueBuilder<CommentsDTO> builder = vbf.newValueBuilder(CommentsDTO.class);
        ValueBuilder<CommentDTO> commentBuilder = vbf.newValueBuilder(CommentDTO.class);
        List<CommentDTO> list = builder.prototype().comments().get();
        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
        for (CommentValue commentValue : task.comments().get())
        {
            commentBuilder.prototype().creationDate().set(commentValue.creationDate().get());
            Commenter commenter = uow.get(Commenter.class, commentValue.commenter().get().identity());
            commentBuilder.prototype().commenter().set(commenter.getDescription());
            commentBuilder.prototype().text().set(commentValue.text().get());
            commentBuilder.prototype().isPublic().set(commentValue.isPublic().get());
            list.add(commentBuilder.newInstance());
        }
        uow.discard();

        return new StringRepresentation(builder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
    }

    public void addComment(NewCommentCommand comment) throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        //NewCommentCommand comment = vbf.newValueFromJSON(NewCommentCommand.class, representation.getText());
        Commentable commentable = uow.get(Commentable.class, getRequest().getAttributes().get("task").toString());
        ValueBuilder<CommentValue> builder = vbf.newValueBuilder(CommentValue.class);
        CommentValue prototype = builder.prototype();
        prototype.commenter().set(comment.commenter().get());
        prototype.creationDate().set(comment.creationDate().get());
        prototype.text().set(comment.text().get());
        prototype.isPublic().set(comment.isPublic().get());
        commentable.addComment(builder.newInstance());
    }
}