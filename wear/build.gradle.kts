import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

val localProps = Properties().apply {
  val f = rootProject.file("local.properties")
  if (f.exists()) FileInputStream(f).use { load(it) }
}

android {
  namespace = "com.neurokim.runmetronm.wear"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.neurokim.pulserun"
    minSdk = 33
    targetSdk = 36
    versionCode = (project.property("runmetro.versionCode") as String).toInt()
    versionName = project.property("runmetro.versionName") as String
  }

  signingConfigs {
    create("release") {
      val path = localProps.getProperty("RUNMETRO_RELEASE_STORE_FILE")
      if (path != null) {
        storeFile = file(path)
        storePassword = localProps.getProperty("RUNMETRO_RELEASE_STORE_PASSWORD")
        keyAlias = localProps.getProperty("RUNMETRO_RELEASE_KEY_ALIAS")
        keyPassword = localProps.getProperty("RUNMETRO_RELEASE_KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig =
        if (localProps.getProperty("RUNMETRO_RELEASE_STORE_FILE") != null) {
          signingConfigs.getByName("release")
        } else {
          signingConfigs.getByName("debug")
        }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    aidl = false
    buildConfig = true
    shaders = false
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  lint {
    // False positive: MainActivity extends androidx.activity.ComponentActivity, which is an Activity.
    disable += "Instantiatable"
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.play.services.wearable)

  testImplementation(libs.junit)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.wear.compose.ui.tooling)
}

tasks.register("prepareKotlinBuildScriptModel")
