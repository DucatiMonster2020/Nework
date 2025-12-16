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
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Singleton
    @Binds
    fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
    @Singleton
    @Binds
    fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
    @Singleton
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Singleton
    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}