import org.example.Repo

data class User(
    val login: String,
    val followers: Int = 0,
    val following: Int = 0,
    val createdAt: String? = null,
    val publicRepos: Int = 0,
    var repos: List<Repo> = emptyList()
) {
    constructor(
        login: String,
        followers: Int?,
        following: Int?,
        createdAt: String?,
        publicRepos: Int?,
        repos: List<Repo>?
    ) : this(
        login = login,
        followers = followers ?: 0,
        following = following ?: 0,
        createdAt = createdAt,
        publicRepos = publicRepos ?: 0,
        repos = repos ?: emptyList()
    )
}