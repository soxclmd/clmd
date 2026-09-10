plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "ph.gov.deped.region12.soxclmd"
    compileSdk = 34

    defaultConfig {
        applicationId = "ph.gov.deped.region12.soxclmd"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // Keep R8 off for v1 so first CI builds are deterministic; enable later
            // once ProGuard rules are validated on a release build.
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

/* ------------------------------------------------------------------ */
/* Export task: copies the debug runtime classpath (jars + classes.jar */
/* extracted from AARs) into build/exported-libs so D8 can be run as  */
/* a standalone process on memory-constrained machines. Not used by   */
/* CI, which runs a normal `assembleDebug`/`bundleDebug`.              */
/* ------------------------------------------------------------------ */
tasks.register("exportDebugClasspath") {
    doLast {
        val out = file(layout.buildDirectory.dir("exported-libs"))
        out.mkdirs()
        out.listFiles()?.forEach { it.delete() }
        var i = 0
        configurations.getByName("debugRuntimeClasspath").resolve().forEach { f ->
            when {
                f.name.endsWith(".aar") -> copy {
                    from(zipTree(f).matching { include("classes.jar") })
                    into(out)
                    rename { "aar-${i++}.jar" }
                }
                f.name.endsWith(".jar") -> copy {
                    from(f)
                    into(out)
                }
            }
        }
        logger.lifecycle("exported ${out.listFiles()?.size} jars to ${out}")
    }
}
