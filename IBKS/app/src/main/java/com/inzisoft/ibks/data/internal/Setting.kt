package com.inzisoft.ibks.data.internal

data class Setting(val titleResId: Int, val defaultIcon: Int, val pressedIcon: Int, val onSelected: () -> Unit)

data class OpenSourceLicense(val title: String, val fileName: String)