package teksturepako.pakku.cli.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import teksturepako.pakku.api.data.PakkuLock
import teksturepako.pakku.api.platforms.Multiplatform
import teksturepako.pakku.api.projects.Project

class Update : CliktCommand("Update projects")
{
    private val projectArgs: List<String> by argument().multiple()
    private val allFlag: Boolean by option("-a", "--all", help = "Update all projects").flag()

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    override fun run() = runBlocking {
        val pakkuLock = PakkuLock.readToResult().fold(
            onSuccess = { it },
            onFailure = {
                terminal.danger(it.message)
                echo()
                return@runBlocking
            }
        )

        val oldProjects = if (allFlag)
        {
            pakkuLock.getAllProjects()
        } else
        {
            projectArgs.mapNotNull { projectArg ->
                pakkuLock.getProject(projectArg).also {
                    if (it == null)
                    {
                        terminal.danger("$projectArg not found")
                    }
                }
            }
        }

        val updatedProjects: ReceiveChannel<Project> = produce {
            val oldProjectIds = oldProjects.asSequence()
                .flatMap {
                    it.id.asSequence()
                }.groupBy({ it.key }, { it.value })

            val newProjects = Multiplatform.requestMultipleProjectsWithFiles(
                pakkuLock.getMcVersions(), pakkuLock.getLoaders(), oldProjectIds, numberOfFiles = 1
            )

            oldProjects.map { oldProject ->
                launch {
                    var updatedProject = oldProject.copy(files = mutableSetOf())

                    // Handle versions
                    val mcVersions = oldProject.files.flatMap { it.mcVersions }
                    val loaders = oldProject.files.flatMap { it.loaders }

                    newProjects.forEach { newProject ->
                        if (newProject isAlmostTheSameAs oldProject)
                        {
                            updatedProject += newProject

                            if (updatedProject != oldProject && mcVersions.isNotEmpty() && loaders.isNotEmpty())
                            {
                                send(updatedProject) // Send updated project to channel
                            }
                        }
                    }
                }
            }.joinAll() // Wait for all jobs to finish
            close() // Close channel

//            oldProjects.map { oldProject ->
//                launch {
//                    /** Create copy of each project with empty files */
//                    var updatedProject = oldProject.copy(files = mutableSetOf())
//
//                    val mcVersions = oldProject.files.flatMap { it.mcVersions }
//                    val loaders = oldProject.files.flatMap { it.loaders }
//
//                    /** For each platform request a new project with files and combine it with [updated project] */
//                    x@for (platform in Multiplatform.platforms)
//                    {
//                        val projectId = oldProject.id[platform.serialName] ?: continue@x
//
//                        platform.requestProjectWithFiles(mcVersions, loaders, projectId)
//                            ?.let { updatedProject += it } // Combine projects
//                    }
//
//                    /** If updated project is different from old project return updated project */
//                    if (updatedProject != oldProject && mcVersions.isNotEmpty() && loaders.isNotEmpty())
//                    {
//                        send(updatedProject) // Send updated project to channel
//                    }
//                }
//            }.joinAll() // Wait for all jobs to finish
//            close() // Close channel
        }

        var updated = false
        val jobs = mutableListOf<Job>()

        for (updatedProject in updatedProjects)
        {
            jobs += launch {
                pakkuLock.update(updatedProject)
                terminal.success("${updatedProject.slug} updated")

                updated = true
            }
        }
        jobs.joinAll()

        if (!updated && updatedProjects.isClosedForReceive && oldProjects.isNotEmpty())
        {
            when
            {
                allFlag || projectArgs.isEmpty() -> terminal.success("All projects are up to date")
                oldProjects.size == 1            -> terminal.success("${oldProjects.first().slug} is up to date")
                else                         ->
                {
                    terminal.success("${oldProjects.map { it.slug }} are up to date")
                }
            }
        }

        echo()
        pakkuLock.write()
    }
}