package com.github.ouchadam.attr

import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val shape = theme.attr<Shape>()


        button.setOnClickListener {  }
    }

}

@Attr
data class Shape(
    @Attr.Id(R.attr.colorPrimary) @ColorInt val colorPrimary: Int,
    @Attr.Id(R.attr.isLightTheme) val isLightTheme: Boolean,
    @Attr.Id(R.attr.dividerPadding) @Dimension val dividerPaddingDp: Float,
    @Attr.Id(R.attr.dividerPadding) @Px val dividerPaddingPx: Int,
    @Attr.Id(R.attr.layout_constraintCircleAngle) val toolTipAnimTime: Int
)