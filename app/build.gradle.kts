import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

// Lee la clave desde local.properties.
val mapsApiKey = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties")
).asText.map { contenido ->
    val propiedades = Properties()
    propiedades.load(contenido.reader())
    propiedades.getProperty("MAPS_API_KEY", "").trim()
}.getOrElse("")
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Disponible como ${MAPS_API_KEY} en AndroidManifest.xml.
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}