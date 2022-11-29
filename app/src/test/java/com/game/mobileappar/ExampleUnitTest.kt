package com.game.mobileappar

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testFlows() = runBlocking {
        val flow: Flow<Int> = flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        flow
            .filter { it % 2 == 0 }
            .map { it % 10 }
            .collect {
                println(it)
            }

        flow
            .filter { it % 2 == 1 }
            .collect {
                println(it)
            }

    }
}