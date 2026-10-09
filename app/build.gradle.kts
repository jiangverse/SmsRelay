plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.jiangverse.smsrelay"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "io.github.jiangverse.smsrelay"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // 保留发布包精简；Hook 入口和反射依赖由 hook/consumer-rules.pro 保留。
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // CI 配置签名密钥后生成可安装的发布包；未配置时保留未签名构建。
    val releaseKeystore = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
    if (!releaseKeystore.isNullOrBlank()) {
        signingConfigs.create("ciRelease") {
            storeFile = file(releaseKeystore)
            storePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").get()
        }
        buildTypes.getByName("release").signingConfig = signingConfigs.getByName("ciRelease")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// 将唯一发布 APK 导出为项目名称和版本号，供本地构建和 CI 共用。
tasks.register<Copy>("exportReleaseApk") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) { include("*.apk") }
    into(layout.buildDirectory.dir("outputs/distribution"))
    rename { "SmsRelay-${android.defaultConfig.versionName}.apk" }
}

dependencies {
    implementation(project(":hook"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
