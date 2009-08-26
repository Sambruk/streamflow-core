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

package se.streamsource.streamflow.web.domain.user;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * JAVADOC
 */
@Mixins(User.UserMixin.class)
public interface User
{
    boolean verifyPassword(String password);

    void changePassword(String currentPassword, String newPassword) throws WrongPasswordException;

    @Mixins(UserStateMixin.class)
    interface UserState
    {
        //        @Immutable
        Property<String> userName();

        Property<String> hashedPassword();

        void passwordChanged(String hashedPassword);

        boolean isCorrectPassword(String password);

        String hashPassword(String password);
    }

    class UserMixin
            implements User
    {
        @This
        UserState state;

        public boolean verifyPassword(String password)
        {
            return state.isCorrectPassword(password);
        }

        public void changePassword(String currentPassword, String newPassword) throws WrongPasswordException
        {
            // Check if current password is correct
            if (!state.isCorrectPassword(currentPassword))
            {
                throw new WrongPasswordException();
            }

            state.passwordChanged(state.hashPassword(newPassword));
        }
    }

    abstract class UserStateMixin
        implements UserState
    {
        @This UserState state;

        public void passwordChanged(String hashedPassword)
        {
            state.hashedPassword().set(hashedPassword);
        }

        public boolean isCorrectPassword(String password)
        {
            return state.hashedPassword().get().equals(hashPassword(password));
        }

        public String hashPassword(String password)
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(password.getBytes("UTF-8"));
                byte raw[] = md.digest();
                String hash = (new BASE64Encoder()).encode(raw);
                return hash;
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new IllegalStateException("No SHA algorithm founde",e);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
