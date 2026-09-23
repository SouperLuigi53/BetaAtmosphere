# Beta Atmosphere

Recreate the feel and look of Minecraft Beta 1.7.3 - best paired with Programmer Art.

Beta Atmosphere is a **client-side Fabric mod for Minecraft Java 26.3**. It changes how the game looks without changing world generation, gameplay, or the server.

## Features

- Beta-style block shading, light colors, and day/night lighting.
- Classic distance fog, sky, sunsets, stars, sun, moon, and clouds.
- Original Beta grass and leaf color maps. Dappled Forest keeps its orange colors.
- Beta-style animated water and lava, including underwater and lava fog.
- Flat dropped-item sprites that always face the camera. Block drops and special item models stay 3D.

The mod stays with Beta 1.7.3 visuals, so it does not add the bedrock fog or dust introduced later.

## Build and install

Use **Minecraft 26.3**, **Fabric Loader 0.19.5 or newer**, and **JDK 25** to build from source. In PowerShell, run:

```powershell
.\gradlew.bat clean build --console=plain
```

Copy `build\libs\beta-atmosphere-0.4.1+mc26.3.jar` into the `mods` folder for your Fabric profile. Do not install the `-sources.jar` file.

Sodium is optional; the mod was tested with **Sodium 0.9.2+mc26.3**. Fabric API and a server-side installation are not required. The Programmer Art resource pack is recommended but optional.

## Notes

This recreates Beta's **visual lighting**, not the old world's light-spreading engine. Modern blocks, biome climate, and models can still look different. The Brightness slider does not change the fixed Beta lighting curve, and shader packs that replace lighting or fog may conflict.

The code is MIT-licensed. The included original Minecraft textures are credited separately in [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md). For test coverage and the known rendering limits, see [VALIDATION.md](VALIDATION.md). The direct Beta reference checks are described in [tools/reference/README.md](tools/reference/README.md).
