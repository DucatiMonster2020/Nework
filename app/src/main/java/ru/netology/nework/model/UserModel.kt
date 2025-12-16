package ru.netology.nework.model

import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.AppError

data class UserModel(
    val users: List<User> = emptyList(),
    val loading: Boolean = false,
    val error: AppError? = null,
    val refreshing: Boolean = false,
    val searchQuery: String = ""
) {
    val isLoading: Boolean get() = loading
    val isRefreshing: Boolean get() = refreshing
}
data class ProfileModel(
    val user: UserResponse? = null,
    val loading: Boolean = false,
    val error: AppError? = null,
    val jobs: List<Job> = emptyList(),
    val posts: List<Post> = emptyList(),
    val isMyProfile: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.WALL
)
enum class ProfileTab {
    WALL, JOBS
}
data class JobModel(
    val job: Job? = null,
    val loading: Boolean = false,
    val error: AppError? = null,
    val isEditing: Boolean = false
)