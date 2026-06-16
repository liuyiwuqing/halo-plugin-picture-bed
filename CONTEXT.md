# Halo Picture Bed

Halo Picture Bed is a plugin context for managing third-party image hosting resources inside the Halo console and
attachment selector.

## Language

**Picture Bed**:
A configured third-party image hosting entry exposed by the plugin. A site can have multiple Picture Beds, each with its
own type, name, credentials, and availability.
_Avoid_: provider, storage account, image host

**Image List**:
The browsable collection of image resources returned by the selected Picture Bed for the current album, folder, search,
page, or loaded batch.
_Avoid_: gallery, attachment list, file list

**Display Mode**:
The user's chosen visual arrangement for the Image List.
_Avoid_: view type, layout type

**Grid Mode**:
A Display Mode where image cards use consistent dimensions in a regular row-and-column arrangement.
_Avoid_: default view, thumbnail grid

**Masonry Mode**:
A Display Mode where image cards form a dense mosaic arrangement with mixed wide, tall, large, and normal tiles while
each preview preserves the complete image content.
_Avoid_: waterfall, Pinterest view

**Album**:
A Picture Bed grouping used to filter the Image List when the backing service supports album-like organization.
_Avoid_: category, tag, group

**Folder**:
A hierarchical grouping returned by 123 Pan and shown as an Album-like filter in the console.
_Avoid_: album, directory
