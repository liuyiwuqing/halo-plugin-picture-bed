# 图床插件

## 交流群

[点击链接加入群聊【halo博客-lywq插件】](https://qm.qq.com/q/wuC7NZr0sw)
![halo插件交流群](https://github.com/user-attachments/assets/bf162401-07fd-49ec-b50f-5218c9510937)

## 介绍

对接多种图床服务，实现图片上传、删除等功能。

![图床插件演示1.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示1.png)
![图床插件演示2.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示2.png)
![图床插件演示3.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示3.png)
![图床插件演示5.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示5.png)
![图床插件演示4.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示4.png)

## 更新日志

### V1.3.0版本

    1. 新增全选功能
    2. 新增删除确认功能

### V1.2.4版本

refactor(picturebed): 重构图床插件代码并优化插件兼容性

    1. 更新 Halo 平台依赖版本至 2.20.11
    2. 缩减安装包大小
    3. 移除不必要的第三方库依赖
    4. 更新 UI 组件，移除调试代码
    5. 优化插件兼容性

### V1.2.3版本

    1. 优化插件配置加载逻辑，使用ReactiveSettingFetcher获取配置
    2. 更新插件和依赖版本，删除无用的注解和工具类，重构图片上传相关逻辑，优化异常处理，提升代码可读性和可维护性。

### V1.2.2版本

> ⚠️ 注意：此版本Halo 版本要求>=2.20.0

    1. 新增权限功能；#7
    2. 支持Halo2.20版本；

### V1.2.1版本

> ⚠️ 注意：此版本Halo 版本要求>=2.19.0
> ⚠️ ⚠️ ⚠️ 更新此版本之前务必先重置插件！！！

    1. 兰空图床支持配置存储策略；#3
    2. 单一类型图床支持多个图床接口配置，支持自定义图床名称；#5
    3. 使用插件的开发者工具生成 API Client，并重构插件的请求逻辑；
    4. 更新依赖库版本；

### V1.1.0版本

> 注意：此版本Halo 版本要求>=2.16.0

    1. 修复删除图片时，缓存导致的图片显示问题；
    2. 修复图库无法使用图床附件的问题；
    3. 更新依赖库版本；

### V1.0.2版本

> 注意：此版本Halo 版本要求>=2.16.0

    1. 修改Halo 版本要求

### V1.0.1版本

> 注意：此版本Halo 版本要求>=2.16.0

    1. 优化附件选项卡，实现图床选项动态加载
    2. 优化上传结果友好提示
    3. 更新依赖库

### V1.0.0版本

    1. 对接兰空图床
    2. 对接SM.MS图床
    3. 支持图片上传、批量上传
    4. 支持图片删除、批量删除

## 插件使用文档

请访问：<https://blog.muyin.site/docs>

## 精选插件

|                              名称                               |                                                     图片                                                     |                        功能                         |                       下载地址                        |
|:-------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| [【Halo程序】产品管理插件](https://auth.muyin.site/docs/PluginProduct)  | <img src="https://auth.muyin.site/upload/productLogo/PluginProduct.png" alt="img" style="width:100px;" />  | 该插件用于管理产品信息，包括产品列表、产品详情、产品版本、产品授权、产品订单，同时提供产品购买等。 | [访问](https://auth.muyin.site/docs/PluginProduct)  |
| [【Halo程序】授权管理插件](https://auth.muyin.site/docs/LywqPluginAuth) | <img src="https://auth.muyin.site/upload/productLogo/LywqPluginAuth.png" alt="img" style="width:100px;" /> |        对自定义插件提供统一管理，发布插件、授权插件、用户自助申请授权等功能。        | [访问](https://auth.muyin.site/docs/LywqPluginAuth) |
|  [【Halo程序】支付插件](https://auth.muyin.site/docs/PluginPayment)   | <img src="https://auth.muyin.site/upload/productLogo/PluginPayment.png" alt="img" style="width:100px;" />  |                      提供支付能力。                      | [访问](https://auth.muyin.site/docs/PluginPayment)  |
|    [Tools工具箱插件](https://auth.muyin.site/docs/PluginTools)     |  <img src="https://auth.muyin.site/upload/productLogo/PluginTools.png" alt="img" style="width:100px;" />   |        提供微信公众号对接，seo优化，文章定时发布，文章验证码等扩展功能。         |  [访问](https://auth.muyin.site/docs/PluginTools)   |
| [【Halo程序】友链自助提交插件](https://auth.muyin.site/docs/LinksSubmit)  |  <img src="https://auth.muyin.site/upload/productLogo/LinksSubmit.png" alt="img" style="width:100px;" />   |             访问者可以自助提交友链，并在后台管理页面进行审核。             |  [访问](https://auth.muyin.site/docs/LinksSubmit)   |
|    [【Halo程序】图床插件](https://blog.muyin.site/docs/pictureBed)    |      <img src="https://blog.muyin.site/upload/lywqPlugins/logo.png" alt="img" style="width:100px;" />      |              对接多种图床服务，实现图片上传、删除等功能。               |   [访问](https://blog.muyin.site/docs/pictureBed)   |

## 开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

所需环境：

1. Java 17
2. Node 18
3. pnpm 8
4. Docker (可选)

克隆项目：

```bash
git clone git@github.com:halo-sigs/plugin-picture-bed.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-picture-bed.git
```

```bash
cd path/to/plugin-picture-bed
```

### 运行方式 1（推荐）

> 此方式需要本地安装 Docker

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew haloServer

# Windows
./gradlew.bat haloServer
```

执行此命令后，会自动创建一个 Halo 的 Docker
容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

### 运行方式 2

> 此方式需要使用源码运行 Halo

编译插件：

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    fixedPluginPath:
      - "/path/to/plugin-picture-bed"
```

最后重启 Halo 项目即可。
