/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.world;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;

public class MobSpawnInfoBuilder extends MobSpawnInfo.Builder
{
    private final Set<EntityClassification> typesView = Collections.unmodifiableSet(this.spawners.keySet());
    private final Set<EntityType<?>> costView = Collections.unmodifiableSet(this.spawnCosts.keySet());

    public MobSpawnInfoBuilder(MobSpawnInfo orig)
    {
        orig.getSpawnerTypes().forEach(k -> {
            spawners.get(k).clear();
            spawners.get(k).addAll(new java.util.ArrayList<>(orig.getSpawners(k)));
        });
        orig.getEntityTypes().forEach(k -> spawnCosts.put(k, orig.getSpawnCost(k)));
        creatureSpawnProbability = orig.getCreatureSpawnProbability();
        validSpawnBiomeForPlayer = orig.isValidSpawnBiomeForPlayer();
    }

    public Set<EntityClassification> getSpawnerTypes()
    {
        return this.typesView;
    }

    public List<MobSpawnInfo.Spawners> getSpawner(EntityClassification type)
    {
        return this.spawners.get(type);
    }

    public Set<EntityType<?>> getEntityTypes()
    {
        return this.costView;
    }

    @Nullable
    public MobSpawnInfo.SpawnCosts getCost(EntityType<?> type)
    {
        return this.spawnCosts.get(type);
    }

    public float getProbability()
    {
        return this.creatureSpawnProbability;
    }

    public MobSpawnInfoBuilder disablePlayerSpawn()
    {
        this.validSpawnBiomeForPlayer = false;
        return this;
    }
}