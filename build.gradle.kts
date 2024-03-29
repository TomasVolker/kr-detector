import org.gradle.internal.os.OperatingSystem

plugins {
    application
    kotlin("jvm") version "1.3.21"
}

group = "tomasvolker"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/openrndr/openrndr/") }
    maven { url = uri("http://dl.bintray.com/tomasvolker/maven") }
    maven { url = uri("https://jitpack.io") }
}

val openrndrVersion = "0.3.30"

val openrndrOS = when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.LINUX -> "linux-x64"
    OperatingSystem.MAC_OS -> "macos"
    else -> error("unsupported OS")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")

    implementation("com.github.TomasVolker:openrndr-math:d56f089253")

    compile("org.openrndr:openrndr-core:$openrndrVersion")
    compile("org.openrndr:openrndr-extensions:$openrndrVersion")
    compile("org.openrndr:openrndr-ffmpeg:$openrndrVersion")

    runtime("org.openrndr:openrndr-gl3:$openrndrVersion")
    runtime("org.openrndr:openrndr-gl3-natives-$openrndrOS:$openrndrVersion")

    compile(group = "com.github.tomasvolker", name = "parallel-utils", version = "v1.0")
    compile(group = "tomasvolker", name = "numeriko-core", version = "0.0.3")
    
    listOf("boofcv-core","boofcv-swing","boofcv-WebcamCapture","demonstrations").forEach { a ->
        compile(group = "org.boofcv", name = a, version = "0.32")
    }
    
}


val mainClass = "tomasvolker.kr.demo.DemoKt"

application {

    mainClassName = mainClass

    if (openrndrOS == "macos")
        applicationDefaultJvmArgs += "-XstartOnFirstThread"

}