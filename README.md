# attr
Attribute parsing using kapt


### Usage


Declare collections of attributes as data classes
```kotlin
@Attr
data class CustomAttributes(
    @Attr.Id(R.attr.radius) @Dimension val radius: Float,
    @Attr.Id(R.attr.rippleColor) @ColorInt val rippleColor: Int,
    @Attr.Id(R.attr.backgroundColor) @ColorInt val backgroundColor: Int
)
```


and read them

```kotlin
val customAttributes = context.theme.attr<CustomAttributes>(attributeSet)
```
