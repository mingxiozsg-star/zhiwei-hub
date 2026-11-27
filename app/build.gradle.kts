plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// 本地 proot(aarch64) 需要 ARM64 版 aapt2；x86_64 的 CI 使用官方二进制
val isArm64Host = System.getProperty("os.arch")?.lowercase()?.let {
    it.contains("aarch64") || it.contains("arm64")
} ?: false

android {
    namespace = "com.zhiwei.hub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zhiwei.hub"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.6"
        vectorDrawables { useSupportLibrary = true }
    }

    // 固定签名：keystore/zhiwei-hub.jks 随仓库提供，任何机器与 CI 出包都能互相覆盖安装
    val zhiweiKs = rootProject.file("keystore/zhiwei-hub.jks")
    val hasZhiweiKs = zhiweiKs.exists()
    signingConfigs {
        if (hasZhiweiKs) {
            create("zhiwei") {
                storeFile = zhiweiKs
                storePassword = "zhiwei123"
                keyAlias = "zhiwei"
                keyPassword = "zhiwei123"
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasZhiweiKs) {
                signingConfigs.getByName("zhiwei")
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
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

// 本地 Proot 环境强制使用 ARM64 aapt2
if (isArm64Host) {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.android.tools.build" && requested.name == "aapt2") {
                useTarget("com.android.tools.build:aapt2:${requested.version}:linux-aarch64")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
}
