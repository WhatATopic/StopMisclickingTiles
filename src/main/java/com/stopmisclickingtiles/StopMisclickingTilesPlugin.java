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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "StopMisclickingTiles",
		description = "Shift + Right-click a tile to disable the \"walk here\" action on it."
)
public class StopMisclickingTilesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TileOverlay overlay;

	@Inject
	private StopMisclickingTilesConfig config;

	@Inject
	private Gson gson;

	@Getter(AccessLevel.PACKAGE)
	private final List<WorldPoint> disabledPoints = new ArrayList<>();

	private static final String CONFIG_GROUP = "smt";
	private static final String REGION_PREFIX = "region_";

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		loadTiles();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		disabledPoints.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// map region has just been updated
		loadTiles();
	}

	void saveTiles(int regionId, Collection<DisabledTile> tiles)
	{
		if (tiles == null || tiles.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
			return;
		}

		String json = gson.toJson(tiles);
		configManager.setConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId, json);
	}

	Collection<DisabledTile> getDisabledTiles(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		// CHECKSTYLE:OFF
		return gson.fromJson(json, new TypeToken<List<DisabledTile>>(){}.getType());
		// CHECKSTYLE:ON
	}

	void loadTiles()
	{
		disabledPoints.clear();

		int[] regions = client.getMapRegions();

		if (regions == null)
		{
			return;
		}

		for (int regionId : regions)
		{
			// load points for region
			log.debug("Loading points for region {}", regionId);
			Collection<DisabledTile> regionTiles = getDisabledTiles(regionId);
			Collection<WorldPoint> points = translateToWorldPoint(regionTiles);
			disabledPoints.addAll(points);
		}
	}

	private Collection<WorldPoint> translateToWorldPoint(Collection<DisabledTile> tiles)
	{
		if (tiles.isEmpty())
		{
			return Collections.emptyList();
		}

		return tiles.stream()
				.map(tile -> WorldPoint.fromRegion(tile.getRegionId(), tile.getRegionX(), tile.getRegionY(), tile.getZ()))
				.flatMap(wp ->
				{
					final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, wp);
					return localWorldPoints.stream();
				})
				.collect(Collectors.toList());
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption().equalsIgnoreCase("Walk here")
			&& event.getMenuTarget().isEmpty())
		{
			final Tile tile = client.getSelectedSceneTile();

			if (tile == null)
			{
				return;
			}
			
			final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, tile.getLocalLocation());
			final int regionId = worldPoint.getRegionID();
			final DisabledTile dTile = new DisabledTile(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
			if (getDisabledTiles(regionId).contains(dTile))
			{
				event.consume();
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (hotKeyPressed && event.getOption().equals("Walk here"))
		{
			final Tile selectedSceneTile = client.getSelectedSceneTile();

			if (selectedSceneTile == null)
			{
				return;
			}

			final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, selectedSceneTile.getLocalLocation());
			final int regionId = worldPoint.getRegionID();
			final DisabledTile dTile = new DisabledTile(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
			final boolean exists = getDisabledTiles(regionId).contains(dTile);

			client.createMenuEntry(-1)
					.setOption(exists ? "Enable Walk here" : "Disable Walk here")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						Tile target = client.getSelectedSceneTile();
						if (target != null)
						{
							toggleTile(target);
						}
					});
		}
	}

	private void toggleTile(Tile tile)
	{
		if (tile == null)
		{
			return;
		}

		final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, tile.getLocalLocation());
		final int regionId = worldPoint.getRegionID();
		final DisabledTile dTile = new DisabledTile(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane());
		List<DisabledTile> tiles = new ArrayList<>(getDisabledTiles(regionId));
		if (tiles.contains(dTile))
		{
			tiles.remove(dTile);
		}
		else
		{
			tiles.add(dTile);
		}

		saveTiles(regionId, tiles);

		loadTiles();
	}



	@Provides
	StopMisclickingTilesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StopMisclickingTilesConfig.class);
	}
}
