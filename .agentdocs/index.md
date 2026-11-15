# Nowbar Meter Documentation Index

This documentation originated in Pixel Meter. Translated into English by teddybaerd for the Nowbar Meter continuation.

The linked documents record the inherited design and requirements. Some details describe older upstream behavior; consult the repository [README](../README.md) and current source for the implemented behavior. Development guidelines are in [GEMINI.md](../GEMINI.md).

## Documentation structure

### 1. Product and requirements (PRD)

- [Core requirements](prd/requirements.md): Original project background, feature list, data flow and non-functional requirements.

### 2. Architecture and technical details

- [Data source strategy](architecture/data-source-strategy.md): Standard Android traffic APIs, physical-interface filtering and the history of the removed data-source approaches.
- [Service lifecycle](architecture/service-lifecycle.md): Foreground service lifecycle, background operation and Android 14+ considerations.

### 3. User interface

- [Design system](ui/design-system.md): Material You theme, notification icon rendering and floating-window interactions.

### 4. Workflows

No workflow documents have been added to this directory yet. The current build, signing and publication workflows are documented in the repository README.

## Related resources

- [Nowbar Meter repository](https://github.com/teddybaerd/NowbarMeter)
- [Upstream Pixel Meter repository](https://github.com/Pixel-Tailor-CN/PixelMeter)
- [GEMINI development guidelines](../GEMINI.md)
