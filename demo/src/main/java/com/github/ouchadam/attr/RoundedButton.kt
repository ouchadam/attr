package com.github.ouchadam.attr

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.widget.AppCompatTextView

class RoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        val customAttributes = context.theme.attr<CustomAttributes>(attrs)

        val baseBackground =
            PaintDrawable(customAttributes.backgroundColor).apply { setCornerRadius(customAttributes.radius) }
        val rippleMask = PaintDrawable(Color.BLACK).apply { setCornerRadius(customAttributes.radius) }
        background = RippleDrawable(ColorStateList.valueOf(customAttributes.rippleColor), baseBackground, rippleMask)
    }
}

@Attr
data class CustomAttributes(
    @Attr.Id(R.attr.radius) @Dimension val radius: Float,
    @Attr.Id(R.attr.rippleColor) @ColorInt val rippleColor: Int,
    @Attr.Id(R.attr.backgroundColor) @ColorInt val backgroundColor: Int
)