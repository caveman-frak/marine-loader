plugins {
    id("marine.application-conventions")
}

dependencies {
    implementation("com.opencsv:opencsv:5.9") {
        exclude("commons-collections", "commons-collections")
    }
    implementation("org.apache.commons:commons-collections4:4.5.0-M2")
    implementation("dk.dma.enav:enav-serialization:0.6")
    implementation("dk.dma.ais.lib:ais-lib-utils:2.8.4") {
        exclude("com.beust", "jcommander")
    }
    implementation("org.jcommander:jcommander:1.83")
    implementation("org.locationtech.spatial4j:spatial4j:0.8")
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