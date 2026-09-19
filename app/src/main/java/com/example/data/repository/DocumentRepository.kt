package com.example.data.repository

import com.example.data.db.DocumentDao
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val dao: DocumentDao) {
    val allDocuments: Flow<List<DocumentEntity>> = dao.getAllDocuments()

    suspend fun getDocumentById(id: Long): DocumentEntity? = dao.getDocumentById(id)

    suspend fun saveDocument(document: DocumentEntity): Long = dao.insertDocument(document)

    suspend fun updateDocument(document: DocumentEntity) = dao.updateDocument(document)

    suspend fun deleteDocument(document: DocumentEntity) = dao.deleteDocument(document)

    suspend fun deleteDocumentById(id: Long) = dao.deleteDocumentById(id)
}
