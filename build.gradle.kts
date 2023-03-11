plugins {
    id("marine.java-conventions")
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":wire"))
    testImplementation(project(":test"))
}
