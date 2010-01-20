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

package se.streamsource.streamflow.client.util;

/**
 * JAVADOC
 */
public class StringUtils
{
	/**
	 * Truncates <code>toTruncate</code> to <code>length</code> number of characters.
	 * 
	 * @param	toTruncate	the string to truncate
	 * @param	length		the number of characters the truncated string will consist of
	 * @return the truncated string.
	 */
	public static String truncate(String toTruncate, int length)
	{
		if (toTruncate == null || toTruncate.length() <= length )
		{
			return toTruncate;
		}

		return toTruncate.substring(0, length);
	}
	
	/**
	 * Truncates <code>toTruncate</code> to <code>length</code> number of characters
	 * and appends three truncation periods "..." at the end of the string.
	 * 
	 * @param	toTruncate	the string to truncate
	 * @param	length		the number of characters the truncated string will consist of
	 * @return the truncated string with three periods "..." appended.
	 */
	public static String truncateWithPeriods(String toTruncate, int length)
	{
		StringBuilder builder = new StringBuilder(truncate(toTruncate, length));
		builder.append("...");
		return builder.toString();
	}
}
