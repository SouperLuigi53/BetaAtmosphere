# Validation — 2026-09-22

Release: `beta-atmosphere-0.4.1+mc26.3.jar`

## Environment

Minecraft Java 26.3, Fabric Loader 0.19.5, Sodium 0.9.2+mc26.3 and a separate vanilla-renderer run. Windows, AMD Radeon RX 7900 XTX, OpenGL, Temurin JDK 25.0.4.1, Gradle 9.5.1 and Fabric Loom 1.17.21.

## Completed checks

- `gradlew.bat clean build --console=plain` passed, including 21 JUnit tests with zero failures or errors.
- Fresh comparisons against the official Beta 1.7.3 client JAR (SHA-1 `43db9b498cb67058d2e12d394e6507722e71bb45`) passed: 3,072,000 exact RGBA fluid-pixel comparisons and 192,000 exact-float celestial-angle/sunset comparisons.
- The full in-game smoke suite passed with both vanilla and Sodium after the sky-temperature correction: 14 scenes, 13 GPU lightmap readbacks and six GPU fluid-atlas readbacks per renderer. These included day, night, sunset, fast/fancy clouds, water, lava, Nether, End, resource reload and fog shader compilation.
- Focused in-game drop checks passed with both renderers after the Beta stack-copy correction. They exercised 576 calls to the transformed item renderer across both runs, including camera yaw, pitch, age and resource reload. The checks verified flat geometry and camera-facing normals for six item types, tint, glint, shared-model isolation and restored 3D rendering for block and special models.
- Stack sizes 1, 2, 5, 6, 20, 21 and 64 produced Beta's expected 1, 2, 2, 3, 3, 4 and 4 copies. Dappled Forest returned its registered grass `#DF6827` and foliage `#E68E30` colors; ordinary Forest and Badlands retained their previous Beta-palette behavior.
- Four focused screenshots per renderer were captured: Dappled Forest with drops, side view, steep overhead view and resource reload. The disposable creative world under `run\saves` was the only world modified.

The full and focused in-game checks run with:

```powershell
.\gradlew.bat runClient -Psodium=true -Psmoke=true --console=plain
.\gradlew.bat runClient -Psodium=true -Psmoke=true -PdropSmoke=true --console=plain
```

Omit `-Psodium=true` to repeat with vanilla rendering. The smoke harness is excluded from normal release builds. The smoke runs preceded a version-metadata-only bump to 0.4.1; the rendering code in the release is the code they tested.

## Scope and limits

The direct fluid comparisons check CPU simulation pixels against unchanged Beta bytecode. The in-game GPU checks separately verify the active fluid atlas palette, alpha, mip levels and animation; they are not exact GPU pixel comparisons against a Beta renderer.

Sky and vegetation colors use the modern world's fixed biome climate rather than Beta's coordinate-varying climate noise. The sky input is now passed unclamped into Beta's divide-by-three-and-clamp formula. Modern block materials, fluid meshes and OpenGL rasterization can differ from Beta. Fluid animations begin with fresh random state and a warm-up period rather than Beta's exact first frame.

Dropped sprite quads face both camera yaw and pitch, extending Beta's yaw-only behavior as requested. Their copy-count thresholds now match Beta, while modern depth spacing remains. Minecraft's item render state does not mark generated sprites; a custom front-lit 3D model with a south-facing quad may also be flattened. Block-lit and special-rendered models are excluded. Dappled Forest's registered orange colors are an intentional modern-biome exception. The current End keeps its own visuals because Beta 1.7.3's dimension 1 was the older Sky dimension.

Shader packs, Vulkan, live multiplayer, custom resource packs and other Sodium versions were not tested.
