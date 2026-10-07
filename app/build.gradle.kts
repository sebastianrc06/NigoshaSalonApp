import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties().apply {
    val archivo = rootProject.layout.projectDirectory.file("local.properties").asFile

    if (archivo.exists()) {
        archivo.inputStream().use {
            load(it)
        }
    }
}

val mapsApiKey = localProperties
    .getProperty("MAPS_API_KEY", "")
    .trim()

val apisPeruToken = localProperties
    .getProperty("APISPERU_TOKEN", "")
    .trim()

android {

    namespace = "pe.uch.nigosha"

    compileSdk {
        version = release(37)
    }

    defaultConfig {

        applicationId = "pe.uch.nigosha"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        buildConfigField(
            "String",
            "APISPERU_TOKEN",
            "\"${apisPeruToken.replace("\"", "\\\"")}\""
        )
    }

    buildFeatures {
        buildConfig = true
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

    // Android
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Firebase
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:20.0.0")

    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.9.4")

    // Retrofit - APIs Perú
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}