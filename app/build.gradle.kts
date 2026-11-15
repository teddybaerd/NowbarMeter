plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val packageName = "de.teddybaerd.nowbarmeter"

private val androidLocaleQualifierRegex = Regex("""^(?:[a-z]{2,3}(?:-r[A-Z]{2})?|b\+[A-Za-z]{2,3}(?:\+[A-Za-z0-9]{2,8})*)$""")

private fun discoverLocaleFilters(resDirectory: java.io.File): List<String> {
    val defaultLocale = resDirectory.resolve("resources.properties")
        .takeIf { it.isFile }
        ?.readLines()
        ?.asSequence()
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("unqualifiedResLocale=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: "en"

    val localizedLocales = resDirectory
        .listFiles { file -> file.isDirectory && file.name.startsWith("values-") }
        .orEmpty()
        .asSequence()
        .filter { it.resolve("strings.xml").isFile }
        .map { it.name.removePrefix("values-") }
        .filter { androidLocaleQualifierRegex.matches(it) }
        .sorted()
        .toList()

    return (listOf(defaultLocale) + localizedLocales).distinct()
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

android {
    namespace = packageName
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 5
        versionName = libs.versions.app.version.get()
    }
    flavorDimensions += "identity"
    productFlavors {
        create("standard") {
            dimension = "identity"
            resValue("string", "app_name", "Nowbar Meter")
        }
        create("compat") {
            dimension = "identity"
            applicationId = "com.kakao.taxi"
            resValue("string", "app_name", "Nowbar Meter Compat")
        }
    }
    packaging {
        dex {
            useLegacyPackaging = true
        }
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.version"
            excludes += "/META-INF/**/LICENSE.txt"
            excludes += "/kotlin/**"
            excludes += "DebugProbesKt.bin"
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        // Upstream translations are incomplete; Android falls back to English.
        warning += "MissingTranslation"
        checkReleaseBuilds = false
    }
    @Suppress("UnstableApiUsage")
    androidResources {
        generateLocaleConfig = true
        localeFilters.addAll(discoverLocaleFilters(layout.projectDirectory.dir("src/main/res").asFile))
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.core)

    // Feature Dependencies
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zhanghai.preference)
    implementation(libs.skydoves.colorpicker)
}
