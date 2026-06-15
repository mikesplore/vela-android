package com.template.app.domain.usecase

import com.template.app.core.utils.Resource
import com.template.app.domain.model.User
import com.template.app.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Resource<List<User>> = repository.fetchUsers()
}

class GetUserByIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Resource<User> = repository.getUserById(id)
}

class CreateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String): Resource<User> = 
        repository.createUser(name, email)
}

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String, name: String, email: String): Resource<User> = 
        repository.updateUser(id, name, email)
}

class DeleteUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> = repository.deleteUser(id)
}
