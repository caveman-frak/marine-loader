plugins {
    id("marine.application-conventions")
}

dependencies {
    implementation(libs.beanio)
    implementation(libs.enav)
    implementation(libs.ais.utils) {
        exclude("com.beust", "jcommander")
    }
    implementation(libs.jcommander)
    implementation(libs.bundles.spatial)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
            }
        }
    }
}

application {
    mainClass.set("uk.co.bluegecko.marine.loader.LoaderApplication")
}