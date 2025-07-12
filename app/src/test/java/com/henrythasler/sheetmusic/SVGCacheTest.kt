package com.henrythasler.sheetmusic

import android.util.Log
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner::class)
class SVGCacheTest {
    @Before
    fun setUp() {
    }

    @Test
    fun add_cache_element() {
        mockStatic(Log::class.java).use {
            `when`(Log.i(anyString(), anyString())).thenReturn(0)
            mockStatic(SVG::class.java).use { mockSVG ->
                mockSVG.`when`<SVG> { SVG.getFromString(anyString()) }
                    .thenThrow(SVGParseException::class.java)
                val res = SVGCache.getCachedSVG("invalid svg data")
                assertNull(res)
            }
        }
    }
}