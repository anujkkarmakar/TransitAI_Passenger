plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.navixpassanger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.navixpassanger"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.md"
            )
        }
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.location)
    implementation(libs.gridlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    add("androidTestImplementation", libs.ext.junit)
    add("androidTestImplementation", libs.espresso.core)

    implementation (libs.circleimageview)
    implementation (libs.picasso)

    implementation (libs.lottie)
    implementation (libs.sdp.android)

    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation (libs.volley)

    implementation(libs.imagepicker)

    implementation(libs.android.mail)
    implementation(libs.android.activation)
    implementation(libs.itext7.core)

    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

    implementation(libs.play.services.maps.v1810)
    implementation(libs.play.services.location.v2101)
}