package se.streamsource.streamflow.client;

import javax.swing.ActionMap;

public class MacOsUIWrapper
{
	/***
	 * Wrapper class for Mac specific UI stuff.
	 * @param actions
	 */
	public static void convertAccelerators(ActionMap actions)
	{
		try 
		{
			MacOsUIExtension.convertAccelerators(actions);
		} catch(Throwable t) 
		{
			// Do nothing
		}
	}

}
