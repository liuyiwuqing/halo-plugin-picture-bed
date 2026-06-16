# Shared Image List display modes

The console Image List supports both Grid Mode and Masonry Mode through a shared frontend display component instead of
duplicating layout logic in each Picture Bed provider. Masonry Mode is implemented with CSS columns rather than fixed
height grid tiles or a third-party masonry library, so images keep their natural proportions and stack tightly without
large wrapper whitespace.

**Considered Options**

- Keep layout markup inside each provider and add mode switching four times.
- Extract the Image List card layout into a shared component and let providers keep only provider-specific data loading
  and actions.
- Use CSS columns and preserve each image's natural vertical height.
- Use CSS Grid with `grid-auto-flow: dense` and tile spans derived from image dimensions plus a stable visual rhythm.
- Introduce a dedicated masonry library.

**Consequences**

All Picture Bed providers should pass their images, selection state, disabled state, and detail handlers into the shared
Image List display component. Future Display Modes should be added there first unless a provider has a genuinely
provider-specific browsing model. Masonry Mode uses `object-contain` with natural image height so thumbnails preserve
complete image content, avoid crop-heavy previews, and do not sit inside large padded cards. The shared component owns
the column rhythm so providers do not need to tune layout independently.
