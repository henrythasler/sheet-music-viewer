package com.henrythasler.sheetmusic

import android.util.Log
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
            val res = SVGCache.getCachedSVG("invalid svg data")
            assertNull(res)
        }
    }
}