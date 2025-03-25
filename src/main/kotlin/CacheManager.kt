package org.example

import User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object CacheManager {
    private val users = mutableListOf<User>()
    private val cacheFile = File("user_cache.json")
    private val gson = Gson()

    fun addUser(user: User, repos: List<Repo>) {
        if (users.contains(user)) {
            println("User exists!")
            return
        }
        val safeUser = User(
            login = user.login,
            followers = user.followers,
            following = user.following,
            createdAt = user.createdAt,
            publicRepos = user.publicRepos,
            repos = repos
        )
        users.add(safeUser)
        saveUsersToFile()
        println("User ${user.login} added to cache")
    }

    fun getAllUsers(): List<User> {
        return users.toList()
    }

    fun searchByUsername(query: String): List<User> {
        return users.filter { it.login.contains(query, ignoreCase = true) }
    }

    fun searchByRepo(repoName: String): List<Pair<User, List<Repo>>> {
        return users.mapNotNull { user ->
            val repos = user.repos ?: emptyList() // Additional null check
            val matchingRepos = repos.filter { repo ->
                repo.name.contains(repoName, ignoreCase = true) ||
                        (repo.description?.contains(repoName, ignoreCase = true) ?: false)
            }
            if (matchingRepos.isNotEmpty()) user to matchingRepos else null
        }
    }

    private fun saveUsersToFile() {
        val json = gson.toJson(users)
        cacheFile.writeText(json)
    }

    fun loadUsersFromFile() {
        if (cacheFile.exists() && cacheFile.length() > 0) {
            val json = cacheFile.readText()
            val type = object : TypeToken<List<User>>() {}.type
            users.clear()
            users.addAll(gson.fromJson(json, type))
        } else {
            println("No users in cache")
            users.clear() // Ensure the list is empty
            return
        }
    }

    fun clearCache() {
        users.clear()
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }
}