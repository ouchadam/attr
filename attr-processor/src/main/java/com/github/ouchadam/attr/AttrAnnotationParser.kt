package com.github.ouchadam.attr

import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.IdRes
import android.support.annotation.Px
import com.github.ouchadam.attr.Classes.JAVA_BOOLEAN
import com.github.ouchadam.attr.Classes.JAVA_FLOAT
import com.github.ouchadam.attr.Classes.JAVA_INT
import com.squareup.kotlinpoet.*
import java.lang.IllegalArgumentException
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind

internal class AttrAnnotationParser(private val factories: TypeFactories) {

    fun parse(klass: TypeElement): AttrAnnotation {
        val params = readConstructorParameters(klass)
        val listOfAttrIds = params.map { it.attrIdLiteral }.sorted().joinToString(",", "intArrayOf(", ")")
        val body = CodeBlock.builder()
            .addStatement("val bar = p0(%L)", listOfAttrIds)
            .addStatement("try {")
            .indent()
            .apply {
                params.sortedBy { it.attrIdLiteral }.forEachIndexed { index, param ->
                    val factory = factories.forType(param.isNullable, param.type)
                    addStatement("val %L = %N(bar, %L)", param.variableName, factory, index)
                }
            }
            .addStatement("return %T(${params.map { it.variableName }.joinToString(",")})", klass.asType())
            .unindent()
            .addStatement("} finally { bar.recycle() }")
            .build()

        val classFactory = FunSpec.builder("${klass.simpleName}")
            .addModifiers(KModifier.PUBLIC)
            .addParameter(
                "p0", LambdaTypeName.get(
                    returnType = Classes.TYPED_ARRAY,
                    parameters = *arrayOf(IntArray::class.asTypeName())
                )
            )
            .returns(klass.asType().asTypeName())
            .addCode(body)
            .build()
        return AttrAnnotation(klass.asType(), classFactory)
    }

    private fun readConstructorParameters(element: TypeElement): List<Param> {
        val ctor = element.enclosedElements.find { it.kind == ElementKind.CONSTRUCTOR } as ExecutableElement
        return ctor.parameters.mapIndexed { index, parameter ->
            val attrId =
                parameter.getAnnotation(Attr.Id::class.java)?.value?.toString() ?: "R.attr.${parameter.simpleName}"
            val type = findType(parameter)
            Param(type, "v_$index", attrId, index, parameter.inferNullability())
        }
    }

    private fun findType(parameter: VariableElement): AndroidType {
        return when {
            parameter.getAnnotation(ColorInt::class.java) != null -> AndroidType.COLOR
            parameter.getAnnotation(Dimension::class.java) != null -> AndroidType.DIMEN
            parameter.getAnnotation(Px::class.java) != null -> AndroidType.PX
            parameter.getAnnotation(IdRes::class.java) != null -> AndroidType.RESOURCE_ID
            else -> {
                when (parameter.asType().asTypeName()) {
                    JAVA_INT, INT -> AndroidType.INTEGER
                    JAVA_BOOLEAN, BOOLEAN -> AndroidType.BOOLEAN
                    JAVA_FLOAT, FLOAT -> AndroidType.FLOAT
                    else -> throw IllegalArgumentException(parameter.asType().toString())
                }
            }
        }
    }

    private fun Element.inferNullability(): Boolean {
        return this.asType().kind == TypeKind.DECLARED
    }

    private data class Param(
        val type: AndroidType,
        val variableName: String,
        val attrIdLiteral: String,
        val parameterPosition: Int,
        val isNullable: Boolean
    )
}