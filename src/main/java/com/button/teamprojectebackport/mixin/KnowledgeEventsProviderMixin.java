package com.button.teamprojectebackport.mixin;

import com.button.teamprojectebackport.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mixin(KnowledgeImpl.Provider.class)
public class KnowledgeEventsProviderMixin {

    private static Field PLAYER_FIELD;

    static {
        try{
            PLAYER_FIELD = Class.forName("moze_intel.projecte.impl.capability.KnowledgeImpl$DefaultImpl").getDeclaredField("player");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @ModifyArg(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lmoze_intel/projecte/capability/managing/SerializableCapabilityResolver;<init>(Lnet/minecraftforge/common/util/INBTSerializable;)V"),
            index = 0,
            remap = false)
    private static INBTSerializable<CompoundNBT> onInit(INBTSerializable<CompoundNBT> internal) throws IllegalAccessException {
        if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && Arrays.asList(internal.getClass().getInterfaces()).contains(IKnowledgeProvider.class))
            return new TeamKnowledgeProvider((ServerPlayerEntity) PLAYER_FIELD.get(internal));
        return internal;
    }
}