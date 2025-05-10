import org.gradle.api.tasks.Delete

plugins {
    id("com.android.application") version "7.4.2" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}



tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
