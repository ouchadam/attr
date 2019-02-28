package com.github.ouchadam.attr

import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.Px
import com.github.ouchadam.attr.Classes.DRAWABLE
import com.github.ouchadam.attr.Classes.JAVA_BOOLEAN
import com.github.ouchadam.attr.Classes.JAVA_FLOAT
import com.github.ouchadam.attr.Classes.JAVA_INT
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

internal class AttrAnnotationParser(private val factories: TypeFactories) {

    fun parse(klass: AttrClass): AttrAnnotation {
        val params = klass.params
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
            .addStatement("return %T(${params.map { it.variableName }.joinToString(",")})", klass.type)
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
            .returns(klass.type.asTypeName())
            .addCode(body)
            .build()
        return AttrAnnotation(klass.type, classFactory)
    }

}

internal data class AttrClass(
    val params: List<Param>,
    val type: TypeMirror,
    val simpleName: Name
)

internal data class Param(
    val type: AndroidType,
    val variableName: String,
    val attrIdLiteral: String,
    val parameterPosition: Int,
    val isNullable: Boolean
)

internal class Create {

    fun create(element: TypeElement): AttrClass {
        val kotlinMetadata = element.kotlinMetadata as KotlinClassMetadata
        val nameResolver = kotlinMetadata.data.nameResolver
        fun ProtoBuf.Type.extractFullName() = extractFullName(kotlinMetadata.data)

        val kotlinParams =
            kotlinMetadata.data.classProto.constructorList.single { it.isPrimary }.valueParameterList.map {
                val name = nameResolver.getString(it.name)
                ConstructorParam(name, it.type.extractFullName().contains("?"))
            }

        val ctor = element.enclosedElements.first { it.kind == ElementKind.CONSTRUCTOR } as ExecutableElement

        val attrParams = ctor.parameters.mapIndexed { index, parameter ->
            val simpleName = parameter.simpleName

            val kotlinParam = kotlinParams.first { it.name == simpleName.toString() }

            val attrId =
                parameter.getAnnotation(Attr.Id::class.java)?.value?.toString() ?: "R.attr.$simpleName"
            val type = findType(parameter)
            Param(type, "v_$index", attrId, index, kotlinParam.isNullable)


        }
        return AttrClass(attrParams, element.asType(), element.simpleName)

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
                    DRAWABLE -> AndroidType.DRAWABLE
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }
}

data class ConstructorParam(val name: String, val isNullable: Boolean)

