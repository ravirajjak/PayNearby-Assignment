package com.appturbo.wardrobe.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appturbo.wardrobe.Clothes
import com.appturbo.wardrobe.R
import com.appturbo.wardrobe.bean.DataModel
import com.appturbo.wardrobe.databinding.LayoutItemImagesBinding
import com.appturbo.wardrobe.interfaces.OnFavouriteFoundListener
import com.appturbo.wardrobe.util.Utility
import java.io.File


class TopAdapter(val onFavouriteFoundListener: OnFavouriteFoundListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var mDataList: ArrayList<DataModel>
    val TYPE_BOTTOM = 1
    val TYPE_TOP = 2

    companion object {
        var mFavouriteFromBottom = ""
    }

    private val mUtility by lazy {
        Utility()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = DataBindingUtil.inflate<LayoutItemImagesBinding>(
            layoutInflater,
            R.layout.layout_item_images,
            parent,
            false
        )
        return TopViewHolder(binding)
    }

    fun setData(mDataList: List<DataModel>) {
        this.mDataList = mDataList as ArrayList<DataModel>
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        if (!::mDataList.isInitialized)
            mDataList = ArrayList()
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList.get(position).type.equals(Clothes.TOP)) {
            TYPE_TOP
        } else {
            TYPE_BOTTOM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBind(holder, position)
    }

    private fun onBind(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TopViewHolder -> {
                BottomAdapter.mFavouriteFromTop = mDataList.get(position).id
                val imgFile = File(mDataList.get(position).filePath)

                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
                    holder.binding.layItemImgDisplay.setImageBitmap(myBitmap)
                }
            }
        }
    }


    class TopViewHolder(val binding: LayoutItemImagesBinding) :
        RecyclerView.ViewHolder(binding.root)

    class BottomViewHolder(val binding: LayoutItemImagesBinding) :
        RecyclerView.ViewHolder(binding.root)

}