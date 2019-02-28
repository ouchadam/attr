package com.github.ouchadam.attr

import com.squareup.kotlinpoet.FunSpec
import javax.lang.model.type.TypeMirror

internal data class AttrAnnotation(val type: TypeMirror, val factoryFunc: FunSpec)