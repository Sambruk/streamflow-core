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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskCommentsClientResource;
import se.streamsource.streamflow.resource.comment.CommentDTO;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;

import javax.swing.AbstractListModel;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JAVADOC
 */
public class TaskCommentsModel
    extends AbstractListModel
{
    @Uses
    TaskCommentsClientResource commentsClientResource;

    List<CommentDTO> comments = Collections.emptyList();

    public void refresh() throws IOException, ResourceException
    {
        comments = commentsClientResource.comments().comments().get();
        fireContentsChanged(this, 0, getSize());
    }

    public int getSize()
    {
        return comments.size();
    }

    public Object getElementAt(int index)
    {
        return comments.get(index);
    }

    public void addComment(NewCommentCommand command) throws ResourceException, IOException
    {
        commentsClientResource.addComment(command);
        refresh();
    }
}
