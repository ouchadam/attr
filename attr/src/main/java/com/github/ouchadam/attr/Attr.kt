package com.github.ouchadam.attr

import android.support.annotation.AttrRes

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Attr {

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class Id(@AttrRes val value: Int)

}