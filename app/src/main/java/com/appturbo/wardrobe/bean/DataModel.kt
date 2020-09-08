package com.appturbo.wardrobe.bean

import com.appturbo.wardrobe.Clothes

data class DataModel(
    val fileName: String,
    val filePath: String,
    val type: Clothes,
    val id: String
)