# Halo 图床插件

图床插件用于把 Halo 控制台和附件选择器接入第三方图床服务。站点管理员可以配置多个图床实例，按实例浏览、上传、选择、预览和删除图片。

![插件 Logo](src/main/resources/logo.png)

## 当前状态

| 项目    | 说明                                                              |
|-------|-----------------------------------------------------------------|
| 插件版本  | `1.3.3`                                                         |
| 插件 ID | `PictureBed`                                                    |
| 运行要求  | Halo `>= 2.25.0`                                                |
| 后端基线  | Java 21、Halo Plugin Platform `2.24.0`                           |
| 前端基线  | Vue 3、Rsbuild、`@halo-dev/components`、`@halo-dev/ui-shared`      |
| 许可证   | GPL-3.0                                                         |
| 仓库    | <https://github.com/liuyiwuqing/halo-plugin-picture-bed>        |
| 问题反馈  | <https://github.com/liuyiwuqing/halo-plugin-picture-bed/issues> |

## 支持的图床

| 图床类型          | 配置值      | 浏览 | 上传       | 删除 | 分组能力 | 备注                        |
|---------------|----------|----|----------|----|------|---------------------------|
| 兰空图床 Lsky Pro | `lsky`   | 支持 | 支持       | 支持 | 相册   | 支持关键字搜索和可选储存策略 ID         |
| SM.MS         | `smms`   | 支持 | 支持       | 支持 | 无    | 使用上传历史列表                  |
| ImgTP         | `imgtp`  | 支持 | 支持       | 支持 | 无    | 使用 ImgTP 图片列表接口           |
| 123 盘图床       | `pan123` | 支持 | 当前管理页未开放 | 支持 | 文件夹  | 使用“查看更多”加载后续批次，不支持常规关键字搜索 |

## 核心能力

- 在 Halo 控制台 `工具 -> 图床管理` 中统一管理已启用的图床实例。
- 支持同一种图床配置多个实例，例如“文章图床”“封面图床”“临时图床”。
- 支持图片网格和瀑布流两种展示模式。
- 支持图片详情预览、链接查看、单选、多选、全选和批量删除。
- 支持在 Halo 附件选择器中注册已启用的图床入口，文章编辑器、主题设置和插件表单可以直接选择图床资源。
- 通过 `plugin:picturebed:manage` 控制 Console 菜单和附件选择器入口。

## 能力边界

- 插件不把第三方图床图片复制为 Halo 本地附件，只返回可访问的外部图片链接。
- 插件不提供统一 CDN、反向代理、图片处理、缩略图生成或缓存刷新能力。
- 删除操作会调用第三方图床删除接口，成功后通常不可恢复。
- 123 盘当前界面侧重浏览、选择和删除，后台服务里保留上传调用，但 Console 页面没有开放上传按钮。
- 不同图床的 API 能力不一致，搜索、相册、分页和文件夹展示不会强行抹平成完全一样。

## 安装和配置

1. 在 Halo 控制台进入 `插件`。
2. 安装并启用 `图床插件`。
3. 进入插件设置页，在 `基本设置 -> 图床接口` 中新增至少一个图床实例。
4. 填写图床类型、图床名称、API 地址、认证信息，并打开 `是否启用`。
5. 保存设置后刷新控制台，进入 `工具 -> 图床管理`。

详细配置和排障见 [用户使用指南](docs/user-guide.md)。

## 权限

插件声明了一个后台权限模板：

| 权限                         | 用途                                |
|----------------------------|-----------------------------------|
| `plugin:picturebed:manage` | 展示 `图床管理` 菜单、访问插件接口、在附件选择器中展示图床入口 |

如果非管理员看不到菜单或附件选择器入口，请先在 Halo 角色权限中分配“图床管理操作”权限。

## 接口概览

插件后端暴露的 API 分组为：

```text
/apis/picturebed.muyin.site/v1alpha1
```

当前接口：

| 接口             | 方法     | 说明                         |
|----------------|--------|----------------------------|
| `/pictureBeds` | `GET`  | 返回插件设置中配置的图床实例             |
| `/albums`      | `GET`  | 返回当前图床的相册或分组，只有部分图床有数据     |
| `/images`      | `GET`  | 返回当前图床的图片列表                |
| `/uploadImage` | `POST` | 上传图片，`multipart/form-data` |
| `/deleteImage` | `GET`  | 删除指定图片                     |

前端客户端由 Halo 插件开发工具根据 OpenAPI 生成，封装入口在 `ui/src/api/index.ts`。

## 项目结构

```text
.
├── src/main/java/site/muyin/picturebed
│   ├── PictureBedEndpoint.java      # 插件自定义 API
│   ├── PictureBedPlugin.java        # 插件生命周期入口
│   ├── config/                      # 插件设置映射
│   ├── query/                       # 查询参数模型
│   ├── service/                     # 图床服务接口与聚合服务
│   ├── service/Impl/                # 各图床服务实现
│   └── vo/                          # Console 和附件选择器使用的返回模型
├── src/main/resources
│   ├── plugin.yaml                  # 插件元数据
│   └── extensions/                  # 设置项和权限模板
├── ui/src
│   ├── index.ts                     # Console 路由和附件选择器扩展点
│   ├── views/PictureBeds.vue        # 图床管理页
│   ├── components/                  # 图床 Provider 和共享图片组件
│   └── api/                         # OpenAPI 生成客户端封装
├── docs/user-guide.md               # 用户使用指南
└── CONTEXT.md                       # 项目领域和架构上下文
```

## 本地开发

准备环境：

- JDK 21
- Node.js 20 或更高版本
- pnpm `10.12.4`
- Docker，可选，用于 `haloServer`

安装前端依赖：

```bash
./gradlew pnpmInstall
```

启动 Halo 插件开发环境：

```bash
./gradlew haloServer
```

构建插件包：

```bash
./gradlew build
```

运行前端单元测试：

```bash
./gradlew :ui:pnpmCheck
```

只检查或构建前端：

```bash
pnpm --dir ui type-check
pnpm --dir ui test:unit
pnpm --dir ui build
```

## 维护规则

- 修改插件设置时，同步检查 `settings.yaml`、`PictureBedConfig` 和文档配置说明。
- 新增图床类型时，同步补齐后端服务、Console Provider、附件选择器注册、权限说明和用户指南。
- 修改接口时，重新生成 OpenAPI 客户端并检查 `ui/src/api/generated`。
- 不要在文档中承诺第三方平台不支持的能力，尤其是相册、搜索、分页和删除语义。
