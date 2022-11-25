package com.stopmisclickingtiles;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StopMisclickingTilesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(StopMisclickingTilesPlugin.class);
		RuneLite.main(args);
	}
}