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

import crdtlib.utils.DCUId
import crdtlib.utils.SimpleEnvironment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*

/**
* Represents a test suite for LWWRegister.
**/
class LWWRegisterTest : StringSpec({

    /**
    * This test evaluates the scenario: create get.
    * Call to get should return the value assigned by the constructor.
    */
    "create register and get" {
        val uid = DCUId("dcid")
        val dc = SimpleEnvironment(uid)
        val ts = dc.getNewTimestamp()
        val value = "value"

        val reg = LWWRegister<String>(value, ts)

        reg.get().shouldBe(value)
    }

    /**
    * This test evaluates the scenario: create assign get.
    * Call to get should return the value set by the assign method.
    */
    "create, assign, get" {
        val uid = DCUId("dcid")
        val dc = SimpleEnvironment(uid)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        val val1 = "value1"
        val val2 = "value2"

        val reg = LWWRegister<String>(val1, ts1)
        reg.assign(val2, ts2)

        reg.get().shouldBe(val2)
    }

    /**
    * This test evaluates the scenario: create assign(older timestamp) get.
    * Call to get should return the value set by the constructor.
    */
    "create, assign with older timestamp, get" {
        val uid = DCUId("dcid")
        val dc = SimpleEnvironment(uid)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        val val1 = "value1"
        val val2 = "value2"
        
        val reg = LWWRegister<String>(val1, ts2)
        reg.assign(val2, ts1)

        reg.get().shouldBe(val1)
    }

    /**
    * This test evaluates the scenario: assign || assign merge get.
    * Call to get should return the value set in the second replica.
    */
    "R1: create; R2: create with greater timestamp, merge, get" {
        val uid1 = DCUId("dcid1")
        val uid2 = DCUId("dcid2")
        val dc1 = SimpleEnvironment(uid1)
        val dc2 = SimpleEnvironment(uid2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val val1 = "value1"
        val val2 = "value2"

        val reg1 = LWWRegister<String>(val1, ts1)
        val reg2 = LWWRegister<String>(val2, ts2)
        reg1.merge(reg2)
        reg2.merge(reg1)

        reg1.get().shouldBe(val2)
        reg2.get().shouldBe(val2)
    }

    /**
    * This test evaluates the scenario: assign || assign merge assign get.
    * Call to get should return the value set by call to assign method in the second replica.
    */
    "R1: create; R2: create, merge, assign, get" {
        val uid1 = DCUId("dcid1")
        val uid2 = DCUId("dcid2")
        val dc1 = SimpleEnvironment(uid1)
        val dc2 = SimpleEnvironment(uid2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts3 = dc2.getNewTimestamp()
        val val1 = "value1"
        val val2 = "value2"
        val val3 = "value3"

        val reg1 = LWWRegister<String>(val1, ts1)
        val reg2 = LWWRegister<String>(val2, ts2)
        reg2.merge(reg1)
        reg2.assign(val3, ts3)

        reg2.get().shouldBe(val3)
    }

    /**
    * This test evaluates the use of delta return by call to assign method.
    * Call to get should return last value set in the second replica.
    */
    "use delta generated by assign" {
        val uid1 = DCUId("dcid1")
        val uid2 = DCUId("dcid2")
        val dc1 = SimpleEnvironment(uid1)
        val dc2 = SimpleEnvironment(uid2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val ts3 = dc1.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts4 = dc2.getNewTimestamp()
        val val1 = "value1"
        val val2 = "value2"
        val val3 = "value3"
        val val4 = "value4"

        val reg1 = LWWRegister<String>(val1, ts1)
        val reg2 = LWWRegister<String>(val2, ts2)
        val assignOp1 = reg1.assign(val3, ts3)
        val assignOp2 = reg2.assign(val4, ts4)

        reg1.merge(assignOp2)
        reg2.merge(assignOp1)

        reg1.get().shouldBe(val4)
        reg2.get().shouldBe(val4)
    }

    /*
    * This test evaluates the generation of delta plus its merging into another replica.
    * Call to get should return the values set in the second replica.
    */
    "generate delta then merge" {
        val uid1 = DCUId("dcid1")
        val uid2 = DCUId("dcid2")
        val dc1 = SimpleEnvironment(uid1)
        val dc2 = SimpleEnvironment(uid2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val vv1 = dc1.getCurrentState()
        dc2.updateStateTS(ts2)
        dc2.updateStateTS(ts1)
        val vv2 = dc2.getCurrentState()
        val val1 = "value1"
        val val2 = "value2"

        val reg1 = LWWRegister<String>(val1, ts1)
        val reg2 = LWWRegister<String>(val2, ts2)
        val delta2 = reg1.generateDelta(vv2)
        val delta1 = reg2.generateDelta(vv1)

        reg1.merge(delta1)
        reg2.merge(delta2)

        reg1.get().shouldBe(val2)
        reg2.get().shouldBe(val2)
    }

    /**
    * This test evaluates JSON serialization of a lww register.
    **/
    "JSON serialization" {
        val uid = DCUId("dcid")
        val dc = SimpleEnvironment(uid)
        val ts = dc.getNewTimestamp()
        val value = "value"

        val reg = LWWRegister<String>(value, ts)
        val regJson = reg.toJson(String::class)

        regJson.shouldBe("""{"_type":"LWWRegister","_metadata":{"uid":{"name":"dcid"},"cnt":-2147483648},"value":"value"}""")
    }

    /**
    * This test evaluates JSON deserialization of a lww register.
    **/
    "JSON deserialization" {
        val regJson = LWWRegister.fromJson(String::class, """{"_type":"LWWRegister","_metadata":{"uid":{"name":"dcid"},"cnt":-2147483648},"value":"value"}""")

        regJson.get().shouldBe("value")
    }
})
