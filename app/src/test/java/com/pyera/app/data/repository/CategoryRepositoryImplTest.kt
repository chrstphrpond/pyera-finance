package com.pyera.app.data.repository

import app.cash.turbine.test
import com.pyera.app.data.local.dao.CategoryDao
import com.pyera.app.data.local.entity.CategoryEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CategoryRepositoryImplTest {

    @MockK
    private lateinit var categoryDao: CategoryDao

    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        repository = CategoryRepositoryImpl(categoryDao)
    }

    // ==================== getAllCategories Tests ====================

    @Test
    fun `getAllCategories returns flow from dao`() = runTest {
        // Arrange
        val categories = listOf(
            CategoryEntity(
                id = 1,
                name = "Food",
                icon = "🍔",
                color = 0xFF5733,
                type = "EXPENSE"
            ),
            CategoryEntity(
                id = 2,
                name = "Salary",
                icon = "💰",
                color = 0x33FF57,
                type = "INCOME"
            ),
            CategoryEntity(
                id = 3,
                name = "Transport",
                icon = "🚗",
                color = 0x3357FF,
                type = "EXPENSE"
            )
        )
        coEvery { categoryDao.getAllCategories() } returns flowOf(categories)

        // Act & Assert
        repository.getAllCategories().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Food", result[0].name)
            assertEquals("EXPENSE", result[0].type)
            assertEquals("Salary", result[1].name)
            assertEquals("INCOME", result[1].type)
            awaitComplete()
        }
    }

    @Test
    fun `getAllCategories emits empty list when no categories`() = runTest {
        // Arrange
        coEvery { categoryDao.getAllCategories() } returns flowOf(emptyList())

        // Act & Assert
        repository.getAllCategories().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getAllCategories emits updates when data changes`() = runTest {
        // Arrange
        val initialCategories = listOf(
            CategoryEntity(id = 1, name = "Food", icon = "🍔", color = 0xFF5733, type = "EXPENSE")
        )
        val updatedCategories = listOf(
            CategoryEntity(id = 1, name = "Food", icon = "🍔", color = 0xFF5733, type = "EXPENSE"),
            CategoryEntity(id = 2, name = "Transport", icon = "🚗", color = 0x3357FF, type = "EXPENSE")
        )
        coEvery { categoryDao.getAllCategories() } returns flowOf(initialCategories, updatedCategories)

        // Act & Assert
        repository.getAllCategories().test {
            val first = awaitItem()
            assertEquals(1, first.size)
            
            val second = awaitItem()
            assertEquals(2, second.size)
            
            awaitComplete()
        }
    }

    // ==================== insertCategory Tests ====================

    @Test
    fun `insertCategory calls dao insert`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 0,
            name = "New Category",
            icon = "🎯",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(category) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(category) }
    }

    @Test
    fun `insertCategory with auto-generated id`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 0, // Will be auto-generated
            name = "Entertainment",
            icon = "🎬",
            color = 0x9933FF,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.id == 0 && it.name == "Entertainment" }) }
    }

    @Test
    fun `insertCategory propagates exception`() = runTest {
        // Arrange
        val category = CategoryEntity(
            name = "Test",
            icon = "🎯",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } throws RuntimeException("Insert failed")

        // Act & Assert
        try {
            repository.insertCategory(category)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Insert failed", e.message)
        }
    }

    @Test
    fun `insertCategory with INCOME type`() = runTest {
        // Arrange
        val category = CategoryEntity(
            name = "Freelance",
            icon = "💻",
            color = 0x33FF57,
            type = "INCOME"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.type == "INCOME" }) }
    }

    @Test
    fun `insertCategory with EXPENSE type`() = runTest {
        // Arrange
        val category = CategoryEntity(
            name = "Bills",
            icon = "💡",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.type == "EXPENSE" }) }
    }

    // ==================== deleteCategory Tests ====================

    @Test
    fun `deleteCategory calls dao delete`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 1,
            name = "To Delete",
            icon = "🗑️",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.deleteCategory(category) } just Runs

        // Act
        repository.deleteCategory(category)

        // Assert
        coVerify { categoryDao.deleteCategory(category) }
    }

    @Test
    fun `deleteCategory propagates exception`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 1,
            name = "Test",
            icon = "🎯",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.deleteCategory(any()) } throws RuntimeException("Delete failed")

        // Act & Assert
        try {
            repository.deleteCategory(category)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `deleteCategory with existing id`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 5,
            name = "Old Category",
            icon = "📦",
            color = 0x999999,
            type = "EXPENSE"
        )
        coEvery { categoryDao.deleteCategory(any()) } just Runs

        // Act
        repository.deleteCategory(category)

        // Assert
        coVerify { categoryDao.deleteCategory(match { it.id == 5 }) }
    }

    // ==================== getCategoryByName Tests ====================

    @Test
    fun `getCategoryByName returns category when found`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 1,
            name = "Food",
            icon = "🍔",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.getCategoryByName("Food") } returns category

        // Act
        val result = repository.getCategoryByName("Food")

        // Assert
        assertNotNull(result)
        assertEquals("Food", result?.name)
        assertEquals(1, result?.id)
        assertEquals("🍔", result?.icon)
    }

    @Test
    fun `getCategoryByName returns null when not found`() = runTest {
        // Arrange
        coEvery { categoryDao.getCategoryByName("NonExistent") } returns null

        // Act
        val result = repository.getCategoryByName("NonExistent")

        // Assert
        assertNull(result)
    }

    @Test
    fun `getCategoryByName is case sensitive`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 1,
            name = "Food",
            icon = "🍔",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.getCategoryByName("Food") } returns category
        coEvery { categoryDao.getCategoryByName("food") } returns null

        // Act
        val resultCorrect = repository.getCategoryByName("Food")
        val resultLower = repository.getCategoryByName("food")

        // Assert
        assertNotNull(resultCorrect)
        assertNull(resultLower)
    }

    @Test
    fun `getCategoryByName with empty string`() = runTest {
        // Arrange
        coEvery { categoryDao.getCategoryByName("") } returns null

        // Act
        val result = repository.getCategoryByName("")

        // Assert
        assertNull(result)
    }

    @Test
    fun `getCategoryByName with special characters`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 1,
            name = "Food & Drinks",
            icon = "🍔",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.getCategoryByName("Food & Drinks") } returns category

        // Act
        val result = repository.getCategoryByName("Food & Drinks")

        // Assert
        assertNotNull(result)
        assertEquals("Food & Drinks", result?.name)
    }

    // ==================== Integration-style Tests ====================

    @Test
    fun `category CRUD operations sequence`() = runTest {
        // Arrange
        val newCategory = CategoryEntity(
            name = "Gym",
            icon = "💪",
            color = 0xFF0000,
            type = "EXPENSE"
        )
        val insertedCategory = newCategory.copy(id = 1)
        
        coEvery { categoryDao.insertCategory(any()) } just Runs
        coEvery { categoryDao.getCategoryByName("Gym") } returns insertedCategory
        coEvery { categoryDao.deleteCategory(any()) } just Runs

        // Act & Assert - Create
        repository.insertCategory(newCategory)
        coVerify { categoryDao.insertCategory(match { it.name == "Gym" }) }

        // Act & Assert - Read
        val found = repository.getCategoryByName("Gym")
        assertNotNull(found)
        assertEquals("Gym", found?.name)

        // Act & Assert - Delete
        found?.let { repository.deleteCategory(it) }
        coVerify { categoryDao.deleteCategory(match { it.name == "Gym" }) }
    }

    @Test
    fun `getAllCategories returns correct flow behavior`() = runTest {
        // Arrange
        val categoriesFlow = flowOf(
            listOf(CategoryEntity(id = 1, name = "A", icon = "🔤", color = 0, type = "EXPENSE")),
            listOf(
                CategoryEntity(id = 1, name = "A", icon = "🔤", color = 0, type = "EXPENSE"),
                CategoryEntity(id = 2, name = "B", icon = "🔤", color = 0, type = "INCOME")
            )
        )
        coEvery { categoryDao.getAllCategories() } returns categoriesFlow

        // Act & Assert
        repository.getAllCategories().test {
            val first = awaitItem()
            assertEquals(1, first.size)
            
            val second = awaitItem()
            assertEquals(2, second.size)
            
            awaitComplete()
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `insertCategory with very long name`() = runTest {
        // Arrange
        val longName = "A".repeat(1000)
        val category = CategoryEntity(
            name = longName,
            icon = "📝",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.name == longName }) }
    }

    @Test
    fun `insertCategory with unicode emoji`() = runTest {
        // Arrange
        val category = CategoryEntity(
            name = "Test",
            icon = "🚀",
            color = 0xFF5733,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(category)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.icon == "🚀" }) }
    }

    @Test
    fun `category with zero id is treated as new`() = runTest {
        // Arrange
        val newCategory = CategoryEntity(
            id = 0,
            name = "New",
            icon = "✨",
            color = 0xFFFFFF,
            type = "EXPENSE"
        )
        coEvery { categoryDao.insertCategory(any()) } just Runs

        // Act
        repository.insertCategory(newCategory)

        // Assert
        coVerify { categoryDao.insertCategory(match { it.id == 0 }) }
    }

    @Test
    fun `deleteCategory requires valid id`() = runTest {
        // Arrange
        val category = CategoryEntity(
            id = 999,
            name = "To Delete",
            icon = "🗑️",
            color = 0xFF0000,
            type = "EXPENSE"
        )
        coEvery { categoryDao.deleteCategory(any()) } just Runs

        // Act
        repository.deleteCategory(category)

        // Assert
        coVerify { categoryDao.deleteCategory(match { it.id == 999 }) }
    }
}
