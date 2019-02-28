package com.github.ouchadam.attr

import com.squareup.kotlinpoet.*

internal class AttrFunctionGenerator {

    fun createAttrFunc(classFactories: List<AttrAnnotation>): FunSpec.Builder {
        val switcher = CodeBlock.Builder()
            .addStatement("when(T::class) {")
            .indent()
            .apply {
                classFactories.forEach {
                    addStatement("%T::class -> return %N(reader) as T", it.type, it.factoryFunc)
                }
            }
            .addStatement("else -> throw IllegalStateException()")
            .unindent()
            .addStatement("}")
            .build()

        return FunSpec.builder("attr")
            .addModifiers(KModifier.PUBLIC, KModifier.INLINE)
            .receiver(Classes.THEME)
            .addParameter(
                ParameterSpec.builder(
                    "p0",
                    Classes.ATTRIBUTE_SET.copy(nullable = true)
                ).defaultValue("null").build())
            .returns(TypeVariableName("T"))
            .addTypeVariable(TypeVariableName("T").copy(reified = true))
            .addStatement(
                "val reader: (IntArray) -> %T = { if (p0 == null) this.obtainStyledAttributes(it) else this.obtainStyledAttributes(p0, it, 0, 0) }",
                Classes.TYPED_ARRAY
            )
            .addCode(switcher)
    }

}