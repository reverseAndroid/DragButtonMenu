package com.wd.floatbutton.sector

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import com.wd.floatbutton.FloatTouchListener
import com.xjx.dragbuttonmenu.menu.HorizontalPosition
import com.xjx.dragbuttonmenu.menu.VerticalPosition

/**
 * 可以在任意位置显示的PopupWindow
 * 用法：
 * SmartPopupWindow popupWindow= SmartPopupWindow.Builder
 * .build(Activity.this, view)
 * .setAlpha(0.4f)                   //背景灰度     默认全透明
 * .setOutsideTouchDismiss(false)    //点击外部消失  默认true（消失）
 * .createPopupWindow();
 * popupWindow.showAtAnchorView(view, VerticalPosition.ABOVE, HorizontalPosition.CENTER);
 */
class SmartPopupWindow @JvmOverloads constructor(
    private var mContext: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PopupWindow(mContext, attrs, defStyleAttr) {
    private var mWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    private var mHeight = ViewGroup.LayoutParams.WRAP_CONTENT
    private var mAlpha = 1f //背景灰度  0-1  1表示全透明
    private var mContentView: View? = null
    private var isTouchOutsideDismiss = true //点击外部消失
    private var mAnimationStyle = -1

    //下面的几个变量只是位置处理外部点击事件（6.0以上）
    //是否只是获取宽高
    //getViewTreeObserver监听时
    private var isOnlyGetWH = true
    private var mAnchorView: View? = null

    @VerticalPosition
    private var mVerticalGravity = VerticalPosition.BELOW

    @HorizontalPosition
    private var mHorizontalGravity = HorizontalPosition.LEFT
    private var mOffsetX = 0
    private var mOffsetY = 0
    fun init() {
        contentView = mContentView
        height = mHeight
        width = mWidth
        touchOutsideDismiss(isTouchOutsideDismiss)
        if (mAnimationStyle != -1) {
            animationStyle = mAnimationStyle
        }
    }

    private fun touchOutsideDismiss(touchOutsideDismiss: Boolean) {
        if (!touchOutsideDismiss) {
            isFocusable = true
            isOutsideTouchable = false
            setBackgroundDrawable(null)
            contentView.isFocusable = true
            contentView.isFocusableInTouchMode = true
            contentView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss()
                    return@OnKeyListener true
                }
                false
            })
            //在Android 6.0以上 ，只能通过拦截事件来解决
            setTouchInterceptor(OnTouchListener { v, event ->
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (event.action == MotionEvent.ACTION_DOWN
                    && (x < 0 || x >= mWidth || y < 0 || y >= mHeight)
                ) {
                    //outside
                    return@OnTouchListener true
                } else if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    //outside
                    return@OnTouchListener true
                }
                false
            })
        } else {
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun showAtLocation(
        parent: View,
        gravity: Int,
        x: Int,
        y: Int
    ) {
        isOnlyGetWH = true
        mAnchorView = parent
        mOffsetX = x
        mOffsetY = y
        addGlobalLayoutListener(contentView)
        super.showAtLocation(parent, gravity, x, y)
    }

    @JvmOverloads
    fun showAtAnchorView(
        anchorView: View,
        @VerticalPosition verticalPos: Int,
        @HorizontalPosition horizontalPos: Int,
        fitInScreen: Boolean = true
    ) {
        showAtAnchorView(anchorView, verticalPos, horizontalPos, 0, 0, fitInScreen)
    }

    @JvmOverloads
    fun showAtAnchorView(
        anchorView: View,
        @VerticalPosition verticalPos: Int,
        @HorizontalPosition horizontalPos: Int,
        x: Int,
        y: Int,
        fitInScreen: Boolean = true
    ) {
        var x = x
        var y = y
        isOnlyGetWH = false
        mAnchorView = anchorView
        mOffsetX = x
        mOffsetY = y
        mVerticalGravity = verticalPos
        mHorizontalGravity = horizontalPos
        showBackgroundAnimator()
        val contentView = contentView
        addGlobalLayoutListener(contentView)
        isClippingEnabled = fitInScreen
        contentView.measure(
            makeDropDownMeasureSpec(width),
            makeDropDownMeasureSpec(height)
        )
        val measuredW = contentView.measuredWidth
        val measuredH = contentView.measuredHeight
        if (!fitInScreen) {
            val anchorLocation = IntArray(2)
            anchorView.getLocationInWindow(anchorLocation)
            x += anchorLocation[0]
            y += anchorLocation[1] + anchorView.height
        }
        y = calculateY(anchorView, verticalPos, measuredH, y)
        x = calculateX(anchorView, horizontalPos, measuredW, x)
        if (fitInScreen) {
            PopupWindowCompat.showAsDropDown(this, anchorView, x, y, Gravity.NO_GRAVITY)
        } else {
            showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
        }
    }

    /**
     * 根据垂直gravity计算y偏移
     */
    private fun calculateY(
        anchor: View,
        verticalGravity: Int,
        measuredH: Int,
        y: Int
    ): Int {
        var y = y
        when (verticalGravity) {
            VerticalPosition.ABOVE -> y -= measuredH + anchor.height
            VerticalPosition.ALIGN_BOTTOM -> y -= measuredH
            VerticalPosition.CENTER -> y -= anchor.height / 2 + measuredH / 2
            VerticalPosition.ALIGN_TOP -> y -= anchor.height
            VerticalPosition.BELOW -> {
            }
        }
        return y
    }

    /**
     * 根据水平gravity计算x偏移
     */
    private fun calculateX(
        anchor: View,
        horizontalGravity: Int,
        measuredW: Int,
        x: Int
    ): Int {
        var x = x
        when (horizontalGravity) {
            HorizontalPosition.LEFT -> x -= measuredW
            HorizontalPosition.ALIGN_RIGHT -> x -= measuredW - anchor.width
            HorizontalPosition.CENTER -> x += anchor.width / 2 - measuredW / 2
            HorizontalPosition.ALIGN_LEFT -> {
            }
            HorizontalPosition.RIGHT -> x += anchor.width
        }
        return x
    }

    //监听器，用于PopupWindow弹出时获取准确的宽高
    private val mOnGlobalLayoutListener = OnGlobalLayoutListener {
        mWidth = contentView.width
        mHeight = contentView.height
        //只获取宽高时，不执行更新操作
        if (isOnlyGetWH) {
            removeGlobalLayoutListener()
            return@OnGlobalLayoutListener
        }
        updateLocation(
            mWidth,
            mHeight,
            mAnchorView!!,
            mVerticalGravity,
            mHorizontalGravity,
            mOffsetX,
            mOffsetY
        )
        removeGlobalLayoutListener()
    }

    private fun updateLocation(
        width: Int, height: Int, anchor: View,
        @VerticalPosition verticalGravity: Int,
        @HorizontalPosition horizontalGravity: Int,
        x: Int, y: Int
    ) {
        var x = x
        var y = y
        x = calculateX(anchor, horizontalGravity, width, x)
        y = calculateY(anchor, verticalGravity, height, y)
        update(anchor, x, y, width, height)
    }

    private fun removeGlobalLayoutListener() {
        if (contentView != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                contentView.viewTreeObserver
                    .removeOnGlobalLayoutListener(mOnGlobalLayoutListener)
            } else {
                contentView.viewTreeObserver
                    .removeGlobalOnLayoutListener(mOnGlobalLayoutListener)
            }
        }
    }

    private fun addGlobalLayoutListener(contentView: View) {
        contentView.viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener)
    }

    override fun dismiss() {
        super.dismiss()
        dismissBackgroundAnimator()
        removeGlobalLayoutListener()
    }

    /**
     * 窗口显示，窗口背景透明度渐变动画
     */
    private fun showBackgroundAnimator() {
        if (mAlpha >= 1f) return
        val animator = ValueAnimator.ofFloat(1.0f, mAlpha)
        animator.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            setWindowBackgroundAlpha(alpha)
        }
        animator.duration = 360
        animator.start()
    }

    /**
     * 窗口隐藏，窗口背景透明度渐变动画
     */
    private fun dismissBackgroundAnimator() {
        if (mAlpha >= 1f) return
        val animator = ValueAnimator.ofFloat(mAlpha, 1.0f)
        animator.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            setWindowBackgroundAlpha(alpha)
        }
        animator.duration = 360
        animator.start()
    }

    /**
     * 控制窗口背景的不透明度
     */
    private fun setWindowBackgroundAlpha(alpha: Float) {
        if (mContext == null) return
        if (mContext is Activity) {
            val window = (mContext as Activity).window
            val layoutParams = window.attributes
            layoutParams.alpha = alpha
            window.attributes = layoutParams
        }
    }

    class Builder private constructor(activity: Activity, view: View) {
        private val mWindow: SmartPopupWindow
        fun setSize(width: Int, height: Int): Builder {
            mWindow.mWidth = width
            mWindow.mHeight = height
            return this
        }

        fun setAnimationStyle(animationStyle: Int): Builder {
            mWindow.mAnimationStyle = animationStyle
            return this
        }

        fun setAlpha(alpha: Float): Builder {
            mWindow.mAlpha = alpha
            return this
        }

        fun setOutsideTouchDismiss(dismiss: Boolean): Builder {
            mWindow.isTouchOutsideDismiss = dismiss
            return this
        }

        /**
         * 创建PopupWindow
         * @return
         */
        fun createPopupWindow(): SmartPopupWindow {
            mWindow.init()
            return mWindow
        }

        companion object {
            fun build(
                activity: Activity,
                view: View
            ): Builder {
                return Builder(activity, view)
            }
        }

        init {
            mWindow = SmartPopupWindow(activity)
            mWindow.mContext = activity
            mWindow.mContentView = view
        }
    }

    companion object {
        private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
            return View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(
                    measureSpec
                ), getDropDownMeasureSpecMode(measureSpec)
            )
        }

        private fun getDropDownMeasureSpecMode(measureSpec: Int): Int {
            return when (measureSpec) {
                ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.UNSPECIFIED
                else -> View.MeasureSpec.EXACTLY
            }
        }
    }

}