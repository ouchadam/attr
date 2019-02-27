package com.github.ouchadam.attr

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val themeAttributes = theme.attr<ThemeAttributes>()

        customView.setTextColor(themeAttributes.colorPrimary)
        customView.setOnClickListener {
            Toast.makeText(this, "is light theme: ${themeAttributes.isLightTheme}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Attr
data class ThemeAttributes(
    @Attr.Id(R.attr.colorPrimary) @ColorInt val colorPrimary: Int,
    @Attr.Id(R.attr.isLightTheme) val isLightTheme: Boolean
)

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        val customAttributes = context.theme.attr<CustomAttributes>(attrs)
        val baseBackground = PaintDrawable(customAttributes.backgroundColor).apply {
            setCornerRadius(customAttributes.radius)
        }
        val rippleMask = PaintDrawable(Color.BLACK).apply { setCornerRadius(customAttributes.radius) }
        background = RippleDrawable(ColorStateList.valueOf(customAttributes.rippleColor), baseBackground, rippleMask)
    }

    @Attr
    data class CustomAttributes(
        @Attr.Id(R.attr.radius) @Dimension val radius: Float,
        @Attr.Id(R.attr.rippleColor) @ColorInt val rippleColor: Int,
        @Attr.Id(R.attr.backgroundColor) @ColorInt val backgroundColor: Int
    )
}