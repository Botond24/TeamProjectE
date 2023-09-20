package net.minecraft.client.renderer.model;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IUnbakedModel extends net.minecraftforge.client.extensions.IForgeUnbakedModel {
   Collection<ResourceLocation> getDependencies();

   Collection<RenderMaterial> getMaterials(Function<ResourceLocation, IUnbakedModel> pModelGetter, Set<Pair<String, String>> pMissingTextureErrors);

   @Nullable
   IBakedModel bake(ModelBakery pModelBakery, Function<RenderMaterial, TextureAtlasSprite> pSpriteGetter, IModelTransform pTransform, ResourceLocation pLocation);
}
