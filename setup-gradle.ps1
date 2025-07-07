$gradleWrapperUrl = "https://services.gradle.org/distributions/gradle-8.13-bin.zip"
$gradleWrapperJar = "https://github.com/gradle/gradle/raw/v8.13.0/gradle/wrapper/gradle-wrapper.jar"
$wrapperDir = "gradle\wrapper"

Write-Host "Downloading Gradle wrapper jar..."
try {
    Invoke-WebRequest -Uri $gradleWrapperJar -OutFile "$wrapperDir\gradle-wrapper.jar" -UseBasicParsing
    Write-Host "✅ Gradle wrapper downloaded successfully!"
    Write-Host "Now you can run: .\gradlew.bat assembleDebug"
} catch {
    Write-Host "❌ Failed to download Gradle wrapper. Please open the project in Android Studio instead."
    Write-Host "Android Studio will automatically set up Gradle for you."
}
