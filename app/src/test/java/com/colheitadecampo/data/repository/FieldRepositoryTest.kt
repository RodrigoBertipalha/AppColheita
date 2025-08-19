package com.colheitadecampo.data.repository

import com.colheitadecampo.data.local.dao.FieldDao
import com.colheitadecampo.data.model.Field
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class FieldRepositoryTest {
    
    private lateinit var fieldDao: FieldDao
    private lateinit var repository: FieldRepository
    
    @Before
    fun setup() {
        fieldDao = mockk()
        repository = FieldRepositoryImpl(fieldDao)
    }
    
    @Test
    fun `getFieldById should return field when exists`() = runTest {
        // Given
        val fieldId = 1L
        val field = Field(id = fieldId, name = "Test Field")
        coEvery { fieldDao.getFieldById(fieldId) } returns field
        
        // When
        val result = repository.getFieldById(fieldId)
        
        // Then
        assertEquals(field, result)
        coVerify { fieldDao.getFieldById(fieldId) }
    }
    
    @Test
    fun `getFieldById should return null when field does not exist`() = runTest {
        // Given
        val fieldId = 1L
        coEvery { fieldDao.getFieldById(fieldId) } returns null
        
        // When
        val result = repository.getFieldById(fieldId)
        
        // Then
        assertNull(result)
        coVerify { fieldDao.getFieldById(fieldId) }
    }
    
    @Test
    fun `getFieldByName should return field when exists`() = runTest {
        // Given
        val fieldName = "Test Field"
        val field = Field(id = 1L, name = fieldName)
        coEvery { fieldDao.getFieldByName(fieldName) } returns field
        
        // When
        val result = repository.getFieldByName(fieldName)
        
        // Then
        assertEquals(field, result)
        coVerify { fieldDao.getFieldByName(fieldName) }
    }
    
    @Test
    fun `getFieldByName should return null when field does not exist`() = runTest {
        // Given
        val fieldName = "Non-existent Field"
        coEvery { fieldDao.getFieldByName(fieldName) } returns null
        
        // When
        val result = repository.getFieldByName(fieldName)
        
        // Then
        assertNull(result)
        coVerify { fieldDao.getFieldByName(fieldName) }
    }
    
    @Test
    fun `insertField should return id of inserted field`() = runTest {
        // Given
        val fieldId = 1L
        val field = Field(name = "New Field")
        coEvery { fieldDao.insertField(field) } returns fieldId
        
        // When
        val result = repository.insertField(field)
        
        // Then
        assertEquals(fieldId, result)
        coVerify { fieldDao.insertField(field) }
    }
    
    @Test
    fun `getAllFields should return list of fields`() = runTest {
        // Given
        val fields = listOf(
            Field(id = 1L, name = "Field 1"),
            Field(id = 2L, name = "Field 2")
        )
        coEvery { fieldDao.getAllFields() } returns fields
        
        // When
        val result = repository.getAllFields()
        
        // Then
        assertEquals(fields, result)
        coVerify { fieldDao.getAllFields() }
    }
}
