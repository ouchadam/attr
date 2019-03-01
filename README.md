# attr [![CircleCI](https://circleci.com/gh/ouchadam/attr.svg?style=shield)](https://circleci.com/gh/ouchadam/attr) ![](https://img.shields.io/github/license/ouchadam/attr.svg)
Attribute parsing using kapt

### Supports

- `@Dimension Float`
- `@ColorInt Int`
- `@Px Int`
- `@IdRes Int`
- `Float`
- `Boolean`
- `Int`
- `Drawable`

### Usage

Declare collections of attributes as data classes
```kotlin
@Attr
data class ThemeAttributes(
    @Attr.Id(R.attr.radius) @Dimension val radius: Float, // explicitly specify the attribute id
    @ColorInt val colorPrimary: Int, // infer attribute id from parameter name
    val missingAttribute : Drawable? // allow attribute to be unavailable
)
```

The library adds an extension function to [Resources.Theme](https://developer.android.com/reference/android/content/res/Resources.Theme)

```kotlin
val themeAttributes = context.theme.attr<ThemeAttributes>()
```

An [AttributeSet](https://developer.android.com/reference/android/util/AttributeSet) can be provide for usage with a custom view
```kotlin
val customViewAttributes = context.theme.attr<CustomViewAttributes>(attributeSet)
```

