package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.engine.CaseTransformMode
import com.example.engine.TextCursorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun stepCursor_singleCharacter_movesCorrectly() {
        val tfv = TextFieldValue("Merhaba Dünya", selection = TextRange(5))
        
        val steppedRight = TextCursorEngine.stepCursor(tfv, 1)
        assertEquals(6, steppedRight.selection.start)
        assertEquals(6, steppedRight.selection.end)

        val steppedLeft = TextCursorEngine.stepCursor(tfv, -1)
        assertEquals(4, steppedLeft.selection.start)
        assertEquals(4, steppedLeft.selection.end)
    }

    @Test
    fun stepCursor_withSelectionExpansion_expandsRange() {
        val tfv = TextFieldValue("Precision Cursor", selection = TextRange(0, 0))
        
        val selRight = TextCursorEngine.stepCursor(tfv, 3, expandSelection = true)
        assertEquals(0, selRight.selection.start)
        assertEquals(3, selRight.selection.end)
        assertEquals("Pre", selRight.text.substring(selRight.selection.min, selRight.selection.max))
    }

    @Test
    fun jumpWord_turkishText_jumpsAccurately() {
        val tfv = TextFieldValue("Hassas imleç kontrolü", selection = TextRange(0))
        
        val word1 = TextCursorEngine.jumpWord(tfv, forward = true, locale = Locale("tr"))
        assertEquals(6, word1.selection.start) // After "Hassas"

        val word2 = TextCursorEngine.jumpWord(word1, forward = true, locale = Locale("tr"))
        assertTrue(word2.selection.start >= 12) // After "imleç"
    }

    @Test
    fun jumpLine_and_jumpDocument_workCorrectly() {
        val multiLine = "İlk satır metni\nİkinci satır kodu\nÜçüncü satır sonu"
        val tfv = TextFieldValue(multiLine, selection = TextRange(20))

        val docStart = TextCursorEngine.jumpDocument(tfv, toEnd = false)
        assertEquals(0, docStart.selection.start)

        val docEnd = TextCursorEngine.jumpDocument(tfv, toEnd = true)
        assertEquals(multiLine.length, docEnd.selection.start)

        val lineStart = TextCursorEngine.jumpLine(tfv, toEnd = false)
        assertEquals(16, lineStart.selection.start) // After first \n
    }

    @Test
    fun wrapSelection_wrapsCorrectly() {
        val tfv = TextFieldValue("seçilen", selection = TextRange(0, 7))
        val wrapped = TextCursorEngine.wrapSelection(tfv, "\"", "\"")
        assertEquals("\"seçilen\"", wrapped.text)
    }

    @Test
    fun transformCase_transformsAccurately() {
        val tfv = TextFieldValue("istanbul", selection = TextRange(0, 8))
        val upper = TextCursorEngine.transformCase(tfv, CaseTransformMode.UPPERCASE, Locale("tr"))
        assertEquals("İSTANBUL", upper.text)
    }

    @Test
    fun characterInspector_analyzesAccurately() {
        val text = "Satır 1\nSatır 2"
        val tfv = TextFieldValue(text, selection = TextRange(8)) // At 'S' of Satır 2
        val inspect = TextCursorEngine.inspect(tfv)

        assertEquals(2, inspect.lineNumber)
        assertEquals(8, inspect.cursorIndex)
        assertEquals('S', inspect.characterUnderCursor)
        assertEquals("U+0053", inspect.unicodeCodePoint)
        assertEquals("Harf (Letter)", inspect.unicodeCategory)
    }
}
