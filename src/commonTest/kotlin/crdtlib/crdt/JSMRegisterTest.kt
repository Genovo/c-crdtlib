/*
* Copyright © 2020, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package crdtlib.crdt

import crdtlib.utils.VersionVector
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*

/**
* Represents a suite test for JSMRegister.
**/
class JSMRegisterTest : StringSpec({

    /**
    * This test evaluates the scenario: create string value get.
    * Call to get should return the value set by the constructor.
    */
    "create string register then get" {
        val value = "value"

        val reg = JSMRegister<String>(value)

        reg.get().shouldBe(value)
    }

    /**
    * This test evaluates the scenario: create int value get.
    * Call to get should return the value set by the constructor.
    */
    "create integer register then get" {
        val value = 42

        val reg = JSMRegister<Int>(value)

        reg.get().shouldBe(value)
    }

    /**
    * This test evaluates the scenario: create assign (with greater value) get.
    * Call to get should return the value set by assign.
    */
    "create, assign greater value, get" {
        val val1 = 42
        val val2 = 100

        val reg = JSMRegister<Int>(val1)
        reg.assign(val2)

        reg.get().shouldBe(val2)
    }

    /**
    * This test evaluates the scenario: create assign (with lower value) get.
    * Call to get should return the value set by the constructor.
    */
    "create, assign lower value, get" {
        val val1 = 42
        val val2 = 3

        val reg = JSMRegister<Int>(val1)
        reg.assign(val2)

        reg.get().shouldBe(val1)
    }

    /**
    * This test evaluates the scenario: create || create (with greater value) merge get.
    * Call to get should return the value set by the second replica.
    */
    "R1: create; R2: create with greater value, merge, get" {
        val val1 = 42
        val val2 = 101

        val reg1 = JSMRegister<Int>(val1)
        val reg2 = JSMRegister<Int>(val2)
        reg2.merge(reg1)

        reg2.get().shouldBe(val2)
    }

    /**
    * This test evaluates the scenario: create || create (with lower value) merge get.
    * Call to get should return the value set by the first replica.
    */
    "R1: create; R2: create with lower value, merge, get" {
        val val1 = 42
        val val2 = 41

        val reg1 = JSMRegister<Int>(val1)
        val reg2 = JSMRegister<Int>(val2)
        reg2.merge(reg1)

        reg2.get().shouldBe(val1)
    }

    /**
    * This test evaluates the scenario: create (with lower value) assign (with the greatest value)
    * || create (with lower value) merge get.
    * Call to get should return the value set by assign in the first replica.
    */
    "R1: create, assign with the greatest value; R2: create, merge, get" {
        val val1 = "BBB"
        val val2 = "CCC"
        val val3 = "AAA"

        val reg1 = JSMRegister<String>(val1)
        reg1.assign(val2)
        val reg2 = JSMRegister<String>(val3)
        reg2.merge(reg1)

        reg2.get().shouldBe(val2)
    }

    /**
    * This test evaluates the scenario: create (with lower value) assign (with a greater value) ||
    * create (with the greatest value) merge get.
    * Call to get should return the value set in the second replica.
    */
    "R1: create, assign; R2: create with greatest value, merge, get" {
        val val1 = "AAA"
        val val2 = "BBB"
        val val3 = "CCC"

        val reg1 = JSMRegister<String>(val1)
        reg1.assign(val2)
        val reg2 = JSMRegister<String>(val3)
        reg2.merge(reg1)

        reg2.get().shouldBe(val3)
    }

    /**
    * This test evaluates the scenario: create (with the greatest value) assign (with lower value)
    * || create (with lower value) merge get.
    * Call to get should return the value set at initialization in the first replica.
    */
    "R1: create with greatest value, assign; R2: create, merge, get" {
        val val1 = "CCC"
        val val2 = "AAA"
        val val3 = "BBB"

        val reg1 = JSMRegister<String>(val1)
        reg1.assign(val2)
        val reg2 = JSMRegister<String>(val3)
        reg2.merge(reg1)

        reg2.get().shouldBe(val1)
    }

    /**
    * This test evaluates the scenario: create (with lower value) merge (before assign in replica 2)
    * || create (with the greatest value) assign (with lower value) merge get.
    * Call to get should return the value set at initialization in the second replica.
    */
    "R1: create, merge before assign; r2: create with greatest value, assign, merge" {
        val val1 = 4
        val val2 = 5
        val val3 = 2

        val reg1 = JSMRegister<Int>(val1)
        val reg2 = JSMRegister<Int>(val2)
        reg1.merge(reg2)
        reg2.assign(val3)
        reg2.merge(reg1)

        reg2.get().shouldBe(val2)
    }

    /**
    * This test evaluates the use of delta return by call to assign method.
    * Call to get should return value set at initialization in the first replica.
    */
    "use delta returned by assign" {
        val val1 = 8
        val val2 = 6
        val val3 = 5

        val reg1 = JSMRegister<Int>(val1)
        val assignOp = reg1.assign(val2)
        val reg2 = JSMRegister<Int>(val3)
        reg2.merge(assignOp)

        reg2.get().shouldBe(val1)
    }

    /*
    * This test evaluates the generation of delta plus its merging into another replica.
    * Call to get should return the value set at initialization in the first replica.
    */
    "generate delta then merge" {
        val vv = VersionVector()
        val val1 = 8
        val val2 = 6
        val val3 = 5

        val reg1 = JSMRegister<Int>(val1)
        reg1.assign(val2)
        val reg2 = JSMRegister<Int>(val3)
        val delta = reg1.generateDelta(vv)
        reg2.merge(delta)

        reg2.get().shouldBe(val1)
    }

    /**
    * This test evaluates JSON serialization of a JSM register.
    **/
    "JSON serialization" {
        val value = "VALUE"

        val reg = JSMRegister<String>(value)
        val regJson = reg.toJson()

        regJson.shouldBe("""{"_type":"JSMRegister","value":"VALUE"}""")
    }

    /**
    * This test evaluates JSON deserialization of a JSM register.
    **/
    "JSON deserialization" {
        val regJson = JSMRegister.fromJson<String>("""{"_type":"JSMRegister","value":"VALUE"}""")

        regJson.get().shouldBe("VALUE")
    }
})
