package com.appturbo.wardrobe

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.AbsListViewBindingAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.appturbo.wardrobe.adapter.BottomAdapter
import com.appturbo.wardrobe.adapter.TopAdapter
import com.appturbo.wardrobe.bean.DataModel
import com.appturbo.wardrobe.databinding.ActivityMainBinding
import com.appturbo.wardrobe.interfaces.OnFavouriteFoundListener
import com.appturbo.wardrobe.interfaces.OnOptionSelected
import com.appturbo.wardrobe.util.OptionType
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), OnOptionSelected, OnFavouriteFoundListener {


    lateinit var mTopAdapter: TopAdapter
    lateinit var mBottomAdapter: BottomAdapter
    lateinit var mTopList: ArrayList<DataModel>
    lateinit var mBottomList: ArrayList<DataModel>
    lateinit var mHomeModel: HomeViewModel
    lateinit var binding: ActivityMainBinding
    lateinit var TYPE: Clothes

    companion object {
        var mFavouriteBottomId: String = ""
        var mFavouriteTopId: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
    }

    private fun init() {

        setDashText()
        setSpeedDialListener()
        setViewModel()
        setSpeedDial()
        initializeAdapter()
        onScrollListener()
        setOnClickListener()

    }


    private fun setOnClickListener() {
        binding.incPosterTop.incPosterRelParent.setOnClickListener {
            TYPE = Clothes.TOP
            verifyBottomSheetState()
        }
        binding.incPosterBottom.incPosterRelParent.setOnClickListener {
            TYPE = Clothes.BOTTOM
            verifyBottomSheetState()
        }
    }


    private fun setDashText() {
        binding.incPosterBottom.tvText.text = getString(R.string.lbl_click_to_add_bottom)
    }


    private fun setSpeedDial() {
        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_pant_label, R.drawable.ic_pant)
                .setLabel(getString(R.string.lbl_add_bottom))
                .setTheme(R.style.AppTheme_Purple)
                .create()
        )

        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_shirt_label, R.drawable.ic_shirt)
                .setLabel(getString(R.string.lbl_add_top))
                .setTheme(R.style.AppTheme_Purple)
                .create()
        )

        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_favourite_label, R.drawable.ic_heart)
                .setLabel(getString(R.string.lbl_favourite))
                .setTheme(R.style.AppTheme_Purple)
                .create()
        )
        binding.speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_shuffle_label, R.drawable.ic_shuffle)
                .setLabel(getString(R.string.lbl_shuffle))
                .setTheme(R.style.AppTheme_Purple)
                .create()
        )
    }


    private fun setViewModel() {
        mHomeModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }


    private fun setSpeedDialListener() {
        binding.speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {

                R.id.fab_shirt_label -> {
                    TYPE = Clothes.TOP
                    verifyBottomSheetState()
                    return@OnActionSelectedListener true // false will close it without animation
                }

                R.id.fab_pant_label -> {
                    TYPE = Clothes.BOTTOM
                    verifyBottomSheetState()
                    return@OnActionSelectedListener true // false will close it without animation
                }

                R.id.fab_shuffle_label -> {

                    Collections.shuffle(mTopList)
                    Collections.shuffle(mBottomList)
                    mTopAdapter.notifyDataSetChanged()
                    mBottomAdapter.notifyDataSetChanged()
                    return@OnActionSelectedListener true // false will close it without animation

                }
                R.id.fab_favourite_label -> {
                    mUtility.setFavourite(mFavouriteTopId.plus("-").plus(mFavouriteBottomId))
                    return@OnActionSelectedListener true // false will close it without animation
                }
            }
            false
        })
    }


    private fun setReadPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1002
                )
            } else {
                openGallery()
            }
        }
        binding.speedDial.close()

    }

    private fun setCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    1001
                )
            } else {
                openCamera()
            }
        }
        binding.speedDial.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onAppActivityResult(requestCode, resultCode, data)
    }

    private fun onAppActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val imageData = data?.extras?.get("data") as Bitmap
            if (imageData != null) {
                val path = filesDir.toString()
                val file = File(
                    path,
                    "Wardrobe".plus("-")
                        .plus(dateToString(Date(), "yyyy-MM-dd-hh-mm-ss")).plus(".jpg")
                )
                val fOut = FileOutputStream(file)
                // obtaining the Bitmap
                imageData.compress(
                    Bitmap.CompressFormat.JPEG,
                    50,
                    fOut
                ) // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
                val mDataModel = DataModel(file.name, file.absolutePath, TYPE, mUtility.getId())
                setGenericAdapter(mDataModel)
                mUtility.setFiles(file.name, mDataModel)
                Log.d("FILENAME ", file.name.plus(" | ").plus(file.absolutePath))
            }
        } else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri = data?.data!!
            val filePathColumn =
                arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? =
                contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor?.moveToFirst()
            val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
            val picturePath: String? = columnIndex?.let { cursor.getString(it) }
            picturePath?.let {
                val mFileName = mUtility.getFileName(it)
                val mDataModel = DataModel(mFileName, picturePath, TYPE, mUtility.getId())
                setGenericAdapter(mDataModel)
                mUtility.setFiles(mFileName, mDataModel)
                Log.d("WOW PICT", picturePath)
            }
            cursor?.close()
        }
    }


    private fun setGenericAdapter(dataModel: DataModel) {
        if (TYPE.equals(Clothes.TOP)) {
            setTopAdapter(dataModel)
        } else {
            setBottomAdapter(dataModel)
        }
    }

    fun initializeAdapter() {

        mTopAdapter = TopAdapter(this)
        mBottomAdapter = BottomAdapter(this)
        binding.actMainRecylerviewTop.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.actMainRecylerviewBottom.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.actMainRecylerviewTop.adapter = mTopAdapter
        binding.actMainRecylerviewBottom.adapter = mBottomAdapter

        mTopList = ArrayList()
        mBottomList = ArrayList()

        setSnapHelper()
        verifyDataExistAndRender()
    }

    private fun verifyDataExistAndRender() {
        val mPair = mUtility.getFileList()

        val mFirst = mPair.first
        mTopList.addAll(mFirst)
        mFirst?.let {
            if (it.size > 0) {
                setTopVisibility()
                mTopAdapter.setData(mTopList)
            }
        }
        val mSecond = mPair.second
        mBottomList.addAll(mPair.second)
        mSecond?.let {
            if (it.size > 0) {
                setBottomVisibility()
                mBottomAdapter.setData(mBottomList)
            }
        }
    }

    private fun setSnapHelper() {
        val snapHelperTop: SnapHelper = LinearSnapHelper()
        val snapHelperBottom: SnapHelper = LinearSnapHelper()
        snapHelperTop.attachToRecyclerView(binding.actMainRecylerviewTop)
        snapHelperBottom.attachToRecyclerView(binding.actMainRecylerviewBottom)
    }

    private fun setTopAdapter(dataModel: DataModel) {
        setTopVisibility()
        mTopList.add(0, dataModel)
        mTopAdapter.setData(mTopList)
    }


    fun setTopVisibility() {
        binding.incPosterTop.incPosterRelParent.visibility = View.GONE
        binding.actMainRecylerviewTop.visibility = View.VISIBLE
    }


    fun setBottomVisibility() {
        binding.incPosterBottom.incPosterRelParent.visibility = View.GONE
        binding.actMainRecylerviewBottom.visibility = View.VISIBLE
    }


    private fun setBottomAdapter(dataModel: DataModel) {
        setBottomVisibility()
        mBottomList.add(0, dataModel)
        mBottomAdapter.setData(mBottomList)
    }


    private fun openCamera() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(intent, 101);
    }


    private fun verifyBottomSheetState() {
        showOptionDialog(this)
    }


    private fun dateToString(date: Date, format: String): String {
        return SimpleDateFormat(format).format(date)
    }


    private fun openGallery() {
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(i, 102)
    }


    fun onScrollListener() {
        var lastClickTime = 0L
        binding.actMainRecylerviewTop.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val mLinearLayoutManager =
                    recyclerView.layoutManager as LinearLayoutManager
                val mPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
                if (SystemClock.elapsedRealtime() - lastClickTime < 600) {
                    return
                }
                mFavouriteTopId = mTopList.get(mPosition).id
                Log.d("POSITION ", mPosition.toString())
                verifyFavourite()
                lastClickTime = SystemClock.elapsedRealtime()

            }
        })


        binding.actMainRecylerviewBottom.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val mLinearLayoutManager =
                    binding.actMainRecylerviewBottom.layoutManager as LinearLayoutManager
                val mPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
                mFavouriteBottomId = mBottomList.get(mPosition).id
                verifyFavourite()

                Log.d("POSITION ", mPosition.toString())
            }
        })

    }


    fun verifyFavourite() {

        if (mUtility.isFavourite(mFavouriteTopId, mFavouriteBottomId)) {
            binding.incAppHeader.incAhFavourite.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.colorRed
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.incAppHeader.incAhFavourite.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimaryDark
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
                openCamera()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 1002) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "read permission granted", Toast.LENGTH_LONG).show()
                openGallery()
            } else {
                Toast.makeText(this, "read permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onOptionSelected(option: OptionType) {
        if (option.equals(OptionType.CAMERA)) {
            setCameraPermission()
        } else if (option.equals(OptionType.GALLERY)) {
            setReadPermission()
        }
    }


    override fun onFoundFavourite(boolean: Boolean) {
        if (boolean) {
            Toast.makeText(this, "Favourite", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("NO FAVOURITE", "FALSE")
        }
    }
}