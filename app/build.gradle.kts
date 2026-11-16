import java.util.zip.ZipInputStream

// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.mikepenz.aboutlibraries.plugin") version "12.2.4"
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
        versionCode = 6
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
    testImplementation("junit:junit:4.13.2")
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

// Build-time metadata only: no AboutLibraries runtime or network code in the APK.
aboutLibraries {
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
}

// Retain actual notices shipped in resolved AARs/JARs, including nested classes.jar.
val noticeAssets = layout.buildDirectory.dir("generated/noticeAssets")
val collectThirdPartyNotices by tasks.registering {
    val artifacts = configurations.named("standardReleaseRuntimeClasspath")
    inputs.files(artifacts)
    outputs.dir(noticeAssets)
    doLast {
        val notices = sortedSetOf<String>()
        fun collectZip(bytes: ByteArray, source: String, nested: Boolean = false) {
            ZipInputStream(bytes.inputStream()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name.substringAfterLast('/').uppercase()
                    if (!entry.isDirectory && (name.startsWith("LICENSE") || name.startsWith("NOTICE") || name in listOf("AL2.0", "LGPL2.1", "COPYING", "COPYRIGHT"))) {
                        notices.add("$source / ${entry.name}\n\n" + zip.readBytes().toString(Charsets.UTF_8))
                    } else if (!nested && entry.name == "classes.jar") {
                        collectZip(zip.readBytes(), "$source / classes.jar", true)
                    }
                }
            }
        }
        artifacts.get().resolvedConfiguration.resolvedArtifacts.sortedBy { it.moduleVersion.id.toString() }.forEach {
            if (it.file.extension in listOf("aar", "jar")) collectZip(it.file.readBytes(), it.moduleVersion.id.toString())
        }
        val output = noticeAssets.get().file("THIRD_PARTY_NOTICES.txt").asFile
        output.parentFile.mkdirs()
        output.writeText(notices.joinToString("\n\n----------------------------------------\n\n") + "\n")
    }
}
android.sourceSets.getByName("main").assets.srcDir(noticeAssets)
tasks.named("preBuild") { dependsOn(collectThirdPartyNotices) }
