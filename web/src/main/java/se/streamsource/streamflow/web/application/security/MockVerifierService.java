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

package se.streamsource.streamflow.web.application.security;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.security.Verifier;

/**
 * Accept login if username==password
 */
public class MockVerifierService
        extends Verifier
{
    public int verify(Request request, Response response)
    {
        ChallengeResponse challengeResponse = request.getChallengeResponse();
        if (challengeResponse == null)
            return Verifier.RESULT_MISSING;

        String username = challengeResponse.getIdentifier();
        String password = new String(request.getChallengeResponse().getSecret());

        if (username.equals(password))
        {
            return Verifier.RESULT_VALID;
        } else
        {
            return Verifier.RESULT_INVALID;
        }
    }
}
