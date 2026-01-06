# JadeEvents.onClientRegistration

## Basic info

- Valid script types: [CLIENT]

- Has result? ✘

- Event class: WailaClientRegistrationEventJS (third-party)

### Available fields:

| Name | Type | Static? |
| ---- | ---- | ------- |

Note: Even if no fields are listed above, some methods are still available as fields through *beans*.

### Available methods:

| Name | Parameters | Return type | Static? |
| ---- | ---------- | ----------- | ------- |
| block | ResourceLocation, Class<? extends Block> |  | BlockComponentProviderBuilder | ✘ |
| progress | ResourceLocation |  | ClientExtensionProviderBuilder<CompoundTag, ProgressView> | ✘ |
| itemStorage | ResourceLocation |  | ClientExtensionProviderBuilder<ItemStack, ItemView> | ✘ |
| fluidStorage | ResourceLocation |  | ClientExtensionProviderBuilder<CompoundTag, FluidView> | ✘ |
| addItemModNameCallback | int, JadeItemModNameCallback |  | void | ✘ |
| addItemModNameCallback | JadeItemModNameCallback |  | void | ✘ |
| createPluginConfigScreen | Screen, String |  | Screen | ✘ |
| addRayTraceCallback | int, JadeRayTraceCallback |  | void | ✘ |
| addRayTraceCallback | JadeRayTraceCallback |  | void | ✘ |
| addRenderBackgroundCallback | int, JadeRenderBackgroundCallback |  | void | ✘ |
| addRenderBackgroundCallback | JadeRenderBackgroundCallback |  | void | ✘ |
| registerEntityComponent | IEntityComponentProvider, Class<? extends Entity> |  | void | ✘ |
| markAsClientFeature | ResourceLocation |  | void | ✘ |
| registerCustomEnchantPower | Block, CustomEnchantPower |  | void | ✘ |
| addAfterRenderCallback | int, JadeAfterRenderCallback |  | void | ✘ |
| addAfterRenderCallback | JadeAfterRenderCallback |  | void | ✘ |
| addTooltipCollectedCallback | JadeTooltipCollectedCallback |  | void | ✘ |
| addTooltipCollectedCallback | int, JadeTooltipCollectedCallback |  | void | ✘ |
| markAsServerFeature | ResourceLocation |  | void | ✘ |
| addBeforeRenderCallback | JadeBeforeRenderCallback |  | void | ✘ |
| addBeforeRenderCallback | int, JadeBeforeRenderCallback |  | void | ✘ |
| registerItemStorageClient | IClientExtensionProvider<ItemStack, ItemView> |  | void | ✘ |
| registerBlockComponent | IBlockComponentProvider, Class<? extends Block> |  | void | ✘ |
| registerFluidStorageClient | IClientExtensionProvider<CompoundTag, FluidView> |  | void | ✘ |
| registerAccessorHandler | Class<T extends Accessor<?>>, Accessor.ClientHandler<T extends Accessor<?>> |  | void | ✘ |
| isShowDetailsPressed |  |  | boolean | ✘ |
| registerEnergyStorageClient | IClientExtensionProvider<CompoundTag, EnergyView> |  | void | ✘ |
| registerProgressClient | IClientExtensionProvider<CompoundTag, ProgressView> |  | void | ✘ |
| addConfigListener | ResourceLocation, Consumer<ResourceLocation> |  | void | ✘ |
| shouldPick | BlockState |  | boolean | ✘ |
| shouldPick | Entity |  | boolean | ✘ |
| shouldHide | Entity |  | boolean | ✘ |
| shouldHide | BlockState |  | boolean | ✘ |
| registerEntityIcon | IEntityComponentProvider, Class<? extends Entity> |  | void | ✘ |
| usePickedResult | EntityType<?> |  | void | ✘ |
| usePickedResult | Block |  | void | ✘ |
| entityAccessor |  |  | Builder | ✘ |
| registerBlockIcon | IBlockComponentProvider, Class<? extends Block> |  | void | ✘ |
| hideTarget | EntityType<?> |  | void | ✘ |
| hideTarget | Block |  | void | ✘ |
| blockAccessor |  |  | Builder | ✘ |
| maybeLowVisionUser |  |  | boolean | ✘ |
| getAccessorHandler | Class<? extends Accessor<?>> |  | Accessor.ClientHandler<Accessor<?>> | ✘ |
| setServerData | CompoundTag |  | void | ✘ |
| getServerData |  |  | CompoundTag | ✘ |
| getBlockCamouflage | LevelAccessor, BlockPos |  | ItemStack | ✘ |
| isServerConnected |  |  | boolean | ✘ |
| isClientFeature | ResourceLocation |  | boolean | ✘ |
| addConfig | ResourceLocation, String, Predicate<String> |  | void | ✘ |
| addConfig | ResourceLocation, Enum<?> |  | void | ✘ |
| addConfig | ResourceLocation, int, int, int, boolean |  | void | ✘ |
| addConfig | ResourceLocation, float, float, float, boolean |  | void | ✘ |
| addConfig | ResourceLocation, boolean |  | void | ✘ |
| entity | ResourceLocation, Class<? extends Entity> |  | EntityComponentProviderBuilder | ✘ |
| energyStorage | ResourceLocation |  | ClientExtensionProviderBuilder<CompoundTag, EnergyView> | ✘ |
| exit | Object |  | Object | ✘ |
| exit |  |  | Object | ✘ |
| success | Object |  | Object | ✘ |
| success |  |  | Object | ✘ |
| cancel | Object |  | Object | ✘ |
| cancel |  |  | Object | ✘ |


### Documented members:

- `Object exit(Object var0)`

  Parameters:
  - var0: Object

```
Stops the event with the given exit value. Execution will be stopped **immediately**.

`exit` denotes a `default` outcome.
```

- `Object exit()`
```
Stops the event with default exit value. Execution will be stopped **immediately**.

`exit` denotes a `default` outcome.
```

- `Object success(Object var0)`

  Parameters:
  - var0: Object

```
Stops the event with the given exit value. Execution will be stopped **immediately**.

`success` denotes a `true` outcome.
```

- `Object success()`
```
Stops the event with default exit value. Execution will be stopped **immediately**.

`success` denotes a `true` outcome.
```

- `Object cancel(Object var0)`

  Parameters:
  - var0: Object

```
Cancels the event with the given exit value. Execution will be stopped **immediately**.

`cancel` denotes a `false` outcome.
```

- `Object cancel()`
```
Cancels the event with default exit value. Execution will be stopped **immediately**.

`cancel` denotes a `false` outcome.
```



### Example script:

```js
JadeEvents.onClientRegistration((event) => {
	// This space (un)intentionally left blank
});
```

