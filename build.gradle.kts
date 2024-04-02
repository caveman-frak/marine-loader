plugins {
    id("marine.application-conventions")
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":wire"))
    testImplementation(project(":test"))
    implementation("com.opencsv:opencsv:5.8")
    implementation("dk.dma.enav:enav-serialization:0.6")
    implementation("dk.dma.ais.lib:ais-lib-utils:2.8.2")
    implementation("org.locationtech.spatial4j:spatial4j:0.8")
}

application {
    mainClass.set("uk.co.bluegecko.marine.loader.LoaderApplication")
}