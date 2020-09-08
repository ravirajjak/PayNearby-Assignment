package com.appturbo.wardrobe

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appturbo.wardrobe.databinding.DialogOptionBinding
import com.appturbo.wardrobe.interfaces.OnOptionSelected
import com.appturbo.wardrobe.util.OptionType
import com.appturbo.wardrobe.util.Utility


open class BaseActivity : AppCompatActivity() {
    var dialog: Dialog? = null
    val mUtility by lazy {
        Utility()
    }

    fun hideOptionDialog() {
        if (dialog != null) {
            if (dialog!!.isShowing()) dialog!!.dismiss()
        }
    }

    companion object {
        fun newInstance(mClass: Class<*>): Class<*> {
            return mClass
        }
    }

    fun showOptionDialog(listener: OnOptionSelected) {
        dialog = getOptionDialog(listener)
        dialog!!.show()
    }

    fun getDisplayMetric(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return Pair(width, height)
    }

    open fun getOptionDialog(listener: OnOptionSelected): Dialog {
        if (dialog == null) {
            dialog = Dialog(this)
            dialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
            val mPair = getDisplayMetric()
            dialog!!.window?.setLayout((6 * mPair.first) / 7, (4 * mPair.second) / 5)
        }


        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(false)
        val inflater = LayoutInflater.from(this)
        val binding: DialogOptionBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_option, null, false)
        dialog!!.setContentView(binding.root)

        binding.linCamera.setOnClickListener {
            listener.onOptionSelected(OptionType.CAMERA)
            hideOptionDialog()
        }
        binding.linGallery.setOnClickListener {
            listener.onOptionSelected(OptionType.GALLERY)
            hideOptionDialog()
        }
        return dialog!!
    }
}