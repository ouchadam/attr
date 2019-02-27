package com.github.ouchadam.attr

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass

class TypeFactories {

    private val typedArray = ClassName("android.content.res", "TypedArray")

    private val valueOrThrow = FunSpec.builder("assertValue")
        .addParameter("p0", typedArray)
        .addParameter("p1", Int::class)
        .addStatement(
            "if (!p0.hasValue(p1)) throw %T(%S)",
            IllegalAccessException::class.asTypeName(),
            "Attribute not defined in set."
        )
        .build()

    private fun booleanFactory(isNullable: Boolean) = create(isNullable, Boolean::class)
        .addStatement("return p0.getBoolean(p1, false)")
        .build()

    private fun intFactory(isNullable: Boolean) = create(isNullable, Int::class)
        .addStatement("return p0.getInteger(p1, 0)")
        .build()

    private fun colorIntFactory(isNullable: Boolean) = create(isNullable, Int::class, "colorInt")
        .addStatement("return p0.getColor(p1, 0)")
        .build()

    private fun dimenFloatFactory(isNullable: Boolean) = create(isNullable, Float::class, "dimen")
        .addStatement("return p0.getDimension(p1, 0f)")
        .build()

    private fun pxIntFactory(isNullable: Boolean) = create(isNullable, Int::class, "pxInt")
        .addStatement("return p0.getDimensionPixelSize(p1, 0)")
        .build()

    private fun floatFactory(isNullable: Boolean) = create(isNullable, Float::class)
        .addStatement("return p0.getFloat(p1, 0f)")
        .build()

    private fun resourceIntFactory(isNullable: Boolean) = create(isNullable, Int::class, "resourceInt")
        .addStatement("return p0.getResourceId(p1, 0)")
        .build()

    private fun create(isNullable: Boolean, klass: KClass<*>, name: String = klass.simpleName!!.decapitalize()) =
        FunSpec.builder("${name}${if (isNullable) "Nullable" else ""}Factory")
            .addModifiers(KModifier.PUBLIC)
            .returns(klass.asTypeName().copy(nullable = isNullable))
            .addParameter("p0", ClassName("android.content.res", "TypedArray"))
            .addParameter("p1", Int::class)
            .apply {
                if (isNullable) {
                    addStatement("if (!p0.hasValue(p1)) return null")
                } else {
                    addStatement("%N(p0, p1)", valueOrThrow)
                }
            }

    private val functionsMap: Map<AndroidType, FunSpec> = mapOf(
        AndroidType.COLOR to colorIntFactory(false),
        AndroidType.DIMEN to dimenFloatFactory(false),
        AndroidType.BOOLEAN to booleanFactory(false),
        AndroidType.INTEGER to intFactory(false),
        AndroidType.PX to pxIntFactory(false),
        AndroidType.RESOURCE_ID to resourceIntFactory(false),
        AndroidType.FLOAT to floatFactory(false)
    )

    private val nullableFunctionsMap: Map<AndroidType, FunSpec> = mapOf(
        AndroidType.COLOR to colorIntFactory(true),
        AndroidType.DIMEN to dimenFloatFactory(true),
        AndroidType.BOOLEAN to booleanFactory(true),
        AndroidType.INTEGER to intFactory(true),
        AndroidType.PX to pxIntFactory(true),
        AndroidType.RESOURCE_ID to resourceIntFactory(true),
        AndroidType.FLOAT to floatFactory(true)
    )

    fun forType(isNullable: Boolean, type: AndroidType): FunSpec {
        return when (isNullable) {
            true -> nullableFunctionsMap.getValue(type)
            false -> functionsMap.getValue(type)
        }
    }

    fun functions(): List<FunSpec> {
        return functionsMap.values + nullableFunctionsMap.values + valueOrThrow
    }
}