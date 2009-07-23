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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Service;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.resource.users.shared.user.task.comments.SharedUserTaskCommentsClientResource;
import se.streamsource.streamflow.resource.comment.CommentsDTO;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;

import javax.swing.*;
import java.io.IOException;

/**
 * JAVADOC
 */
public class TaskCommentsModel
    extends AbstractListModel
{
    @Service
    UncaughtExceptionHandler exception;

    private SharedUserTaskCommentsClientResource commentsClientResource;

    CommentsDTO comments;

    public void refresh() throws IOException, ResourceException
    {
        comments = commentsClientResource.comments();
        fireContentsChanged(this, 0, getSize());
    }

    public int getSize()
    {
        if (comments == null)
            return 0;
        else
            return comments.comments().get().size();
    }

    public Object getElementAt(int index)
    {
        return comments.comments().get().get(index);
    }

    public void addComment(NewCommentCommand command) throws ResourceException, IOException
    {
        commentsClientResource.addComment(command);
        refresh();
    }

    public void setResource(SharedUserTaskCommentsClientResource commentsClientResource) throws IOException, ResourceException
    {
        this.commentsClientResource = commentsClientResource;
    }
}
