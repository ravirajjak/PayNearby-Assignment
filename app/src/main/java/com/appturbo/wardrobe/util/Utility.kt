package com.appturbo.wardrobe.util

import android.R.attr.path
import com.appturbo.wardrobe.Clothes
import com.appturbo.wardrobe.bean.DataModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class Utility {
    companion object {
        val mHashFavourite = HashSet<String>()
    }

    fun setFiles(registered: String, value: DataModel) {
        var mList = HashMap<String, DataModel>()
        val mPref = PreferenceManager()
        val gson = Gson()
        val mStrPop = mPref.getPrefString(PreferenceKeys.PROFILE_DATA)
        if (mStrPop == null) {
            mList.put(registered, value)
            val mStrList = gson.toJson(mList)
            mPref.setPrefString(PreferenceKeys.PROFILE_DATA, mStrList)

        } else {
            val mPopList = genericType<HashMap<String, DataModel>>()
            mList = gson.fromJson(mStrPop, mPopList)
            mList.put(registered, value)
            val mStrList = gson.toJson(mList)
            mPref.setPrefString(PreferenceKeys.PROFILE_DATA, mStrList)
        }

    }

    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type


    fun getFileList(): Pair<ArrayList<DataModel>, ArrayList<DataModel>> {
        var mList: HashMap<String, DataModel>
        val mPref = PreferenceManager()
        val gson = Gson()
        val mStrList = mPref.getPrefString(PreferenceKeys.PROFILE_DATA)
        val mPopList = genericType<HashMap<String, DataModel>>()
        val mTopList = ArrayList<DataModel>()
        val mBottomList = ArrayList<DataModel>()
        if (mStrList != null) {
            mList = gson.fromJson(mStrList, mPopList)
            mList.forEach({ (key, value) ->
                if (value.type.equals(Clothes.TOP))
                    mTopList.add(value)
                else
                    mBottomList.add(value)
            })
        }
        return Pair(mTopList, mBottomList)
    }

    fun getFileName(filePath: String): String {
        if (filePath.isNotEmpty())
            return filePath.substring(filePath.lastIndexOf("/") + 1)
        return ""
    }

    fun getId(): String {
        return UUID.randomUUID().toString()

    }

    fun setFavourite(data: String) {
        mHashFavourite.add(data)
    }

    fun isFavourite(fav1: String, fav2: String): Boolean {
        val mFav = fav1.plus("-").plus(fav2)

        return if (mHashFavourite.contains(mFav)) {
            return true
        } else {
            return false
        }
    }
}