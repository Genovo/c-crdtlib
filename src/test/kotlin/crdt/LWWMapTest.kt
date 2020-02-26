package crdtlib.test

import crdtlib.crdt.Delta
import crdtlib.crdt.LWWMap
import crdtlib.utils.DCId
import crdtlib.utils.SimpleEnvironment
import crdtlib.utils.VersionVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
* Represents a suite test for LWWMap.
**/
class LWWMapTest {
    /**
    * This test evaluates the scenario: get.
    * Call to get should return null
    */
    @Test
    fun emptyGet() {
        val key = "key"
        val map = LWWMap()

        assertNull(map.get(key))
    }
    
    /**
    * This test evaluates the scenario: put get.
    * Call to get should return the value set by the put.
    */
    @Test
    fun putGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts = dc.getNewTimestamp()
        val key = "key"
        val value = "value"
        val map = LWWMap()

        map.put(key, value, ts)

        assertEquals(value, map.get(key))
    }
    
    /**
    * This test evaluates the scenario: put del get.
    * Call to get should return null.
    */
    @Test
    fun putDelGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        val key = "key"
        val value = "value"
        val map = LWWMap()

        map.put(key, value, ts1)
        map.delete(key, ts2)

        assertNull(map.get(key))
    }
 
    /**
    * This test evaluates the scenario: del get.
    * Call to get should return null.
    */
    @Test
    fun delGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        val key = "key"
        val map = LWWMap()

        map.delete(key, ts1)

        assertNull(map.get(key))
    }

    /**
    * This test evaluates the scenario: put put get
    * Call to get should return the value set by the second put.
    */
    @Test
    fun putPutGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map = LWWMap()

        map.put(key, val1, ts1)
        map.put(key, val2, ts2)

        assertEquals(val2, map.get(key))
    }

    /**
    * This test evaluates the scenario: put put del get.
    * Call to get should return null.
    */
    @Test
    fun putPutDelGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        dc.updateStateTS(ts2)
        val ts3 = dc.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map = LWWMap()

        map.put(key, val1, ts1)
        map.put(key, val2, ts2)
        map.delete(key, ts3)

        assertNull(map.get(key))
    }

    /**
    * This test evaluates the scenario: put || merge get.
    * Call to get should return the value set by the put registered in the first replica.
    */
    @Test
    fun put_MergeGet() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts = dc.getNewTimestamp()
        val key = "key"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key, value, ts)
        map1.merge(map2)
        map2.merge(map1)

        assertEquals(value, map1.get(key))
        assertEquals(value, map2.get(key))
    }

    /**
    * This test evaluates the scenario: put || merge putLWW get.
    * Call to get should return the value set by put registered in the second replica.
    */
    @Test
    fun put_MergePutLWWGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key, val1, ts1)
        map2.merge(map1)
        map2.put(key, val2, ts2)

        assertEquals(val2, map2.get(key))
    }

    /**
    * This test evaluates the scenario: put || putLWW merge get
    * Call to get should return the value set by put registered in the second replica.
    */
    @Test
    fun put_PutLWWMergeGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key, val1, ts1)
        map2.put(key, val2, ts2)
        map2.merge(map1)

        assertEquals(val2, map2.get(key))
    }
    
    /**
    * This test evaluates the scenario: putLWW || put merge get.
    * Call to get should return the value set by put registered in the first replica.
    */
    @Test
    fun putLWW_PutMergeGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map2.put(key, val2, ts1)
        map1.put(key, val1, ts2)
        map2.merge(map1)

        assertEquals(val1, map2.get(key))
    }

    /**
    * This test evaluates the scenario: put delLWW || put merge get.
    * Call to get should return null.
    */
    @Test
    fun putDelLWW_PutMergeGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts3 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map2.put(key, val2, ts1)
        map1.put(key, val1, ts2)
        map1.delete(key, ts3)
        map2.merge(map1)

        assertNull(map2.get(key))
    }

    /**
    * This test evaluates the scenario: put delLWW || put merge(before del) merge(after del) get.
    * Call to get should return null.
    */
    @Test
    fun putDelLWW_PutMergeBeforeDelMergeAfterDelGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts3 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map2.put(key, val2, ts1)
        map1.put(key, val1, ts2)
        map2.merge(map1)
        map1.delete(key, ts3)
        map2.merge(map1)

        assertNull(map2.get(key))
    }

    /*
    * This test evaluates the scenario: put del || putLWW merge get.
    * Call to get should return the value set by put registered in the second replica.
    */
    @Test
    fun putDel_PutLWWMergeGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val ts3 = dc1.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts4 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key, val1, ts1)
        map1.delete(key, ts3)
        map2.put(key, val2, ts4)
        map2.merge(map1)

        assertEquals(val2, map2.get(key))
    }

    /*
    * This test evaluates the scenario: put del || putLWW merge(before del) merge(after del) get.
    * Call to get should return the value set by put registered in the second replica.
    */
    @Test
    fun putDel_PutLWWMergeBeforeDelMergeAfterDelGet() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val ts3 = dc1.getNewTimestamp()
        dc2.updateStateTS(ts2)
        val ts4 = dc2.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key, val1, ts1)
        map2.put(key, val2, ts4)
        map2.merge(map1)
        map1.delete(key, ts3)
        map2.merge(map1)

        assertEquals(val2, map2.get(key))
    }

    /*
    * This test evaluates the scenario: put || put || merge1 delLWW merge2 get.
    * Call to get should return null.
    */
    @Test
    fun put_Put_Merge1DelLWWMerge2Get() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val id3 = DCId("dcid3")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val dc3 = SimpleEnvironment(id3)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val ts3 = dc3.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()
        val map3 = LWWMap()

        map1.put(key, val1, ts1)
        map3.merge(map1)
        map2.put(key, val2, ts2)
        map3.delete(key, ts3)
        map3.merge(map2)

        assertNull(map3.get(key))
    }

    /*
    * This test evaluates the scenario: putLWW || put || merge1 del merge2 get.
    * Call to get should return the value set by put registered in the second replica.
    */
    @Test
    fun put_PutLWW_Merge1DelMerge2Get() {
        val id1 = DCId("dcid1")
        val id2 = DCId("dcid2")
        val id3 = DCId("dcid3")
        val dc1 = SimpleEnvironment(id1)
        val dc2 = SimpleEnvironment(id2)
        val dc3 = SimpleEnvironment(id3)
        val ts1 = dc1.getNewTimestamp()
        val ts2 = dc2.getNewTimestamp()
        val ts3 = dc3.getNewTimestamp()
        val key = "key"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()
        val map3 = LWWMap()

        map1.put(key, val1, ts1)
        map3.merge(map1)
        map3.delete(key, ts2)
        map2.put(key, val2, ts3)
        map3.merge(map2)

        assertEquals(val2, map3.get(key))
    }

    /*
    * This test evaluates the use of deltas return by call to put method.
    * Call to get should return the value set by put registered in the first replica.
    */
    @Test
    fun putOp() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts = dc.getNewTimestamp()
        val key = "key"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        val op = map1.put(key, value, ts)
        map1.merge(op)
        map2.merge(op)

        assertEquals(value, map1.get(key))
        assertEquals(value, map2.get(key))
    }

    /*
    * This test evaluates the use of deltas return by call to put and delete methods.
    * Call to get should return null.
    */
    @Test
    fun putDelOp() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        val key = "key"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        val putOp = map1.put(key, value, ts1)
        val delOp = map1.delete(key, ts2)
        map1.merge(putOp)
        map1.merge(delOp)
        map2.merge(putOp)
        map2.merge(delOp)

        assertNull(map1.get(key))
        assertNull(map2.get(key))
    }

    /*
    * This test evaluates the merge of deltas return by call to put method.
    * Call to get should return the values set by puts registered in the first replica.
    */
    @Test
    fun putOpFusion() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        dc.updateStateTS(ts2)
        val ts3 = dc.getNewTimestamp()
        val key1 = "key1"
        val key2 = "key2"
        val val1 = "value1"
        val val2 = "value2"
        val map1 = LWWMap()
        val map2 = LWWMap()

        val op1 = map1.put(key1, val1, ts1)
        val op2 = map1.put(key1, val2, ts2)
        val op3 = map1.put(key2, val1, ts3)
        op2.merge(op3)
        op1.merge(op2)
        map2.merge(op1)

        assertEquals(val2, map2.get(key1))
        assertEquals(val1, map2.get(key2))
    }

    /*
    * This test evaluates the merge of deltas return by call to put and delete methods.
    * Call to get should return the value set by put or null if it has been deleted.
    */
    @Test
    fun putDelOpFusion() {
        val id = DCId("dcid")
        val dc = SimpleEnvironment(id)
        val ts1 = dc.getNewTimestamp()
        dc.updateStateTS(ts1)
        val ts2 = dc.getNewTimestamp()
        dc.updateStateTS(ts2)
        val ts3 = dc.getNewTimestamp()
        val key1 = "key1"
        val key2 = "key2"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        val op1 = map1.put(key1, value, ts1)
        val op2 = map1.delete(key1, ts2)
        val op3 = map1.put(key2, value, ts3)
        op2.merge(op3)
        op1.merge(op2)
        map2.merge(op1)

        assertNull(map2.get(key1))
        assertEquals(value, map2.get(key2))
    }

    /*
    * This test evaluates the generation of delta (only put) plus its merging into another replica.
    * Call to get should return the values set by puts registered in the first replica after w.r.t
    * the given context.
    */
    @Test
    fun generateDelta() {
        val id1 = DCId("dcid1")
        val dc1 = SimpleEnvironment(id1)
        val ts1 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val ts2 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts2)
        val ts3 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts3)
        val ts4 = dc1.getNewTimestamp()
        val vv = VersionVector()
        vv.addTS(ts2)
        val key1 = "key1"
        val key2 = "key2"
        val key3 = "key3"
        val key4 = "key4"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key1, value, ts1)
        map1.put(key2, value, ts2)
        map1.put(key3, value, ts3)
        map1.put(key4, value, ts4)
        val delta = map1.generateDelta(vv)
        map2.merge(delta)

        assertNull(map2.get(key1))
        assertNull(map2.get(key2))
        assertEquals(value, map2.get(key3))
        assertEquals(value, map2.get(key4))
    }

    /*
    * This test evaluates the generation of delta (including delete) plus its merging into another replica.
    * Call to get should return the values set by puts or null set by delete w.r.t the given context.
    */
    @Test
    fun generateDeltaWithDel() {
        val id1 = DCId("dcid1")
        val dc1 = SimpleEnvironment(id1)
        val ts1 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts1)
        val ts2 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts2)
        val ts3 = dc1.getNewTimestamp()
        dc1.updateStateTS(ts3)
        val ts4 = dc1.getNewTimestamp()
        val vv = VersionVector()
        vv.addTS(ts1)
        val key1 = "key1"
        val key2 = "key2"
        val key3 = "key3"
        val value = "value"
        val map1 = LWWMap()
        val map2 = LWWMap()

        map1.put(key1, value, ts1)
        map1.put(key2, value, ts2)
        map1.delete(key2, ts3)
        map1.put(key3, value, ts4)
        val delta = map1.generateDelta(vv)
        map2.merge(delta)

        assertNull(map2.get(key1))
        assertNull(map2.get(key2))
        assertEquals(value, map2.get(key3))
    }
}