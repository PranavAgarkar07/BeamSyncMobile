package com.example.beamsyncmobile.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Permissions

@Serializable
object Home

@Serializable
object History

@Serializable
object Settings

@Serializable
object About

@Serializable
data class QrScanner(val mode: String)

@Serializable
object Downloads

@Serializable
object Uploads
