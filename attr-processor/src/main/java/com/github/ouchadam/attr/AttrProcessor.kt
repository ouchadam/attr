package com.github.ouchadam.attr

import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.Px
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import java.lang.IllegalArgumentException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(AttrProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("com.github.ouchadam.attr.Attr")
class AttrProcessor : AbstractProcessor() {

    private val theme = ClassName("android.content.res.Resources", "Theme")
    private val attributeSet = ClassName("android.util", "AttributeSet")
    private val typedArray = ClassName("android.content.res", "TypedArray")

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun process(elements: MutableSet<out TypeElement>, environment: RoundEnvironment): Boolean {
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        val factories = TypeFactories()
        val classFactories = environment.getElementsAnnotatedWith(Attr::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .map { classType(it as TypeElement, factories) }

        if (classFactories.isEmpty()) {
            return false
        }

        val packageOfMethod = "com.github.ouchadam.attr"

        val switcher = CodeBlock.Builder()
            .addStatement("when(T::class) {")
            .indent()
            .apply {
                classFactories.forEach {
                    addStatement("%T::class -> return %N(reader) as T", it.type, it.func)
                }
            }
            .addStatement("else -> throw IllegalStateException()")
            .unindent()
            .addStatement("}")
            .build()

        val themeAttrFunc = FunSpec.builder("attr")
            .addModifiers(KModifier.PUBLIC, KModifier.INLINE)
            .receiver(theme)
            .addParameter(ParameterSpec.builder("p0", attributeSet.copy(nullable = true)).defaultValue("null").build())
            .returns(TypeVariableName("T"))
            .addTypeVariable(TypeVariableName("T").copy(reified = true))
            .addStatement(
                "val reader: (IntArray) -> %T = { if (p0 == null) this.obtainStyledAttributes(it) else this.obtainStyledAttributes(p0, it, 0, 0) }",
                typedArray
            )
            .addCode(switcher)

        val file = File(generatedSourcesRoot).apply { mkdir() }
        FileSpec.builder(packageOfMethod, "AttrGenerated")
            .addFunctions(factories.functions())
            .addFunctions(classFactories.map { it.func })
            .addFunction(themeAttrFunc.build())
            .build()
            .writeTo(file)

        return false
    }

    private fun classType(klass: TypeElement, factories: TypeFactories): ClassType {
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
                    returnType = typedArray,
                    parameters = *arrayOf(IntArray::class.asTypeName())
                )
            )
            .returns(klass.asType().asTypeName())
            .addCode(body)
            .build()
        return ClassType(klass.asType(), classFactory)
    }

    private fun readConstructorParameters(element: TypeElement): List<Param> {
        val ctor = element.enclosedElements.find { it.kind == ElementKind.CONSTRUCTOR } as ExecutableElement
        return ctor.parameters.mapIndexed { index, parameter ->
            val attrId = parameter.getAnnotation(Attr.Id::class.java)?.value?.toString() ?: "R.attr.${parameter.simpleName}"
            val type = findType(parameter)
            Param(type, "v_$index", attrId, index, parameter.inferNullability())
        }
    }
}

private fun FileSpec.Builder.addFunctions(functions: List<FunSpec>): FileSpec.Builder {
    functions.forEach { this.addFunction(it) }
    return this
}

private data class Param(
    val type: AndroidType,
    val variableName: String,
    val attrIdLiteral: String,
    val parameterPosition: Int,
    val isNullable: Boolean
)

private data class ClassType(val type: TypeMirror, val func: FunSpec)

private fun findType(parameter: VariableElement): AndroidType {
    return when {
        parameter.getAnnotation(ColorInt::class.java) != null -> AndroidType.COLOR
        parameter.getAnnotation(Dimension::class.java) != null -> AndroidType.DIMEN
        parameter.getAnnotation(Px::class.java) != null -> AndroidType.PX
        else -> {
            when (parameter.asType().asTypeName()) {
                ClassName("java.lang", "Integer"), INT -> AndroidType.INTEGER
                ClassName("java.lang", "Boolean"), BOOLEAN -> AndroidType.BOOLEAN
                ClassName("java.lang", "Float"), FLOAT -> AndroidType.FLOAT
                else -> throw IllegalArgumentException(parameter.asType().toString())
            }
        }
    }
}

private fun Element.inferNullability(): Boolean {
    return this.asType().kind == TypeKind.DECLARED
}
