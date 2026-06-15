package com.template.app

import app.cash.turbine.test
import com.template.app.core.utils.Resource
import com.template.app.domain.model.User
import com.template.app.domain.repository.UserRepository
import com.template.app.domain.usecase.CreateUserUseCase
import com.template.app.domain.usecase.DeleteUserUseCase
import com.template.app.domain.usecase.GetUsersUseCase
import com.template.app.domain.usecase.UpdateUserUseCase
import com.template.app.presentation.viewmodel.UsersUiState
import com.template.app.presentation.viewmodel.UsersViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: UserRepository
    private lateinit var viewModel: UsersViewModel

    private val fakeUsers = listOf(
        User("1", "Alice", "alice@example.com", null),
        User("2", "Bob", "bob@example.com", null)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk {
            coEvery { observeUsers() } returns flowOf(fakeUsers)
            coEvery { fetchUsers() } returns Resource.Success(fakeUsers)
        }
        viewModel = UsersViewModel(GetUsersUseCase(repository), CreateUserUseCase(repository),
            UpdateUserUseCase(repository), DeleteUserUseCase(repository), repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers emits Success state with correct data`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is UsersUiState.Success)
            assertEquals(fakeUsers, (state as UsersUiState.Success).users)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadUsers emits Error state on network failure`() = runTest {
        coEvery { repository.fetchUsers() } returns Resource.Error("Network error")
        viewModel.loadUsers()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is UsersUiState.Error)
            assertEquals("Network error", (state as UsersUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
