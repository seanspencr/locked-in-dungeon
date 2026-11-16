import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.Properties



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")
    id("org.openapi.generator") version "7.27.0"
}



val properties : Properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())


val apiBaseUrl = properties.getProperty("api.baseUrl")
android {
    namespace = "com.example.lockedindungeon"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.lockedindungeon"
        minSdk = 35
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "apiBaseUrl",  "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            isDebuggable = true
            optimization {
                enable = false
            }
        }

        debug {
            isDebuggable = true
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


//    ini supaya dia append project yang generated dari openapi (http client) ke project kita supaya bisa diimport
}

androidComponents {
    onVariants { variant ->
        val openApiTask = tasks.named<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate")

        variant.sources.java?.addGeneratedSourceDirectory(
            openApiTask,
            GenerateTask::outputDir
        )

        // keeps the openApiGenerate task dependency from above, but the kotlin sources of the
        // generated client live in <outputDir>/src/main/kotlin, so that folder has to be a source
        // root too, otherwise api/* and dto/* never reach the compiler
        variant.sources.java?.addStaticSourceDirectory(
            "${layout.buildDirectory.get()}/generated/openapi/src/main/kotlin"
        )
    }
}

openApiGenerate{
    generatorName.set("kotlin")
    inputSpec.set("$apiBaseUrl/v3/api-docs") // Path to your Swagger spec file
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("com.example.lockedindungeon.api")
    modelPackage.set("com.example.lockedindungeon.dto")

    generateApiDocumentation.set(false)
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateModelDocumentation.set(false)

    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "library" to "jvm-retrofit2", // Uses Retrofit 2 + Moshi/Gson under the hood
        "serializationLibrary" to "kotlinx_serialization", // or "moshi", "gson"
    ))

    globalProperties.set(mapOf(
        "apiTests" to "false",
        "modelTests" to "false",
        "apiDocs" to "false",
        "modelDocs" to "false"
    ))
}
//ini supaya openapigenerate ndak ngecachhe
tasks.named("openApiGenerate") {
    outputs.upToDateWhen { false }
}

dependencies {
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

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Lifecycles only (without ViewModel or LiveData)
    // Lifecycle utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Saved state module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation(libs.androidx.lifecycle.common.java8)

    // optional - helpers for implementing LifecycleOwner in a Service
    implementation(libs.androidx.lifecycle.service)

    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    implementation(libs.androidx.lifecycle.process)

    implementation(platform(libs.firebase.bom))
//    ini buat firebase messaageing
    implementation(libs.firebase.messaging)





    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Feature module support for Fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material.icons.extended)

//    google font
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.work.runtime.ktx)

    // Source: https://mvnrepository.com/artifact/androidx.hilt/hilt-work
    implementation(libs.androidx.hilt.work)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.lottiefiles.dotlottie.android)
    // generates the WorkerAssistedFactory bindings consumed by HiltWorkerFactory (@HiltWorker)
    ksp(libs.androidx.hilt.compiler)

    // runtime for the OpenAPI generated client (api/*, dto/*)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.okhttp.logging.interceptor)


//    unit testnya openapi
    testImplementation(libs.kotlintest.runner.junit5)
}