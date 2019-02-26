package com.github.ouchadam.attr

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class TypeFactories {

    private val booleanFactory = create("boolean")
        .returns(Boolean::class)
        .addStatement("return p0.getBoolean(p1, false)")
        .build()

    private val intFactory = create("int")
        .returns(Int::class)
        .addStatement("return p0.getInteger(p1, 0)")
        .build()

    private val colorIntFactory = create("color")
        .returns(Int::class)
        .addStatement("return p0.getColor(p1, 0)")
        .build()

    private val dimenFloatFactory = create("dimen")
        .returns(Float::class)
        .addStatement("return p0.getDimension(p1, 0f)")
        .build()

    private val pxIntFactory = create("px")
        .returns(Int::class)
        .addStatement("return p0.getDimensionPixelSize(p1, 0)")
        .build()

    private fun create(name: String) = FunSpec.builder("${name}Factory")
        .addModifiers(KModifier.PUBLIC)
        .addParameter("p0", ClassName("android.content.res", "TypedArray"))
        .addParameter("p1", Int::class)

    fun forType(type: AndroidType): FunSpec {
        return when (type) {
            AndroidType.COLOR -> colorIntFactory
            AndroidType.DIMEN -> dimenFloatFactory
            AndroidType.BOOLEAN -> booleanFactory
            AndroidType.INTEGER -> intFactory
            AndroidType.PX -> pxIntFactory
        }
    }

    fun functions(): List<FunSpec> {
        return listOf(booleanFactory, intFactory, colorIntFactory, dimenFloatFactory, pxIntFactory)
    }
}