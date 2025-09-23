// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    kotlin("android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}


