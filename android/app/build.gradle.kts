plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "org.sicdic.app"
    compileSdk = 35
    defaultConfig { applicationId = "org.sicdic.calculator"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0-dev" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { buildConfig = true }
    lint { abortOnError = true; checkReleaseBuilds = true }
    buildTypes { release { isMinifyEnabled = false } }
}
dependencies { implementation(project(":core")) }
