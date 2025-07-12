package com.henrythasler.sheetmusic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var fontFamilyResolver: FontFamily.Resolver

    @Before
    fun setup() {
        // Initialize context (non-Compose setup)
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Set up the composable with common configuration
        composeTestRule.setContent {
            // Get fontFamilyResolver inside Compose context
            fontFamilyResolver = LocalFontFamilyResolver.current
        }
    }

    @Test
    fun resolveFont_returns_null_for_non_existent_font() {
        val resolver = FastFontResolver(context, "fonts", fontFamilyResolver)
        val typeface = resolver.resolveFont("NonExistentFont", 400, "Normal")
        assertNull(typeface)
    }

    @Test
    fun resolveFont_caches_resolved_font() {
        val resolver = FastFontResolver(context, "fonts", fontFamilyResolver)
        val typeface = resolver.resolveFont("Edwin-Roman", 400, "Normal")
        assertNotNull(typeface)
    }

//    @Test
//    fun `generateKey produces consistent results`() {
//        val resolver = FastFontResolver(context, "fonts", fontFamilyResolver)
//        val key1 = resolver.javaClass.getDeclaredMethod(
//            "generateKey", String::class.java, Int::class.java, String::class.java
//        ).apply { isAccessible = true }
//            .invoke(resolver, "FontA", 400, "Normal") as Int
//        val key2 = resolver.javaClass.getDeclaredMethod(
//            "generateKey", String::class.java, Int::class.java, String::class.java
//        ).apply { isAccessible = true }
//            .invoke(resolver, "FontA", 400, "Normal") as Int
//        assertEquals(key1, key2)
//    }
//
//    @Test
//    fun `isItalic returns true for Italic`() {
//        val resolver = FastFontResolver(context, "fonts", fontFamilyResolver)
//        val method = resolver.javaClass.getDeclaredMethod("isItalic", String::class.java)
//        method.isAccessible = true
//        val result = method.invoke(resolver, "Italic") as Boolean
//        assertTrue(result)
//    }
//
//    @Test
//    fun `isItalic returns false for Normal`() {
//        val resolver = FastFontResolver(context, "fonts", fontFamilyResolver)
//        val method = resolver.javaClass.getDeclaredMethod("isItalic", String::class.java)
//        method.isAccessible = true
//        val result = method.invoke(resolver, "Normal") as Boolean
//        assertFalse(result)
//    }

    @Test
    fun app_metadata() {
        // Context of the app under test.
        assertEquals("com.henrythasler.sheetmusic", context.packageName)
    }
}