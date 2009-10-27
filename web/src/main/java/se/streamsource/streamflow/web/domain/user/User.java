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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Matches;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
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
    boolean login(String password);

    void changePassword(String currentPassword, @Matches("\\w+{6,30}")String newPassword) throws WrongPasswordException;

    void resetPassword(String password);

    void changeEnabled(boolean enabled);

    interface UserState
    {
        Property<String> userName();

        Property<String> hashedPassword();

        @UseDefaults
        Property<Boolean> disabled();

        boolean isCorrectPassword(String password);

        String hashPassword(String password);

        boolean isAdministrator();


        void passwordChanged(DomainEvent event, String hashedPassword);

        void failedLogin(DomainEvent event);

        void enabledChanged(DomainEvent event, boolean enabled);
    }

    abstract class UserMixin
            implements User, UserState
    {
        @This
        UserState state;

        public boolean login(String password)
        {
            if (disabled().get())
                return false;

            boolean correct = isCorrectPassword(password);

            if (!correct)
                failedLogin(DomainEvent.CREATE);

            return correct;
        }

        public void changeEnabled(boolean enabled)
        {
            if (enabled == disabled().get())
            {
                enabledChanged(DomainEvent.CREATE, !enabled);
            }
        }

        public void changePassword(String currentPassword, String newPassword) throws WrongPasswordException
        {
            // Check if current password is correct
            if (!isCorrectPassword(currentPassword))
            {
                throw new WrongPasswordException();
            }

            passwordChanged(DomainEvent.CREATE, hashPassword(newPassword));
        }

        public void resetPassword(String password)
        {
            passwordChanged(DomainEvent.CREATE,  hashPassword(password));
        }

        public void passwordChanged(DomainEvent event, String hashedPassword)
        {
            hashedPassword().set(hashedPassword);
        }

        public void failedLogin(DomainEvent event)
        {
        }

        public void enabledChanged(DomainEvent event, boolean enabled)
        {
            state.disabled().set(enabled);
        }

        public boolean isCorrectPassword(String password)
        {
            return hashedPassword().get().equals(hashPassword(password));
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
                throw new IllegalStateException("No SHA algorithm founde", e);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public boolean isAdministrator()
        {
            return userName().get().equals("administrator");
        }
    }
}
