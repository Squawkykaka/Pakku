@file:Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")

package teksturepako.pakku.api.actions.errors

import teksturepako.pakku.api.data.LockFile
import teksturepako.pakku.api.platforms.Provider
import teksturepako.pakku.api.projects.Project
import teksturepako.pakku.cli.ui.dim
import teksturepako.pakku.cli.ui.getFlavoredSlug
import java.nio.file.Path

data class DirectoryNotEmpty(val file: String) : ActionError()
{
    override val rawMessage = "Directory '$file' is not empty."
}

data class FileNotFound(val file: String) : ActionError()
{
    override val rawMessage = "File '$file' not found."
}

data class CouldNotRead(val file: String, val reason: String? = "") : ActionError()
{
    override val rawMessage = "Could not read: '$file'. $reason"
}

data class ErrorWhileReading(val file: String, val reason: String? = "") : ActionError()
{
    override val rawMessage = "Error occurred while reading: '$file'. $reason"
}

class AlreadyExists(val file: String) : ActionError()
{
    override val rawMessage = "File '$file' already exists."
}

// -- PROJECT FILE --

class DownloadFailed(val path: Path?, val retryNumber: Int = 0) : ActionError()
{
    override val rawMessage = "Failed to download '$path'. ${if (retryNumber > 0) "Retry number $retryNumber." else ""}"
}

class NoHashes(val path: Path?) : ActionError()
{
    override val rawMessage = "File '$path' has no hashes."
    override val isWarning = true
}

class HashMismatch(val path: Path?, val originalHash: String, val newHash: String) : ActionError()
{
    override val rawMessage = """Failed to math hash for file '$path'.
            | Original hash: $originalHash
            | New hash: $newHash
            """.trimMargin()
}

class CouldNotSave(val path: Path?, val reason: String? = "") : ActionError()
{
    override val rawMessage = if (path != null) "Could not save: '$path'. $reason" else "Could not save file. $reason"
}

// -- IMPORT --

class CouldNotImport(val file: String) : ActionError()
{
    override val rawMessage = "Could not import from: '$file'. It is not a proper modpack file."
}

// -- PROJECT --

class ProjNotFound : ActionError()
{
    override val rawMessage = "Project not found."
}

class ProjDiffTypes(val project: Project, val otherProject: Project) : ActionError(
)
{
    override val rawMessage = """Can not combine two projects of different types:
            | ${project.slug} ${project.type} + ${otherProject.slug} ${otherProject.type}
            """.trimMargin()

    override fun message(arg: String): String = """Can not combine two projects of different types:
            | ${project.getFlavoredSlug()} ${dim(project.type)} + ${otherProject.getFlavoredSlug()} ${dim(otherProject.type)}
            """.trimMargin()
}

class ProjDiffPLinks(val project: Project, val otherProject: Project) : ActionError(
)
{
    override val rawMessage = """Can not combine two projects with different pakku links:
            | ${project.slug} ${project.type} + ${otherProject.slug} ${otherProject.type}
            """.trimMargin()

    override fun message(arg: String): String = """Can not combine two projects with different pakku links:
                | ${project.getFlavoredSlug()} ${dim(project.type)} +
                | ${otherProject.getFlavoredSlug()} ${dim(otherProject.type)}
                """.trimMargin()
}

// -- EXPORT --

class NotRedistributable(val project: Project) : ActionError()
{
    override val rawMessage = "${project.type} ${project.slug} can not be exported, because it is not redistributable."

    override fun message(arg: String): String =
        "${dim(project.type)} ${project.getFlavoredSlug()} can not be exported, because it is not redistributable."
}

// -- ADDITION --

class AlreadyAdded(val project: Project) : ActionError()
{
    override val rawMessage = "${project.type} ${project.slug} is already added."

    override fun message(arg: String): String = "${dim(project.type)} ${project.getFlavoredSlug()} is already added."
}

class NotFoundOn(val project: Project, val provider: Provider) : ActionError()
{
    override val rawMessage = "${project.type} ${project.slug} was not found on ${provider.name}"

    override fun message(arg: String): String =
        "${dim(project.type)} ${project.getFlavoredSlug()} was not found on ${provider.name}."
}

class NoFilesOn(val project: Project, val provider: Provider) : ActionError()
{
    override val rawMessage = "No files for ${project.type} ${project.slug} found on ${provider.name}."

    override fun message(arg: String) =
        "No files for ${dim(project.type)} ${project.getFlavoredSlug()} found on ${provider.name}."
}

class NoFiles(val project: Project, val lockFile: LockFile) : ActionError(

)
{
    override val rawMessage = """No files found for ${project.type} ${project.slug}.
            | Your modpack requires Minecraft versions: ${lockFile.getMcVersions()} and loaders: ${lockFile.getLoaders()}.
            | Make sure the project complies these requirements.
            """.trimMargin()

    override fun message(arg: String) = """No files found for ${dim(project.type)} ${project.getFlavoredSlug()}.
            | Your modpack requires Minecraft versions: ${lockFile.getMcVersions()} and loaders: ${lockFile.getLoaders()}.
            | Make sure the project complies these requirements.
            """.trimMargin()
}

class FileNamesDoNotMatch(val project: Project) : ActionError(

)
{
    override val rawMessage = """${project.type} ${project.slug} versions do not match across platforms.
            | ${project.files.map { "${it.type}: ${it.fileName}" }}
            """.trimMargin()

    override fun message(arg: String) =
        """${dim(project.type)} ${project.getFlavoredSlug()} versions do not match across platforms.
                | ${project.files.map { "${it.type}: ${it.fileName}" }}
                """.trimMargin()
}

// -- REMOVAL --

class ProjRequiredBy(val project: Project, val dependants: List<Project>) : ActionError()
{
    override val rawMessage = "${project.type} ${project.slug} is required by ${dependants.map { it.slug }}"
    override val isWarning = true
    override fun message(arg: String) =
        "${dim(project.type)} ${project.getFlavoredSlug()} is required by " +
                "${dependants.map { it.getFlavoredSlug() }}."
}
