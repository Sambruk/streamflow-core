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

package se.streamsource.streamflow.web.infrastructure.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Utility for reading template files from resources
 */
public class TemplateUtil
{
    public static String getTemplate(String resourceName, Class resourceClass) throws IOException
    {
        String template = "";
        InputStream in = resourceClass.getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null)
            template += line + "\n";
        reader.close();

        return template;
    }

    public static String eval(String template, String... parameters)
    {
        for (int i = 0; i < parameters.length; i += 2)
        {
            String variable = parameters[i];
            String value = parameters[i + 1];

            template = template.replace(variable, value);
        }

        return template;
    }

    public static String methodList(Class methodClass)
    {
        // List methods
        String links = "";
        Method[] methods = methodClass.getMethods();
        for (Method method : methods)
        {
            links += "<li><a href=\"" + method.getName() + "/\" rel=\"" + method.getName() + "\">" + method.getName() + "</a></li>\n";
        }

        return links;
    }
}
