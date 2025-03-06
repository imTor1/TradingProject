plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.tradingproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tradingproject"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    //  Google Sign-In API สำหรับการล็อกอินด้วย Google
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    //  Retrofit สำหรับการเรียก API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // ✅ ใช้ Gson แปลง JSON เป็น Object
    //  OkHttp สำหรับจัดการ HTTP Requests
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // ✅ ใช้สำหรับ Log HTTP Requests
    //  Coroutines สำหรับทำงานแบบ Asynchronous
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    //  Navigation Component สำหรับจัดการ Fragment และ Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    //  MPAndroidChart ใช้สร้างกราฟข้อมูล
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //  Material Design Components สำหรับ UI
    implementation("com.google.android.material:material:1.6.1")
    //  RecyclerView Swipe Decorator สำหรับ Swipe ลบ/แก้ไข รายการใน RecyclerView
    implementation("com.github.xabaras:RecyclerViewSwipeDecorator:1.4")
    //  Biometric API สำหรับใช้สแกนนิ้วหรือใบหน้า
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    //  Glide สำหรับโหลดและแสดงรูปภาพ
    implementation("com.github.bumptech.glide:glide:4.12.0")
    //  Activity KTX สำหรับจัดการ Activity lifecycle
    implementation("androidx.activity:activity-ktx:1.3.1")
    //  AndroidX Core และ AppCompat สำหรับการใช้งานฟีเจอร์ใหม่ๆ
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    //  ConstraintLayout สำหรับจัดการ Layout UI
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.preference)
    //  การทดสอบ (Unit Test และ UI Test)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
