package com.example.ui

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DocumentEntity
import com.example.data.repository.DocumentRepository
import com.example.engine.CaseTransformMode
import com.example.engine.CharacterInspectorData
import com.example.engine.TextCursorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.LinkedList

enum class HudPositionMode {
    FLOATING_ABOVE,
    FLOATING_BELOW,
    DOCKED_BOTTOM,
    COMPACT_CHEVRONS
}

enum class ScrubberSensitivity(val label: String, val pixelsPerChar: Float) {
    FINE("Hassas (12px)", 12f),
    NORMAL("Normal (8px)", 8f),
    FAST("Hızlı (4px)", 4f)
}

data class PrecisionCursorUiState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val activeDocument: DocumentEntity? = null,
    val expandSelectionMode: Boolean = false,
    val hudPositionMode: HudPositionMode = HudPositionMode.FLOATING_ABOVE,
    val isHudVisible: Boolean = true,
    val scrubberSensitivity: ScrubberSensitivity = ScrubberSensitivity.NORMAL,
    val hapticFeedbackEnabled: Boolean = true,
    val inspectorData: CharacterInspectorData = TextCursorEngine.inspect(TextFieldValue("")),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showInspectorSheet: Boolean = false,
    val showDocumentDrawer: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val statusMessage: String? = null
)

class PrecisionCursorViewModel(
    application: Application,
    private val repository: DocumentRepository
) : AndroidViewModel(application) {

    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PrecisionCursorUiState())
    val uiState: StateFlow<PrecisionCursorUiState> = _uiState.asStateFlow()

    // Undo / Redo history stacks
    private val undoStack = LinkedList<TextFieldValue>()
    private val redoStack = LinkedList<TextFieldValue>()
    private val maxHistorySize = 50

    // Touch scrubber accumulator
    private var scrubberAccumulator = 0f

    init {
        viewModelScope.launch {
            allDocuments.collect { docs ->
                if (_uiState.value.activeDocument == null && docs.isNotEmpty()) {
                    loadDocument(docs.first())
                }
            }
        }
    }

    private fun pushHistory(current: TextFieldValue) {
        if (undoStack.isEmpty() || undoStack.last.text != current.text) {
            if (undoStack.size >= maxHistorySize) {
                undoStack.removeFirst()
            }
            undoStack.add(current)
            redoStack.clear()
            updateHistoryFlags()
        }
    }

    private fun updateHistoryFlags() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun onTextChanged(newValue: TextFieldValue) {
        val current = _uiState.value.textFieldValue
        if (current.text != newValue.text) {
            pushHistory(current)
        }
        val inspector = TextCursorEngine.inspect(newValue)
        _uiState.update {
            it.copy(
                textFieldValue = newValue,
                inspectorData = inspector,
                isHudVisible = true
            )
        }
    }

    fun stepCursor(delta: Int) {
        val current = _uiState.value.textFieldValue
        val isSelection = _uiState.value.expandSelectionMode
        val next = TextCursorEngine.stepCursor(current, delta, expandSelection = isSelection)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                isHudVisible = true
            )
        }
    }

    fun jumpWord(forward: Boolean) {
        val current = _uiState.value.textFieldValue
        val isSelection = _uiState.value.expandSelectionMode
        val next = TextCursorEngine.jumpWord(current, forward, expandSelection = isSelection)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                isHudVisible = true
            )
        }
    }

    fun jumpLine(toEnd: Boolean) {
        val current = _uiState.value.textFieldValue
        val isSelection = _uiState.value.expandSelectionMode
        val next = TextCursorEngine.jumpLine(current, toEnd, expandSelection = isSelection)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                isHudVisible = true
            )
        }
    }

    fun jumpDocument(toEnd: Boolean) {
        val current = _uiState.value.textFieldValue
        val isSelection = _uiState.value.expandSelectionMode
        val next = TextCursorEngine.jumpDocument(current, toEnd, expandSelection = isSelection)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                isHudVisible = true
            )
        }
    }

    fun onScrubberDrag(dragPixels: Float): Boolean {
        scrubberAccumulator += dragPixels
        val threshold = _uiState.value.scrubberSensitivity.pixelsPerChar
        var stepped = false

        while (scrubberAccumulator >= threshold) {
            stepCursor(1)
            scrubberAccumulator -= threshold
            stepped = true
        }
        while (scrubberAccumulator <= -threshold) {
            stepCursor(-1)
            scrubberAccumulator += threshold
            stepped = true
        }
        return stepped
    }

    fun resetScrubber() {
        scrubberAccumulator = 0f
    }

    fun toggleSelectionMode() {
        _uiState.update {
            val newMode = !it.expandSelectionMode
            val cur = it.textFieldValue
            // If turning on selection mode and collapsed, set anchor
            val updatedTfv = if (newMode && cur.selection.collapsed) {
                cur.copy(selection = TextRange(cur.selection.start, cur.selection.start))
            } else cur

            it.copy(
                expandSelectionMode = newMode,
                textFieldValue = updatedTfv,
                statusMessage = if (newMode) "Seçim Modu Aktif (İmleç Seçerek Genişler)" else "Normal İmleç Modu"
            )
        }
    }

    fun selectCurrentWord() {
        val current = _uiState.value.textFieldValue
        val next = TextCursorEngine.selectCurrentWord(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                expandSelectionMode = true,
                statusMessage = "Kelime Seçildi"
            )
        }
    }

    fun selectCurrentSentence() {
        val current = _uiState.value.textFieldValue
        val next = TextCursorEngine.selectCurrentSentence(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                expandSelectionMode = true,
                statusMessage = "Cümle/Paragraf Seçildi"
            )
        }
    }

    fun selectAll() {
        val current = _uiState.value.textFieldValue
        val next = TextCursorEngine.selectAll(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                expandSelectionMode = true,
                statusMessage = "Tüm Metin Seçildi"
            )
        }
    }

    fun clearSelection() {
        val current = _uiState.value.textFieldValue
        val next = TextCursorEngine.clearSelection(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                expandSelectionMode = false,
                statusMessage = "Seçim Kaldırıldı"
            )
        }
    }

    fun deleteCharBefore() {
        val current = _uiState.value.textFieldValue
        pushHistory(current)
        val next = TextCursorEngine.deleteCharBefore(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector
            )
        }
    }

    fun deleteCharAfter() {
        val current = _uiState.value.textFieldValue
        pushHistory(current)
        val next = TextCursorEngine.deleteCharAfter(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector
            )
        }
    }

    fun wrapSelection(open: String, close: String) {
        val current = _uiState.value.textFieldValue
        pushHistory(current)
        val next = TextCursorEngine.wrapSelection(current, open, close)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                statusMessage = "Parantez içine alındı: $open $close"
            )
        }
    }

    fun transformCase(mode: CaseTransformMode) {
        val current = _uiState.value.textFieldValue
        pushHistory(current)
        val next = TextCursorEngine.transformCase(current, mode)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                statusMessage = "Harf dönüşümü uygulandı"
            )
        }
    }

    fun duplicate() {
        val current = _uiState.value.textFieldValue
        pushHistory(current)
        val next = TextCursorEngine.duplicateLineOrSelection(current)
        val inspector = TextCursorEngine.inspect(next)
        _uiState.update {
            it.copy(
                textFieldValue = next,
                inspectorData = inspector,
                statusMessage = "Çoğaltıldı (Duplicate)"
            )
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val cur = _uiState.value.textFieldValue
            redoStack.add(cur)
            val previous = undoStack.removeLast()
            val inspector = TextCursorEngine.inspect(previous)
            _uiState.update {
                it.copy(
                    textFieldValue = previous,
                    inspectorData = inspector,
                    statusMessage = "Geri Alındı (Undo)"
                )
            }
            updateHistoryFlags()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val cur = _uiState.value.textFieldValue
            undoStack.add(cur)
            val next = redoStack.removeLast()
            val inspector = TextCursorEngine.inspect(next)
            _uiState.update {
                it.copy(
                    textFieldValue = next,
                    inspectorData = inspector,
                    statusMessage = "Yinelendi (Redo)"
                )
            }
            updateHistoryFlags()
        }
    }

    fun setHudPositionMode(mode: HudPositionMode) {
        _uiState.update { it.copy(hudPositionMode = mode) }
    }

    fun setScrubberSensitivity(sensitivity: ScrubberSensitivity) {
        _uiState.update { it.copy(scrubberSensitivity = sensitivity) }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
    }

    fun setHudVisibility(visible: Boolean) {
        _uiState.update { it.copy(isHudVisible = visible) }
    }

    fun toggleInspectorSheet(show: Boolean) {
        _uiState.update { it.copy(showInspectorSheet = show) }
    }

    fun toggleDocumentDrawer(show: Boolean) {
        _uiState.update { it.copy(showDocumentDrawer = show) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun loadDocument(doc: DocumentEntity) {
        val tfv = TextFieldValue(
            text = doc.content,
            selection = TextRange(doc.cursorPosition.coerceIn(0, doc.content.length))
        )
        undoStack.clear()
        redoStack.clear()
        updateHistoryFlags()
        val inspector = TextCursorEngine.inspect(tfv)
        _uiState.update {
            it.copy(
                activeDocument = doc,
                textFieldValue = tfv,
                inspectorData = inspector,
                showDocumentDrawer = false,
                statusMessage = "Belge Yüklendi: ${doc.title}"
            )
        }
    }

    fun saveCurrentDocument(title: String? = null, category: String? = null) {
        val curDoc = _uiState.value.activeDocument
        val content = _uiState.value.textFieldValue.text
        val cursor = _uiState.value.textFieldValue.selection.start

        viewModelScope.launch {
            if (curDoc != null) {
                val updated = curDoc.copy(
                    title = title ?: curDoc.title,
                    category = category ?: curDoc.category,
                    content = content,
                    cursorPosition = cursor,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateDocument(updated)
                _uiState.update {
                    it.copy(
                        activeDocument = updated,
                        statusMessage = "Belge Kaydedildi: ${updated.title}"
                    )
                }
            } else {
                val newDoc = DocumentEntity(
                    title = title ?: "Yeni Not (${System.currentTimeMillis() % 10000})",
                    category = category ?: "Genel",
                    content = content,
                    cursorPosition = cursor
                )
                val newId = repository.saveDocument(newDoc)
                val saved = newDoc.copy(id = newId)
                _uiState.update {
                    it.copy(
                        activeDocument = saved,
                        statusMessage = "Yeni Belge Oluşturuldu: ${saved.title}"
                    )
                }
            }
        }
    }

    fun createNewDocument(title: String = "Yeni Boş Belge", category: String = "Genel") {
        viewModelScope.launch {
            val newDoc = DocumentEntity(
                title = title,
                category = category,
                content = "",
                cursorPosition = 0
            )
            val newId = repository.saveDocument(newDoc)
            loadDocument(newDoc.copy(id = newId))
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            if (_uiState.value.activeDocument?.id == doc.id) {
                val remaining = allDocuments.value.filter { it.id != doc.id }
                if (remaining.isNotEmpty()) {
                    loadDocument(remaining.first())
                } else {
                    _uiState.update {
                        it.copy(
                            activeDocument = null,
                            textFieldValue = TextFieldValue(""),
                            inspectorData = TextCursorEngine.inspect(TextFieldValue(""))
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application, kotlinx.coroutines.GlobalScope)
                    val repo = DocumentRepository(db.documentDao())
                    return PrecisionCursorViewModel(application, repo) as T
                }
            }
        }
    }
}
