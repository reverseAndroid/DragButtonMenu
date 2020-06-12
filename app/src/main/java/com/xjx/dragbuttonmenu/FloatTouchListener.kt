package com.wd.floatbutton

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import com.xjx.dragbuttonmenu.MainActivity
import java.lang.ref.WeakReference

class FloatTouchListener : OnTouchListener {

    private var mBoundsInScreen: Rect? = null

    companion object {
        var mFloatView: View? = null
    }

    private var mFloatViewWindowParam: FrameLayout.LayoutParams? = null
    private var mParentMarginTop = 0
    private var mPreviousX = 0f
    private var mPreviousY = 0f
    private var mHasMoved = false
    private var mTouchSlop = 0
    private var mEdgePaddingLeft = 0
    private var mEdgePaddingRight = 0
    private var mEdgePaddingTop = 0
    private var mEdgePaddingBottom = 0
    private var mFloatButtonCallback: FloatButtonCallback? = null
    private var mDownPointerId = 0
    private var mInterpolator: Interpolator? = null
    private val mUpdateListener: FloatAnimatorUpdateListener? = null

    constructor(
        context: Context?, boundsInScreen: Rect?, floatView: View?,
        floatViewWindowParam: FrameLayout.LayoutParams?, parentMarginTop: Int, edgePadding: Int
    ) {
        mBoundsInScreen = boundsInScreen
        mFloatView = floatView
        mFloatViewWindowParam = floatViewWindowParam
        mParentMarginTop = parentMarginTop
        mInterpolator = DecelerateInterpolator()
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mEdgePaddingBottom = edgePadding
        mEdgePaddingLeft = edgePadding
        mEdgePaddingRight = edgePadding
        mEdgePaddingTop = edgePadding
    }

    /**
     * 调整floatview布局
     *
     * @param v
     * @param event
     * @return
     */
    private fun adjustMarginParams(v: View, event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY
        val deltaX = x - mPreviousX
        val deltaY = y - mPreviousY
        if (!mHasMoved) {
            if (Math.abs(deltaX) < mTouchSlop && Math.abs(deltaY) < mTouchSlop) {
                return false
            }
        }
        //左上角位置
        var newX = x.toInt() - mFloatView!!.width / 2
        var newY = y.toInt() - mFloatView!!.height / 2
        newX = Math.max(newX, mBoundsInScreen!!.left + mEdgePaddingLeft)
        newX = Math.min(newX, mBoundsInScreen!!.right - mEdgePaddingRight - mFloatView!!.width)
        newY = Math.max(newY, mBoundsInScreen!!.top + mEdgePaddingTop)
        newY = Math.min(newY, mBoundsInScreen!!.bottom - mEdgePaddingBottom - mFloatView!!.height)
        mFloatViewWindowParam!!.leftMargin = newX
        mFloatViewWindowParam!!.topMargin = newY - mParentMarginTop
        return true
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (mFloatButtonCallback != null) {
            mFloatButtonCallback!!.onTouch()
        }
        var result = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownPointerId = event.getPointerId(0)
                mPreviousX = event.rawX
                mPreviousY = event.rawY
                result = isTouchPointInView(mFloatView, mPreviousX, mPreviousY)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mDownPointerId >= 0) {
                    val index = event.actionIndex
                    val id = event.getPointerId(index)
                    if (id == mDownPointerId) {
                        val update = adjustMarginParams(view, event)
                        if (!update) {
                            return false
                        }
                        mFloatView!!.requestLayout()
                        mHasMoved = true
                        result = true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mDownPointerId >= 0 && mHasMoved) {
                    event.action = MotionEvent.ACTION_CANCEL
                    adjustMarginParams(view, event)
                    mFloatView!!.requestLayout()
                }
                resetStatus()
            }
        }
        return result
    }

    //判断点击区域是否在按钮内，如果不在，返回true
    private fun isTouchPointInView(view: View?, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        val floatViewLeft = location[0]
        val floatViewTop = location[1]
        val floatViewRight = floatViewLeft + view!!.measuredWidth
        val floatViewBottom = floatViewTop + view.measuredHeight
        return !(y.toInt() in floatViewTop..floatViewBottom && x >= floatViewLeft && x <= floatViewRight)
    }

    private fun resetStatus() {
        mDownPointerId = -1
        mPreviousX = -1f
        mPreviousY = -1f
        mHasMoved = false
    }

    fun setFloatButtonCallback(floatButtonCallback: FloatButtonCallback?) {
        mFloatButtonCallback = floatButtonCallback
    }

    private inner class FloatAnimatorUpdateListener : AnimatorUpdateListener {

        private var mListener: WeakReference<FloatTouchListener>? = null

        fun setUpdateView(listener: FloatTouchListener) {
            mListener = WeakReference(listener)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue as Int
            var listener: FloatTouchListener? = null
            if (mListener == null || mListener!!.get().also { listener = it!! } == null) {
                return
            }
            listener?.mFloatViewWindowParam!!.leftMargin = value
            mFloatView?.requestLayout()
        }
    }

    /**
     * 触摸监听回调
     */
    interface FloatButtonCallback {
        //void onPositionChanged(int x, int y, int gravityX, float percentY);
        fun onTouch()
    }
}