package com.inzisoft.ibks.base

sealed interface PopupState
object Cancel : PopupState
object Left : PopupState
object Right : PopupState
