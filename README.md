# attr [![CircleCI](https://circleci.com/gh/ouchadam/attr.svg?style=shield)](https://circleci.com/gh/ouchadam/attr) ![](https://img.shields.io/github/license/ouchadam/attr.svg) [ ![Download](https://api.bintray.com/packages/ouchadam/maven/attr/images/download.svg) ](https://bintray.com/ouchadam/maven/attr/_latestVersion)
Attribute parsing using kapt


```
compileOnly 'com.github.ouchadam:attr-processor:<latest-version>'
implementation 'com.github.ouchadam:attr:<latest-version>'
```

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
@Attr // mark data class for processing
data class ThemeAttributes(
    @Attr.Id(R.attr.colorPrimary) @ColorInt val colorPrimary: Int, // explicitly specify the attribute id
    @Attr.Id(-1) val missingAttribute: Drawable?, // allow attribute to be unavailable
    @ColorInt val colorPrimaryDark: Int // infer attribute id from parameter name
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

