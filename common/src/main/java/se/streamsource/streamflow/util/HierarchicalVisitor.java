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
package se.streamsource.streamflow.util;

/**
 * TODO
 */
public class HierarchicalVisitor<NODE, LEAF, ThrowableType extends Throwable>
        implements Visitor<LEAF, ThrowableType>
{
    /**
     * Enter an instance of T
     *
     * @param visited the visited instance which is now entered
     * @return true if the visitor pattern should continue, false if it should be aborted for this level
     * @throws ThrowableType if an exception occurred during processing. Any client call that initiated the visiting should
     *                       get the exception in order to handle it properly.
     */
    public boolean visitEnter( NODE visited )
            throws ThrowableType
    {
        return true;
    }

    /**
     * Leave an instance of T
     *
     * @param visited the visited instance which is now left
     * @return true if the visitor pattern should continue, false if it should be aborted for the level of this node
     * @throws ThrowableType if an exception occurred during processing. Any client call that initiated the visiting should
     *                       get the exception in order to handle it properly.
     */
    public boolean visitLeave( NODE visited )
            throws ThrowableType
    {
        return true;
    }

    public boolean visit( LEAF visited ) throws ThrowableType
    {
        return true;
    }
}
