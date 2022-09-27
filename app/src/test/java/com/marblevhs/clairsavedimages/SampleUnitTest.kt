package com.marblevhs.clairsavedimages

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SampleUnitTest {
    @Test
    fun addition_isCorrect() = runTest {
        assertEquals(4, 2 + 2)
    }
}