plugins {
    id("marine.application-conventions")
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":wire"))
    testImplementation(project(":test"))
    implementation("com.opencsv:opencsv:5.7.1")
}

application {
    mainClass.set("uk.co.bluegecko.marine.loader.LoaderApplication")
}