# 图床插件

## 介绍

对接多种图床服务，实现图片上传、删除等功能。

![图床插件演示1.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示1.png)
![图床插件演示2.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示2.png)
![图床插件演示3.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示3.png)
![图床插件演示5.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示5.png)
![图床插件演示4.png](https://blog.muyin.site/upload/lywqPlugins/图床插件演示4.png)

## 功能

### V1.0.0版本
    1. 对接兰空图床
    2. 对接SM.MS图床
    3. 支持图片上传、批量上传
    4. 支持图片删除、批量删除

## 插件文档

请访问：<https://blog.muyin.site/docs>

## 精选插件

|                          名称                           |                                                       图片                                                       |                 功能                  |                       下载地址                        |
|:-----------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------:|:-----------------------------------:|:-------------------------------------------------:|
| [授权管理插件](https://blog.muyin.site/docs/lywqPluginAuth) | <img src="https://blog.muyin.site/upload/lywqPlugins/lywqPluginAuthlogo.png" alt="img" style="width:100px;" /> | 对自定义插件提供统一管理，发布插件、授权插件、用户自助申请授权等功能。 | [访问](https://blog.muyin.site/docs/lywqPluginAuth) |
| [友链自助提交插件](https://blog.muyin.site/docs/linksSubmit)  |  <img src="https://blog.muyin.site/upload/lywqPlugins/linksSubmitlogo.png" alt="img" style="width:100px;" />   |      访问者可以自助提交友链，并在后台管理页面进行审核。      |  [访问](https://blog.muyin.site/docs/linksSubmit)   |
|   [Tools工具箱插件](https://blog.muyin.site/docs/tools)    |     <img src="https://blog.muyin.site/upload/lywqPlugins/tools-logo.png" alt="img" style="width:100px;" />     | 提供微信公众号对接，seo优化，文章定时发布，文章验证码等扩展功能。  |     [访问](https://blog.muyin.site/docs/tools)      |
|    [图床插件](https://blog.muyin.site/docs/pictureBed)    |        <img src="https://blog.muyin.site/upload/lywqPlugins/logo.png" alt="img" style="width:100px;" />        |       对接多种图床服务，实现图片上传、删除等功能。        |   [访问](https://blog.muyin.site/docs/pictureBed)   |

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

执行此命令后，会自动创建一个 Halo 的 Docker 容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

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
