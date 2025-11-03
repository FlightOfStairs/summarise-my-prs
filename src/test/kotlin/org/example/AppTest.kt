package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull

class AppTest : FunSpec({
    test("app has a greeting") {
        App().greeting.shouldNotBeNull()
    }
})
