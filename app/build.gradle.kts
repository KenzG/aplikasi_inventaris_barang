plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt") // Ingat, ini mungkin tidak diperlukan jika tidak ada library lain yang butuh
    id("com.google.gms.google-services") // Google Services di akhir
    id("kotlin-parcelize")
}

android {
    namespace = "edu.praktis.org.newtestapp"
    compileSdk = 35 // Pertimbangkan untuk menggunakan 34 jika 35 masih preview

    defaultConfig {
        applicationId = "edu.praktis.org.newtestapp"
        minSdk = 24
        targetSdk = 35 // Pertimbangkan untuk menggunakan 34 jika 35 masih preview
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Untuk RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0") // Atau versi lebih baru
    // Untuk CardView
    implementation("androidx.cardview:cardview:1.0.0") // Atau versi lebih baru

    //Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0")) // Pastikan ini versi terbaru yang stabil

    // Dependensi Firebase
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
}
