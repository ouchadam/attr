package com.github.ouchadam.attr

import com.squareup.kotlinpoet.ClassName

internal object Classes {

    val THEME = ClassName("android.content.res.Resources", "Theme")
    val ATTRIBUTE_SET = ClassName("android.util", "AttributeSet")
    val TYPED_ARRAY = ClassName("android.content.res", "TypedArray")
    val JAVA_INT = ClassName("java.lang", "Integer")
    val JAVA_BOOLEAN = ClassName("java.lang", "Boolean")
    val JAVA_FLOAT = ClassName("java.lang", "Float")

}