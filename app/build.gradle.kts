import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

val secretsPropsFile = rootProject.file("secrets.properties")
val secretsProps = Properties()
if (secretsPropsFile.exists()) {
    // Usar inputStream() en lugar de java.io.FileInputStream para Kotlin DSL
    secretsProps.load(secretsPropsFile.inputStream())
}

android {
    namespace = "com.example.saborforaneo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.saborforaneo"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Exponer la URL del backend como BuildConfig (se lee desde secrets.properties si existe)
        val geminiUrl = secretsProps.getProperty("GEMINI_BASE_URL") ?: "https://chatagent-saborforaneofork-production.up.railway.app/"
        buildConfigField("String", "GEMINI_BASE_URL", "\"$geminiUrl\"")
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
        compose = true
        // Habilitar BuildConfig para poder usar buildConfigField
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.3")

    // Coil para carga de imágenes
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // DataStore (para preferencias)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")
    
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage")
    
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    
    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging")
    
    // WorkManager para notificaciones periódicas
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Retrofit para API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Accompanist (para funcionalidades adicionales)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // ============================================
    // DEPENDENCIAS PARA PRUEBAS UNITARIAS
    // ============================================
    // Mockito para crear mocks de dependencias
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    // Coroutines Test para probar código asíncrono
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Kotlin Test para assertions más expresivas
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //otras dependencias
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
}
