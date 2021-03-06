package com.github.ouchadam.attr

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("com.github.ouchadam.attr.Attr")
@Suppress("unused")
class AttrProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    private val factories = TypeFactories()
    private val attrParser = AttrAnnotationParser(factories)
    private val attrGenerator = AttrFunctionGenerator()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val attrAnnotations = parseAllAttrAnnotations(roundEnv)

        if (attrAnnotations.isNotEmpty()) {
            val attrFunc = attrGenerator.createAttrFunc(attrAnnotations)
            writeGeneratedCode(attrAnnotations, attrFunc)
        }
        return true
    }

    private fun parseAllAttrAnnotations(environment: RoundEnvironment) =
        environment.getElementsAnnotatedWith(Attr::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .filter { element -> element.kotlinMetadata is KotlinClassMetadata }
            .map { attrParser.parse(Create().create(it as TypeElement)) }

    private fun writeGeneratedCode(classFactories: List<AttrAnnotation>, themeAttrFunc: FunSpec.Builder) {
        val generatedSourcesRoot: String = options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        val packageOfMethod = "com.github.ouchadam.attr"
        val file = File(generatedSourcesRoot).apply { mkdir() }
        FileSpec.builder(packageOfMethod, "AttrGenerated")
            .addFunctions(factories.functions())
            .addFunctions(classFactories.map { it.factoryFunc })
            .addFunction(themeAttrFunc.build())
            .build()
            .writeTo(file)
    }
}

private fun FileSpec.Builder.addFunctions(functions: List<FunSpec>): FileSpec.Builder {
    functions.forEach { this.addFunction(it) }
    return this
}

