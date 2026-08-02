import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object AppVersion {

    fun getVersionCode(): Int {
        return try {
            val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
            process.waitFor()
            process.inputStream.bufferedReader().readText().trim().toInt()
        } catch (e: Exception) {
            1
        }
    }

    fun getVersionName(): String {
        return try {
            val tagProcess = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
            tagProcess.waitFor()
            val tag = tagProcess.inputStream.bufferedReader().readText().trim()

            val countProcess = Runtime.getRuntime()
                .exec("git rev-list --count $tag..HEAD")
            countProcess.waitFor()
            val commitsSinceTag = countProcess.inputStream
                .bufferedReader().readText().trim().toIntOrNull() ?: 0

            if (tag.isNotEmpty()) "$tag.$commitsSinceTag"
            else "1.0.${getVersionCode()}"
        } catch (e: Exception) {
            "1.0.${getVersionCode()}"
        }
    }

    fun getBuildDate(): String {
        return SimpleDateFormat("yyyyMMdd").format(Date())
    }

    fun getAabFileName(
        flavorName: String,
        buildType: String
    ): String {
        val versionName = getVersionName()
        val versionCode = getVersionCode()
        val date = getBuildDate()
        return "pitlane-$flavorName-$buildType-v$versionName($versionCode)-$date.aab"
    }

    fun renameAabOutput(
        outputDir: File,
        flavorName: String,
        buildType: String
    ) {
        outputDir.listFiles()?.forEach { file ->
            if (file.extension == "aab") {
                val newName = getAabFileName(flavorName, buildType)
                file.renameTo(File(outputDir, newName))
            }
        }
    }
}