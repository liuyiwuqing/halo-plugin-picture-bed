package site.muyin.picturebed.reconcile;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.utils.PluginCacheManager;


/**
 * @author: lywq
 * @date: 2024/02/27 17:35
 * @version: v1.0.0
 * @description:
 **/
@Component
@RequiredArgsConstructor
public class SettingToolsReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;
    private final PluginCacheManager pluginCacheManager;

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        if (CharSequenceUtil.equals(name, PictureBedConfig.CONFIG_MAP_NAME)) {
            client.fetch(ConfigMap.class, name)
                .ifPresent(configMap -> {
                    pluginCacheManager.put(PictureBedConfig.CONFIG_MAP_NAME,
                        JSONUtil.toJsonStr(configMap.getData()));
                });
        }
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new ConfigMap())
            .build();
    }
}
