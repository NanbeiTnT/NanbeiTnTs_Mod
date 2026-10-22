package com.nanbeitnt.mod.client.mixin;

import com.nanbeitnt.mod.client.pack.PackSync;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 天空覆盖包生效时去掉原版云。
 * 云层画在天空外侧，会和覆盖包的天空盒叠在一起；这里只是不提交云的渲染 pass，
 * 不动玩家的"云"设置（options.txt 保持原样）。
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
	private void nanbeitnts$hideVanillaClouds(CallbackInfo ci) {
		if (PackSync.isSkyPackLoaded()) {
			ci.cancel();
		}
	}
}
