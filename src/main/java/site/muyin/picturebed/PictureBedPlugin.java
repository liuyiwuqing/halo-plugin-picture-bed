package site.muyin.picturebed;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * @author: lywq
 * @date: 2024/04/14 19:43
 * @version: v1.0.0
 * @description:
 **/
@Component
public class PictureBedPlugin extends BasePlugin {

    public PictureBedPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
    }
}
