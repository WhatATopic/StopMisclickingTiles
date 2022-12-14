/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.stopmisclickingtiles;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("stopmisclickingtiles")
public interface StopMisclickingTilesConfig extends Config
{
	@Alpha
	@ConfigItem(
			position = 0,
			keyName = "tileColor",
			name = "Disabled Tile Color",
			description = "Configures the color of disabled tiles"
	)
	default Color markerColor()
	{
		return new Color(79,0,0,170);
	}
	@ConfigItem(
			position = 1,
			keyName = "borderWidth",
			name = "Border Width",
			description = "Width of the disabled tile border"
	)
	default double borderWidth()
	{
		return 2;
	}
	@ConfigItem(
			keyName = "disableTilesBlockedByObject",
			name = "Disable Tiles Blocked By Object",
			description = "Disables the walk here action on tiles blocked by an object.",
			position = 2
	)
	default boolean disableTilesBlockedByObject()
	{
		return false;
	}
	@ConfigItem(
			keyName = "disableOtherBlockedTiles",
			name = "Disable Other Blocked Tiles",
			description = "Disables the walk here action on other blocked tiles.",
			position = 3
	)
	default boolean disableOtherBlockedTiles()
	{
		return false;
	}
}
