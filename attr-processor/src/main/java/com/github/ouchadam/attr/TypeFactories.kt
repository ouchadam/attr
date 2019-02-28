package com.github.ouchadam.attr

import com.github.ouchadam.attr.Classes.DRAWABLE
import com.github.ouchadam.attr.Classes.TYPED_ARRAY
import com.squareup.kotlinpoet.*

class TypeFactories {

    private val valueOrThrow = FunSpec.builder("assertValue")
        .addParameter("p0", TYPED_ARRAY)
        .addParameter("p1", Int::class)
        .addStatement(
            "if (!p0.hasValue(p1)) throw %T(%S)",
            IllegalAccessException::class.asTypeName(),
            "Attribute not defined in set."
        )
        .build()

    private fun booleanFactory(isNullable: Boolean) = create(isNullable, BOOLEAN)
        .addStatement("return p0.getBoolean(p1, false)")
        .build()

    private fun intFactory(isNullable: Boolean) = create(isNullable, INT)
        .addStatement("return p0.getInteger(p1, 0)")
        .build()

    private fun colorIntFactory(isNullable: Boolean) = create(isNullable, INT, "colorInt")
        .addStatement("return p0.getColor(p1, 0)")
        .build()

    private fun dimenFloatFactory(isNullable: Boolean) = create(isNullable, FLOAT, "dimen")
        .addStatement("return p0.getDimension(p1, 0f)")
        .build()

    private fun drawableFactory(isNullable: Boolean) = create(isNullable, DRAWABLE, "drawable")
        .addStatement("return p0.getDrawable(p1)!!")
        .build()

    private fun pxIntFactory(isNullable: Boolean) = create(isNullable, INT, "pxInt")
        .addStatement("return p0.getDimensionPixelSize(p1, 0)")
        .build()

    private fun floatFactory(isNullable: Boolean) = create(isNullable, FLOAT)
        .addStatement("return p0.getFloat(p1, 0f)")
        .build()

    private fun resourceIntFactory(isNullable: Boolean) = create(isNullable, INT, "resourceInt")
        .addStatement("return p0.getResourceId(p1, 0)")
        .build()

    private fun create(isNullable: Boolean, klass: ClassName, name: String = klass.simpleName.decapitalize()) =
        FunSpec.builder("${name}${if (isNullable) "Nullable" else ""}Factory")
            .addModifiers(KModifier.PUBLIC)
            .returns(klass.copy(nullable = isNullable))
            .addParameter("p0", TYPED_ARRAY)
            .addParameter("p1", Int::class)
            .apply {
                if (isNullable) {
                    addStatement("if (!p0.hasValue(p1)) return null")
                } else {
                    addStatement("%N(p0, p1)", valueOrThrow)
                }
            }

    private val functionsMap: Map<AndroidType, Factory> = mapOf(
        AndroidType.COLOR to colorIntFactory(false),
        AndroidType.DIMEN to dimenFloatFactory(false),
        AndroidType.DRAWABLE to drawableFactory(false),
        AndroidType.BOOLEAN to booleanFactory(false),
        AndroidType.INTEGER to intFactory(false),
        AndroidType.PX to pxIntFactory(false),
        AndroidType.RESOURCE_ID to resourceIntFactory(false),
        AndroidType.FLOAT to floatFactory(false)
    )

    private val nullableFunctionsMap: Map<AndroidType, Factory> = mapOf(
        AndroidType.COLOR to colorIntFactory(true),
        AndroidType.DIMEN to dimenFloatFactory(true),
        AndroidType.DRAWABLE to drawableFactory(true),
        AndroidType.BOOLEAN to booleanFactory(true),
        AndroidType.INTEGER to intFactory(true),
        AndroidType.PX to pxIntFactory(true),
        AndroidType.RESOURCE_ID to resourceIntFactory(true),
        AndroidType.FLOAT to floatFactory(true)
    )

    fun forType(isNullable: Boolean, type: AndroidType): Factory {
        return when (isNullable) {
            true -> nullableFunctionsMap.getValue(type)
            false -> functionsMap.getValue(type)
        }
    }

    fun functions(): List<FunSpec> {
        return functionsMap.values + nullableFunctionsMap.values + valueOrThrow
    }
}

typealias Factory = FunSpec