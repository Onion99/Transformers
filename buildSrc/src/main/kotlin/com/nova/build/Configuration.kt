package com.nova.build

object Configuration {
  const val app = "Transformers"
  const val compileSdk = 33
  const val targetSdk = 33
  const val minSdk = 23
  private const val majorVersion = 1
  private const val minorVersion = 0
  const val patchVersion = 52
  const val versionName = "$majorVersion.$minorVersion.$patchVersion"
  var versionCode = "$majorVersion$minorVersion$patchVersion".toInt()
  const val nameSpace= "com.nova.star"
  const val pluginGroup   = "com.nova.sun.plugin"
  const val pluginVersion = "$majorVersion.$minorVersion.$patchVersion"
  const val debugApplicationId= "com.nova.beta"
  const val debugSignPassWord = "nova9999"
  const val debugSignAlias = "nova"
  const val releaseApplicationId = "com.nova.omega"
  const val releaseSignPassWord = "nova9999"
  const val releaseSignAlias = "nova"
}
