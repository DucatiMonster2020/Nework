package ru.netology.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repository.AuthRepository
import ru.netology.nework.repository.AuthRepositoryImpl
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.EventRepositoryImpl
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.PostRepositoryImpl
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.repository.UserRepositoryImpl

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}