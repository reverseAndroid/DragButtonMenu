package com.xjx.dragbuttonmenu

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.PopupWindowCompat
import com.wd.floatbutton.FloatTouchListener
import com.wd.floatbutton.FloatTouchListener.FloatButtonCallback
import com.wd.floatbutton.sector.SmartPopupWindow
import com.xjx.dragbuttonmenu.menu.HorizontalPosition
import com.xjx.dragbuttonmenu.menu.TestPopupWindow
import com.xjx.dragbuttonmenu.menu.VerticalPosition

class MainActivity : AppCompatActivity() {

    private var mFloatBtnWrapper: RelativeLayout? = null
    private var mFloatBtnWindowParams: FrameLayout.LayoutParams? = null
    private var mFloatRootView: FrameLayout? = null
    private var mMainLayout: ConstraintLayout? = null
    private var mFloatTouchListener: FloatTouchListener? = null
    private var mFloatViewBoundsInScreens: Rect? = null
    private var mEdgePadding = 0
    private var mImageView: ImageView? = null

    companion object {
        var mWindow: TestPopupWindow? = null
        var mPopupContentView: View? = null
    }

    private var mGravity = Gravity.START
    private var mOffsetX = 0
    private var mOffsetY = 0

    private val useSmartPopup = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMainLayout = findViewById(R.id.main_layout)
        mPopupContentView = layoutInflater.inflate(R.layout.popup_test_vertical, null)
        addFloatBtn()
        setTouchListener()
    }

    /**
     * 添加浮动按钮
     */
    private fun addFloatBtn() {
        mFloatBtnWrapper =
            LayoutInflater.from(this).inflate(R.layout.float_btn, null, false) as RelativeLayout
        mImageView = mFloatBtnWrapper!!.findViewById(R.id.iv_shine)
        mFloatBtnWindowParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mFloatRootView = FrameLayout(this)
        mMainLayout!!.addView(
            mFloatRootView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        mFloatRootView!!.addView(mFloatBtnWrapper, mFloatBtnWindowParams)
    }

    /**
     * 设置触摸监听
     */
    private fun setTouchListener() {
        val scale = this@MainActivity.resources.displayMetrics.density
//        mEdgePadding = (10 * scale + 0.5).toInt()
        mEdgePadding = 0
        mFloatRootView!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mFloatRootView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mFloatViewBoundsInScreens = Rect()
                val mainLocation = IntArray(2)
                mMainLayout!!.getLocationOnScreen(mainLocation)
                mFloatViewBoundsInScreens!![mainLocation[0], mainLocation[1], mainLocation[0] + mMainLayout!!.width] =
                    mMainLayout!!.height + mainLocation[1]
                mFloatTouchListener = FloatTouchListener(
                    this@MainActivity,
                    mFloatViewBoundsInScreens,
                    mFloatBtnWrapper,
                    mFloatBtnWindowParams,
                    mainLocation[1],
                    mEdgePadding
                )
                mFloatTouchListener!!.setFloatButtonCallback(object : FloatButtonCallback {
                    override fun onTouch() {

                    }
                })
                mFloatRootView!!.setOnTouchListener(mFloatTouchListener)
                mFloatRootView?.isLongClickable = false
                mFloatRootView!!.setOnClickListener {
//                    val rotateAnimation = RotateAnimation(
//                        0F,
//                        360F,
//                        Animation.RELATIVE_TO_SELF,
//                        0.5f,
//                        Animation.RELATIVE_TO_SELF,
//                        0.5f
//                    )
//                    rotateAnimation.duration = 1000
//                    rotateAnimation.repeatCount = 3
//                    mImageView!!.startAnimation(rotateAnimation)

                    initPopupWindow()

                    val toParentHeight =
                        mWindow!!.contentView.measuredHeight - FloatTouchListener.mFloatView?.bottom!!
                    val toParentWidthLeft =
                        FloatTouchListener.mFloatView?.left!! - mWindow!!.contentView.measuredWidth
                    val toParentWidthRight =
                        FloatTouchListener.mFloatView?.right!! - mWindow!!.contentView.measuredWidth

                    val verticalPosition: Int
                    val horizontalPosition: Int

                    if (toParentHeight < 0) {
                        verticalPosition = VerticalPosition.ALIGN_BOTTOM
                    } else {
                        verticalPosition = VerticalPosition.ALIGN_TOP
                    }

                    if (toParentWidthLeft > 0 && toParentWidthRight > toParentWidthRight - mWindow!!.contentView.measuredWidth) {
                        horizontalPosition = HorizontalPosition.LEFT
                    } else {
                        horizontalPosition = HorizontalPosition.RIGHT
                    }

                    //使用SmartPopup
                    SmartPopupWindow.Builder
                        .build(this@MainActivity, mPopupContentView!!)
                        .createPopupWindow()
                        .showAtAnchorView(
                            FloatTouchListener.mFloatView!!,
                            verticalPosition,
                            horizontalPosition
                        )

                    val imageCopy = mPopupContentView!!.findViewById<ImageView>(R.id.popup_test_iv1)
                    val imageHeart =
                        mPopupContentView!!.findViewById<ImageView>(R.id.popup_test_iv2)
                    val imageInfo = mPopupContentView!!.findViewById<ImageView>(R.id.popup_test_iv3)
                    val imageSearch =
                        mPopupContentView!!.findViewById<ImageView>(R.id.popup_test_iv4)
                    val imageSettings =
                        mPopupContentView!!.findViewById<ImageView>(R.id.popup_test_iv5)

                    imageCopy.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Copy",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    imageHeart.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Heart",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    imageInfo.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Info",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    imageSearch.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Search",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    imageSettings.setOnClickListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun initPopupWindow() {
        mWindow = TestPopupWindow(this)
        val contentView = mWindow!!.getContentView()
        //需要先测量，PopupWindow还未弹出时，宽高为0
        contentView.measure(
            makeDropDownMeasureSpec(mWindow!!.getWidth()),
            makeDropDownMeasureSpec(mWindow!!.getHeight())
        )
    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode: Int
        mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            View.MeasureSpec.UNSPECIFIED
        } else {
            View.MeasureSpec.EXACTLY
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }

    private fun showPopup() {
        PopupWindowCompat.showAsDropDown(
            mWindow!!,
            FloatTouchListener.mFloatView!!,
            mOffsetX,
            mOffsetY,
            mGravity
        )
    }
}