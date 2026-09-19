package com.example.engine

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.BreakIterator
import java.util.Locale

data class CharacterInspectorData(
    val cursorIndex: Int,
    val totalLength: Int,
    val lineNumber: Int,
    val totalLines: Int,
    val columnNumber: Int,
    val characterUnderCursor: Char?,
    val unicodeCodePoint: String,
    val unicodeCategory: String,
    val selectionLength: Int,
    val selectionWordCount: Int,
    val selectedTextPreview: String,
    val previousChar: Char?,
    val nextChar: Char?,
    val isStartOfDocument: Boolean,
    val isEndOfDocument: Boolean,
    val isStartOfLine: Boolean,
    val isEndOfLine: Boolean
)

object TextCursorEngine {

    /**
     * Move cursor by delta characters.
     * If [expandSelection] is true, expands or shrinks the selection range from anchor.
     */
    fun stepCursor(
        current: TextFieldValue,
        delta: Int,
        expandSelection: Boolean = false
    ): TextFieldValue {
        val text = current.text
        val len = text.length
        if (len == 0) return current

        val sel = current.selection
        if (!expandSelection) {
            // Normal cursor move: collapse selection to destination
            val base = if (sel.collapsed) sel.start else if (delta < 0) sel.min else sel.max
            val target = (base + delta).coerceIn(0, len)
            return current.copy(selection = TextRange(target))
        } else {
            // Expand / contract selection with anchor preserved
            // We maintain an anchor (start) and move end
            val anchor = if (sel.collapsed) sel.start else sel.start
            val activeEnd = sel.end
            val newEnd = (activeEnd + delta).coerceIn(0, len)
            return current.copy(selection = TextRange(anchor, newEnd))
        }
    }

    /**
     * Jump by word using language-aware BreakIterator.
     */
    fun jumpWord(
        current: TextFieldValue,
        forward: Boolean,
        expandSelection: Boolean = false,
        locale: Locale = Locale.getDefault()
    ): TextFieldValue {
        val text = current.text
        val len = text.length
        if (len == 0) return current

        val iterator = BreakIterator.getWordInstance(locale)
        iterator.setText(text)

        val activePos = if (expandSelection) current.selection.end else {
            if (current.selection.collapsed) current.selection.start
            else if (forward) current.selection.max else current.selection.min
        }

        val targetPos = if (forward) {
            var next = iterator.following(activePos)
            while (next != BreakIterator.DONE && next < len && isWhitespaceOnly(text, activePos, next)) {
                next = iterator.next()
            }
            if (next == BreakIterator.DONE) len else next.coerceIn(0, len)
        } else {
            var prev = iterator.preceding(activePos)
            while (prev != BreakIterator.DONE && prev > 0 && isWhitespaceOnly(text, prev, activePos)) {
                prev = iterator.previous()
            }
            if (prev == BreakIterator.DONE) 0 else prev.coerceIn(0, len)
        }

        return if (!expandSelection) {
            current.copy(selection = TextRange(targetPos))
        } else {
            val anchor = current.selection.start
            current.copy(selection = TextRange(anchor, targetPos))
        }
    }

    private fun isWhitespaceOnly(text: String, start: Int, end: Int): Boolean {
        val s = start.coerceIn(0, text.length)
        val e = end.coerceIn(0, text.length)
        if (s >= e) return false
        for (i in s until e) {
            if (!text[i].isWhitespace()) return false
        }
        return true
    }

    /**
     * Jump to line start or line end.
     */
    fun jumpLine(
        current: TextFieldValue,
        toEnd: Boolean,
        expandSelection: Boolean = false
    ): TextFieldValue {
        val text = current.text
        val len = text.length
        if (len == 0) return current

        val activePos = if (expandSelection) current.selection.end else current.selection.start
        val targetPos = if (toEnd) {
            val nextNewline = text.indexOf('\n', activePos)
            if (nextNewline == -1) len else nextNewline
        } else {
            val prevNewline = text.lastIndexOf('\n', (activePos - 1).coerceAtLeast(0))
            if (prevNewline == -1) 0 else prevNewline + 1
        }

        return if (!expandSelection) {
            current.copy(selection = TextRange(targetPos))
        } else {
            val anchor = current.selection.start
            current.copy(selection = TextRange(anchor, targetPos))
        }
    }

    /**
     * Jump to document start (0) or end (length).
     */
    fun jumpDocument(
        current: TextFieldValue,
        toEnd: Boolean,
        expandSelection: Boolean = false
    ): TextFieldValue {
        val targetPos = if (toEnd) current.text.length else 0
        return if (!expandSelection) {
            current.copy(selection = TextRange(targetPos))
        } else {
            current.copy(selection = TextRange(current.selection.start, targetPos))
        }
    }

    /**
     * Select word at current cursor position.
     */
    fun selectCurrentWord(
        current: TextFieldValue,
        locale: Locale = Locale.getDefault()
    ): TextFieldValue {
        val text = current.text
        val len = text.length
        if (len == 0) return current

        val iterator = BreakIterator.getWordInstance(locale)
        iterator.setText(text)

        val pos = current.selection.start.coerceIn(0, len)
        var start = iterator.preceding((pos + 1).coerceAtMost(len))
        var end = iterator.following((pos - 1).coerceAtLeast(0))

        if (start == BreakIterator.DONE) start = 0
        if (end == BreakIterator.DONE) end = len

        // Find boundary
        var currentBoundary = iterator.first()
        var wordStart = 0
        var wordEnd = len
        while (currentBoundary != BreakIterator.DONE) {
            val next = iterator.next()
            if (next != BreakIterator.DONE && pos >= currentBoundary && pos <= next) {
                wordStart = currentBoundary
                wordEnd = next
                break
            }
            currentBoundary = next
        }

        // Trim whitespace from selected word if desired
        while (wordStart < wordEnd && text[wordStart].isWhitespace()) wordStart++
        while (wordEnd > wordStart && text[wordEnd - 1].isWhitespace()) wordEnd--

        return if (wordStart <= wordEnd) {
            current.copy(selection = TextRange(wordStart, wordEnd))
        } else {
            current
        }
    }

    /**
     * Select the whole current sentence or paragraph.
     */
    fun selectCurrentSentence(
        current: TextFieldValue,
        locale: Locale = Locale.getDefault()
    ): TextFieldValue {
        val text = current.text
        val len = text.length
        if (len == 0) return current

        val iterator = BreakIterator.getSentenceInstance(locale)
        iterator.setText(text)

        val pos = current.selection.start.coerceIn(0, len)
        var boundary = iterator.first()
        var sStart = 0
        var sEnd = len
        while (boundary != BreakIterator.DONE) {
            val next = iterator.next()
            if (next != BreakIterator.DONE && pos >= boundary && pos <= next) {
                sStart = boundary
                sEnd = next
                break
            }
            boundary = next
        }

        return current.copy(selection = TextRange(sStart, sEnd))
    }

    /**
     * Select Entire Document.
     */
    fun selectAll(current: TextFieldValue): TextFieldValue {
        return current.copy(selection = TextRange(0, current.text.length))
    }

    /**
     * Clear selection (collapse to end or start).
     */
    fun clearSelection(current: TextFieldValue): TextFieldValue {
        return current.copy(selection = TextRange(current.selection.max))
    }

    /**
     * Delete character before cursor (Backspace).
     */
    fun deleteCharBefore(current: TextFieldValue): TextFieldValue {
        val text = current.text
        val sel = current.selection
        if (!sel.collapsed) {
            val newText = text.removeRange(sel.min, sel.max)
            return current.copy(text = newText, selection = TextRange(sel.min))
        }
        if (sel.start <= 0) return current
        val newText = text.removeRange(sel.start - 1, sel.start)
        return current.copy(text = newText, selection = TextRange(sel.start - 1))
    }

    /**
     * Delete character after cursor (Delete).
     */
    fun deleteCharAfter(current: TextFieldValue): TextFieldValue {
        val text = current.text
        val sel = current.selection
        if (!sel.collapsed) {
            val newText = text.removeRange(sel.min, sel.max)
            return current.copy(text = newText, selection = TextRange(sel.min))
        }
        if (sel.start >= text.length) return current
        val newText = text.removeRange(sel.start, sel.start + 1)
        return current.copy(text = newText, selection = TextRange(sel.start))
    }

    /**
     * Wrap selection with opening and closing tags/brackets (e.g. `"` or `(` `)`).
     */
    fun wrapSelection(
        current: TextFieldValue,
        open: String,
        close: String
    ): TextFieldValue {
        val text = current.text
        val sel = current.selection
        if (sel.collapsed) {
            // Insert brackets and place cursor inside
            val newText = text.substring(0, sel.start) + open + close + text.substring(sel.start)
            val newPos = sel.start + open.length
            return current.copy(text = newText, selection = TextRange(newPos))
        } else {
            val selected = text.substring(sel.min, sel.max)
            val wrapped = open + selected + close
            val newText = text.replaceRange(sel.min, sel.max, wrapped)
            return current.copy(
                text = newText,
                selection = TextRange(sel.min + open.length, sel.min + open.length + selected.length)
            )
        }
    }

    /**
     * Transform casing of selection or current word.
     */
    fun transformCase(
        current: TextFieldValue,
        mode: CaseTransformMode,
        locale: Locale = Locale.getDefault()
    ): TextFieldValue {
        val target = if (current.selection.collapsed) selectCurrentWord(current, locale) else current
        val sel = target.selection
        if (sel.min >= sel.max) return current

        val text = target.text
        val selectedText = text.substring(sel.min, sel.max)
        val transformed = when (mode) {
            CaseTransformMode.UPPERCASE -> selectedText.uppercase(locale)
            CaseTransformMode.LOWERCASE -> selectedText.lowercase(locale)
            CaseTransformMode.TITLECASE -> selectedText.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            }
            CaseTransformMode.INVERT -> selectedText.map {
                if (it.isUpperCase()) it.lowercaseChar() else it.uppercaseChar()
            }.joinToString("")
        }

        val newText = text.replaceRange(sel.min, sel.max, transformed)
        return target.copy(text = newText, selection = TextRange(sel.min, sel.min + transformed.length))
    }

    /**
     * Duplicate current line or selected text.
     */
    fun duplicateLineOrSelection(current: TextFieldValue): TextFieldValue {
        val text = current.text
        val sel = current.selection
        if (!sel.collapsed) {
            val selected = text.substring(sel.min, sel.max)
            val newText = text.substring(0, sel.max) + selected + text.substring(sel.max)
            return current.copy(
                text = newText,
                selection = TextRange(sel.max, sel.max + selected.length)
            )
        }
        // Duplicate line
        val pos = sel.start
        val lineStart = text.lastIndexOf('\n', (pos - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
        val lineText = text.substring(lineStart, lineEnd)
        val insertion = "\n$lineText"
        val newText = text.substring(0, lineEnd) + insertion + text.substring(lineEnd)
        return current.copy(
            text = newText,
            selection = TextRange(lineEnd + insertion.length)
        )
    }

    /**
     * Computes full character inspector details for the active cursor/selection.
     */
    fun inspect(current: TextFieldValue): CharacterInspectorData {
        val text = current.text
        val len = text.length
        val pos = current.selection.start.coerceIn(0, len)
        val sel = current.selection

        // Line and column calculations
        var lineNum = 1
        var lastNewlinePos = -1
        var totalLines = 1
        for (i in 0 until len) {
            if (text[i] == '\n') {
                totalLines++
                if (i < pos) {
                    lineNum++
                    lastNewlinePos = i
                }
            }
        }
        val colNum = pos - lastNewlinePos

        // Characters around cursor
        val charUnder = if (pos in 0 until len) text[pos] else null
        val prevChar = if (pos > 0 && pos - 1 < len) text[pos - 1] else null
        val nextChar = if (pos + 1 in 0 until len) text[pos + 1] else null

        // Unicode info
        val unicodeCodePoint = if (charUnder != null) {
            "U+" + charUnder.code.toString(16).uppercase().padStart(4, '0')
        } else {
            "N/A"
        }

        val unicodeCategory = if (charUnder != null) {
            when {
                charUnder.isLetter() -> "Harf (Letter)"
                charUnder.isDigit() -> "Rakam (Digit)"
                charUnder.isWhitespace() -> "Boşluk (Whitespace)"
                Character.isISOControl(charUnder) -> "Kontrol (Control)"
                else -> "İşaret/Sembol (Punctuation)"
            }
        } else {
            "Metin Sonu"
        }

        val selLen = if (sel.collapsed) 0 else Math.abs(sel.end - sel.start)
        val selPreview = if (!sel.collapsed && sel.min < sel.max && sel.max <= len) {
            val s = text.substring(sel.min, sel.max)
            if (s.length > 28) s.take(28) + "..." else s
        } else ""

        val selWords = if (selPreview.isNotBlank()) {
            selPreview.trim().split("\\s+".toRegex()).size
        } else 0

        val isStartDoc = pos == 0
        val isEndDoc = pos == len
        val isStartLine = pos == 0 || (pos - 1 >= 0 && text[pos - 1] == '\n')
        val isEndLine = pos == len || (pos < len && text[pos] == '\n')

        return CharacterInspectorData(
            cursorIndex = pos,
            totalLength = len,
            lineNumber = lineNum,
            totalLines = totalLines,
            columnNumber = colNum,
            characterUnderCursor = charUnder,
            unicodeCodePoint = unicodeCodePoint,
            unicodeCategory = unicodeCategory,
            selectionLength = selLen,
            selectionWordCount = selWords,
            selectedTextPreview = selPreview,
            previousChar = prevChar,
            nextChar = nextChar,
            isStartOfDocument = isStartDoc,
            isEndOfDocument = isEndDoc,
            isStartOfLine = isStartLine,
            isEndOfLine = isEndLine
        )
    }
}

enum class CaseTransformMode {
    UPPERCASE,
    LOWERCASE,
    TITLECASE,
    INVERT
}
