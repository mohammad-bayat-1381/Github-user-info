package org.example

import User
import java.util.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

suspend fun main() {

    CacheManager.loadUsersFromFile()
    CacheManager.getAllUsers().forEach { user ->
        if (user.repos == null) {
            CacheManager.addUser(user, emptyList())
        }
    }

    val retrofit =
        Retrofit.Builder().baseUrl("https://api.github.com/").addConverterFactory(GsonConverterFactory.create()).build()

    val service = retrofit.create(GitHubService::class.java)
    val scanner = Scanner(System.`in`)

    while (true) {
        printMenu()
        when (scanner.nextInt()) {
            1 -> fetchUser(service, scanner)
            2 -> listUsers()
            3 -> searchUser(scanner)
            4 -> searchRepo(scanner)
            5 -> return
        }
    }
}

private suspend fun fetchUser(service: GitHubService, scanner: Scanner) {
    try {
        print("Enter username: ")
        val username = scanner.next()
        val userResponse = service.getUser(username)
        val repos = service.getRepos(username)

        val user = User(
            login = userResponse.login,
            followers = userResponse.followers,
            following = userResponse.following,
            createdAt = userResponse.createdAt,
            publicRepos = userResponse.publicRepos,
            repos = repos
        )

        CacheManager.addUser(user, repos)
    } catch (e: Exception) {
        println("Error: ${e.message?.take(100)}...")
    }
}

private suspend fun <T> handleApiCall(block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: HttpException) {
        println("HTTP Error: ${e.code()} - ${e.message()}")
        null
    } catch (e: IOException) {
        println("Network Error: ${e.message}")
        null
    } catch (e: Exception) {
        println("Unexpected Error: ${e.message}")
        null
    }
}

private fun printMenu() {
    println(
        """
        1️⃣ Get user by username
        2️⃣ List cached users
        3️⃣ Search by username
        4️⃣ Search by repository
        5️⃣ Exit
    """.trimIndent()
    )
}

private fun searchUser(scanner: Scanner) {
    print("Enter username to search: ")
    val query = scanner.next()
    val results = CacheManager.searchByUsername(query)

    if (results.isEmpty()) {
        println("No users found matching '$query'")
        return
    }

    println("\nMatching users:")
    results.forEach { user ->
        println(
            """
            Username: ${user.login}
            Followers: ${user.followers}
            Following: ${user.following}
            Created at: ${user.createdAt}
            --------------------------
        """.trimIndent()
        )
    }
}

private fun searchRepo(scanner: Scanner) {
    print("Enter repository name to search: ")
    val repoName = scanner.next()
    val results = CacheManager.searchByRepo(repoName)

    if (results.isEmpty()) {
        println("No repositories found matching '$repoName'")
        return
    }

    println("\nUsers with matching repositories:")
    results.forEach { (user, repos) ->
        println(
            """
            User: ${user.login}
            Matching repositories:
            ${
                repos.joinToString("\n") {
                    "  - ${it.name}: ${it.description ?: "No description"} (${it.url})"
                }
            }
            """.trimIndent())
    }
}

private fun listUsers() {
    val users = CacheManager.getAllUsers()

    if (users.isEmpty()) {
        println("No users in cache!")
        return
    }

    println("\nCached users:")
    users.forEach { user ->
        println(
            """
            Username: ${user.login}
            Followers: ${user.followers}
            Following: ${user.following}
            Public repos: ${user.publicRepos}
            Created at: ${user.createdAt}
            --------------------------
        """.trimIndent()
        )
    }
}