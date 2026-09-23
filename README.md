# Beta Atmosphere

A client-only Fabric mod for **Minecraft Java 26.3**, porting **Beta 1.7.3's visual lighting and atmosphere**. Sodium is optional; the supported Sodium target is **0.9.2+mc26.3**.

## Install

1. Use a Minecraft **26.3** profile with **Fabric Loader 0.19.5 or later**.
2. Remove the older Beta Atmosphere JAR, then put `beta-atmosphere-0.4.1+mc26.3.jar` in that profile's `mods` folder.
3. Keep your existing Sodium 26.3 JAR in the same folder, if you use Sodium.
4. Launch the profile. All effects are enabled automatically. The existing Fast/Fancy/Off cloud setting is respected.

Beta Atmosphere itself does not require Fabric API, Mod Menu, a resource pack, or a server mod. It works in singleplayer and as a purely visual client mod in multiplayer. To uninstall, remove its JAR.

## Behavior

- **Block lighting:** Beta's corner sampling and diagonal-occlusion rule, with each neighbor converted through the old brightness curve before averaging. This replaces modern smooth-lighting/AO on block models in both vanilla and Sodium. Includes Beta's directional face shading and flat-lighting path when smooth lighting is disabled.
- **Light appearance:** Beta's grayscale brightness curve, neutral torchlight without modern flicker, maximum-of-block-and-sky light mixing, and Beta day/night/weather skylight dimming. Ambient floors are 5% in the Overworld and 10% in the Nether. Fluid faces use Beta's flat brightness sampling and face shading.
- **Distance fog:** spherical, linear fog starting at 25% of the view distance in the Overworld, or at the camera in the Nether. The horizon is limited to Beta's 256-block Far setting; lower render distances are respected. Includes the old atmospheric fog palette, sky/fog blend, and local-light fog darkening at shorter view distances.
- **Sky and sunsets:** Beta's temperature-based sky color, celestial clock, weather dimming, sunset gradient, star field and star sizes. The original sun and full-moon images are included. Moon phases are disabled in the Overworld, and the celestial orientation follows Beta. The lower sky uses Beta's colored horizon instead of the later black void disc.
- **Clouds:** the original cloud image, at Y=108.33, with Beta's day/night/weather colors and 80% opacity. Fancy clouds have 12-block cells and four-block thickness; fast clouds use an eight-block scale. Both drift at 0.03 blocks per tick.
- **Grass and leaves:** the original grass and foliage color maps and climate lookup. Modern swamp/dark-forest/mesa color overrides are bypassed. **Dappled Forest is an intentional exception:** its registered grass and foliage overrides are preserved, restoring its orange palette. The exception follows the biome ID, including server/data-pack color changes. Spruce and birch retain their original fixed colors, which already match Beta.
- **Dropped item sprites:** ordinary item sprites become genuine flat quads and face the camera, including when viewed from above or below. Their textures, tint layers, enchantment glint and bobbing remain active. Stack-copy counts follow Beta: one copy for a single item, two for 2–5, three for 6–20 and four for 21 or more. 3D block drops and special models (such as shields) keep their normal rendering. Held items, inventory icons and item frames are unaffected. Full pitch-facing is a convenience beyond Beta's yaw-only billboard behavior.
- **Water and lava:** live 16×16 procedural simulations using Beta's update rules and palettes, including the tiled flowing sprites and original transparency. Water has no modern biome tint or special side overlay. The block atlas and its mip levels are updated directly, so both renderers use the same animation.
- **Inside fluids:** Beta's dark-blue underwater color, orange lava color, and exponential fog densities (0.1 for water, 2.0 for lava) in the Overworld and Nether. This also applies in spectator mode. The mod patches the standard vanilla and Sodium fog includes to support exponential fluid fog.

The Beta brightness curve is fixed: the modern Brightness slider does not brighten it. Modern lightmap effects such as Night Vision, Darkness, and boss darkening continue to work. The fixed Beta fluid fog replaces modern water-vision and fire-resistance visibility adjustments; potion gameplay effects remain unchanged. World generation, sounds, block-light propagation, spawning rules, blocks, items, and server state are unchanged.

**No bedrock fog or drifting void dust is added. Those effects belong to Beta 1.8, not Beta 1.7.3.**
Modern height-based void darkening is also disabled in the Overworld and Nether; status-effect fog remains active.

## Accuracy and scope

This ports Beta's visual rendering rules into the modern renderers; it is not a replacement for Minecraft's world light engine. Classic cube faces use the Beta corner algorithm. Partial and rotated modern models interpolate that corner field over their existing geometry. Modern block solidity and the current world's light levels supply the inputs, so new block types and light-transmission differences are adaptations rather than an exact recreation of every Beta rendering quirk.

The five legacy PNGs are unchanged from the official Beta 1.7.3 client; their hashes are recorded in `LEGACY-ASSETS.sha256`. Procedural fluid pixels are generated using the original numerical rules, with fresh random state on load. They do not repeat a recorded loop.

The modern world's biome temperature and rainfall drive the old color maps. This does not recreate Beta's climate-noise map or world generator. Modern biome blending, terrain, entity models, post-Beta blocks, and unrelated block textures remain modern. Fancy clouds use the modern/Sodium mesh builder with Beta's texture, scale, height, thickness, colors and motion; old OpenGL rasterization quirks are not reproduced. Resource packs can override the sky/cloud images, but procedural fluid pixels and the legacy vegetation maps are controlled by the mod.

Minecraft's item render state does not identify which 3D meshes came from a generated sprite. A custom 3D item model that uses ordinary item lighting and has a front-facing quad may also be flattened when dropped. Block-lit models and special renderers are excluded.

Beta sunlight is part of the compiled block lighting. When its integer brightness changes, loaded nonempty sections are queued for rebuilding, nearest first, up to 64 sections per tick. This adds chunk-building work around dawn, dusk, weather changes, and time jumps. Distant chunks can catch up over several seconds; no performance guarantee is made for large render distances.

The lightmap has a linear brightness row for the new vertices, preserving Beta's smooth gradients and modern Night Vision/Darkness effects. Ordinary integer light samples retain the old mod's brightness values. Brightness is quantized to the modern vertex format and GPU texture precision.

Powder-snow and status-effect fog retain their modern paths. The End and custom dimensions keep their own lighting and fog. Legacy fluid textures and vegetation color maps are shared assets and also apply in those dimensions. Custom dimensions using the standard star buffer also receive the Beta star geometry.

Shader packs that replace lighting or fog are outside the supported scope. Other mods that replace the same rendering methods may conflict. Ordinary resource packs can still change unrelated textures.

## Build

Requires a **JDK 25** on `PATH`. Gradle downloads the pinned toolchain dependencies automatically. From the extracted source folder in PowerShell:

```powershell
.\gradlew.bat clean build --console=plain
```

The installable JAR is generated under `build\libs`. The `-sources.jar` is for developers and should not be placed in `mods`.

Launch a separate development game, with Sodium:

```powershell
.\gradlew.bat runClient -Psodium=true --console=plain
```

The development game uses this project's `run` directory, not your regular Minecraft profile.

## Validation

`src/test` checks Beta reference lighting, corner interpolation, fog distances, celestial timing, sunset colors, cloud/night/weather colors, climate lookup, fluid palette bounds and flowing-lava timing.

`src/smoke` is an opt-in development harness. It opens a **disposable creative world named `New World` in `run\saves`**, changes that world to stage fourteen rendering scenarios, calls the transformed vanilla/Sodium lighting classes against known vertex fixtures, captures screenshots, checks all 65,536 GPU lightmap pixels in each supported scene, checks the four fluid sprites and mipmaps on the GPU, verifies active legacy resource bytes and fog shader hooks, checks sunlight mesh invalidation and Sodium's stored fog parameters, and exits. Create that disposable world with commands enabled before running:

```powershell
.\gradlew.bat runClient -Psodium=true -Psmoke=true --console=plain
```

Omit `-Psodium=true` to repeat with the vanilla renderer. Reports and screenshots appear in `run`. Always run `clean build` without `-Psmoke=true` to produce the release. The delivered JAR excludes the harness.

The focused dropped-item checks exercise actual renderer submissions across camera yaw/pitch and item age, Beta stack-copy counts, 3D blocks and shared inventory/held models, Dappled Forest color overrides, and four scenes including resource reload:

```powershell
.\gradlew.bat runClient -Psodium=true -Psmoke=true -PdropSmoke=true --console=plain
```

`tools/reference/README.md` describes optional direct comparisons with the original Beta client bytecode. These verified 3,072,000 fluid pixels and 192,000 celestial-angle/sunset samples for this release.

## Implementation references

The rendering behavior was checked against Mojang's Beta 1.7.3 client and 26.3 client, obtained through the [official version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json). The five included legacy PNG assets are credited in `THIRD-PARTY-NOTICES.md` and are excluded from the code's MIT license.

Build setup follows the [Fabric example project](https://github.com/FabricMC/fabric-example-mod/tree/26.3). The optional Sodium adapter targets the internal lighting pipelines in [Sodium 0.9.2 for 26.3](https://modrinth.com/mod/sodium/version/mc26.3-0.9.2-fabric). Other Sodium versions need separate validation. No Minecraft or Sodium class files are bundled.

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
