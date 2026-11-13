plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.interview.assessment"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.interview.assessment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // 角色列表
    implementation(libs.androidx.recyclerview)

    // ViewModel 和 生命周期
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // 网络请求 和 JSON解析
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // 网络图片加载（Coil 2 与当前项目的 Kotlin 版本兼容）
    implementation(libs.coil)

    testImplementation(libs.junit)
    // ViewModel 协程与 StateFlow 的本地单元测试工具
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
