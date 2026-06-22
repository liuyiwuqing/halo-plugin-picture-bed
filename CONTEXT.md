# Halo 图床插件上下文

本文是仓库的领域和架构上下文，供维护者和 Agent 在修改代码或文档前快速校准事实。

## 产品定位

图床插件把 Halo 控制台、附件选择器和第三方图床服务连接起来。它不托管图片文件，也不创建 Halo 本地附件，而是把第三方平台返回的图片信息转换成
Halo UI 可使用的附件式数据。

核心用户是 Halo 站点管理员和内容编辑者：

- 管理员在插件设置中配置一个或多个图床实例。
- 内容编辑者在图床管理页或 Halo 附件选择器中浏览、选择和使用图片。
- 有权限的用户可以上传和删除第三方图床中的图片。

## 运行事实

| 项目                  | 当前值                              |
|---------------------|----------------------------------|
| 插件 ID               | `PictureBed`                     |
| 插件版本                | `1.3.3`                          |
| Halo 要求             | `>=2.25.0`                       |
| 插件 API GroupVersion | `picturebed.muyin.site/v1alpha1` |
| UI 权限               | `plugin:picturebed:manage`       |
| 设置名称                | `picture-bed-settings`           |
| 配置组                 | `basic`                          |
| 配置 Map              | `picture-bed-config`             |

## 领域语言

**图床实例 Picture Bed**

插件设置中的一条第三方图床配置。一个站点可以有多个图床实例，每个实例有自己的类型、名称、认证信息和启用状态。

避免混用：provider、storage account、image host。

**图床类型 Picture Bed Type**

图床实例背后的服务类型。当前支持 `lsky`、`smms`、`imgtp`、`pan123`。

**图片列表 Image List**

当前图床实例在当前相册、文件夹、页码或批次下返回的图片集合。

避免混用：gallery、attachment list、file list。

**展示模式 Display Mode**

图片列表的视觉排列方式。当前有 `grid` 和 `masonry`。

**网格模式 Grid Mode**

图片卡片按稳定尺寸排列，适合快速浏览。

**瀑布流模式 Masonry Mode**

图片按自然比例紧凑排列，适合查看不同尺寸素材。实现决策见 `docs/adr/0001-shared-image-list-display-modes.md`。

**相册 Album**

图床平台提供的扁平分组。当前主要由兰空图床提供。

避免混用：category、tag、group。

**文件夹 Folder**

123 盘返回的层级分组。前端会把它显示成类似相册的过滤入口，但不要把 123 盘文件夹写成兰空相册。

## 架构地图

### 插件声明层

- `src/main/resources/plugin.yaml`：插件元数据、Halo 版本要求、设置名称、配置 Map、仓库和许可证。
- `src/main/resources/extensions/settings.yaml`：图床实例表单，字段需要与 `PictureBedConfig` 保持同步。
- `src/main/resources/extensions/roleTemplates.yaml`：图床管理权限模板和匿名隐藏权限模板。

### 后端接口层

- `PictureBedEndpoint` 是唯一的 CustomEndpoint。
- API 路径根为 `/apis/picturebed.muyin.site/v1alpha1`。
- 当前接口包括 `pictureBeds`、`albums`、`images`、`uploadImage`、`deleteImage`。
- `CommonQuery` 统一读取 `pictureBedId`、`type`、`albumId`、`imageId`、`keyword`、`page`、`size`。

### 后端服务层

- `PictureBedService` 是图床聚合服务，根据 `type` 分发到具体实现。
- `LskyProServiceImpl` 负责兰空图床。
- `SmmsServiceImpl` 负责 SM.MS。
- `ImgtpServiceImpl` 负责 ImgTP。
- `Pan123ServiceImpl` 负责 123 盘，并按 `client_id` 缓存访问令牌。
- `ImageVO`、`AlbumVO`、`PageResult`、`PictureBedVO` 是前端依赖的统一返回模型。

### 前端入口层

- `ui/src/index.ts` 注册 Console 路由和附件选择器扩展点。
- Console 路由挂在 `ToolsRoot` 下，路径为 `picture-bed`，菜单名为 `图床管理`。
- 附件选择器扩展点是 `attachment:selector:create`。
- 附件选择器只返回已启用且当前用户有权限的图床实例。

### 前端页面层

- `ui/src/views/PictureBeds.vue` 是图床管理页外壳，负责加载图床实例和切换 Provider。
- `LskySelectorProvider.vue`、`SmmsSelectorProvider.vue`、`ImgtpSelectorProvider.vue`、`Pan123SelectorProvider.vue`
  分别处理不同图床的浏览模型。
- `components/image/*` 是共享图片列表、展示模式、详情弹窗、上传弹窗和导航逻辑。

## 图床能力矩阵

| 类型    | 图床实例值    | 相册或文件夹 | 搜索        | 页码分页 | 批次加载         | 上传入口      | 删除 |
|-------|----------|--------|-----------|------|--------------|-----------|----|
| 兰空图床  | `lsky`   | 相册     | 支持        | 支持   | 无            | 支持        | 支持 |
| SM.MS | `smms`   | 无      | 当前 UI 无入口 | 支持   | 无            | 支持        | 支持 |
| ImgTP | `imgtp`  | 无      | 当前 UI 无入口 | 支持   | 无            | 支持        | 支持 |
| 123 盘 | `pan123` | 文件夹    | 不支持       | 不支持  | `lastFileId` | 当前 UI 未开放 | 支持 |

## 非目标

- 不实现本地图像存储。
- 不把第三方图片同步成 Halo Attachment 自定义资源。
- 不做统一 CDN、压缩、裁剪、水印或缩略图服务。
- 不把所有第三方图床强制抽象成同一套相册和分页模型。
- 不绕过第三方平台的权限、频率、文件大小或删除限制。

## 维护约定

- 新增或修改设置字段时，同时更新 `settings.yaml`、`PictureBedConfig`、`README.md` 和 `docs/user-guide.md`。
- 新增图床类型时，要补齐后端服务实现、`PictureBedService` 分发、Console Provider、附件选择器映射、用户文档和能力矩阵。
- 修改接口时，要同步 OpenAPI 生成客户端，避免前端手写接口路径。
- 修改图片列表展示逻辑时，优先改共享组件，不要在四个 Provider 中复制 UI。
- 文档描述必须以当前代码和插件元数据为准，不要沿用旧版本号、旧 Halo 要求或已经移除的开发命令。
