package com.js.nowakelock.data.db.dao



//class WakeLockDaoTest {
//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var dao: WakeLockDao
//    private lateinit var db: AppDatabase
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(
//            context, AppDatabase::class.java
//        ).allowMainThreadQueries()
//            .build()
//        dao = db.wakeLockDao()
//    }
//
//    @After
//    fun tearDown() {
//        db.close()
//    }
//
//    @Test
//    fun testInfo() {
//        runBlocking {
//            dao.insert(TestData.wakeLocks)
//        }
//        val a = runBlocking { dao.loadWakeLocks().asLiveData() }
//        val b = runBlocking { dao.loadWakeLocks(TestData.pN).asLiveData() }
//        val c = runBlocking { dao.loadWakeLock("w1") }
//        val d = runBlocking { dao.loadAllWakeLocks() }
//
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 10)
//        Assert.assertEquals(LiveDataTestUtil.getValue(b).size, 10)
//        Assert.assertEquals(c!!.count, 1)
//        Assert.assertEquals(d.size, 10)
//
//        runBlocking {
//            dao.delete(TestData.wakeLocks[0])
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 9)
//
//        runBlocking {
//            dao.delete(TestData.wakeLocks)
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 0)
//    }
//
//    @Test
//    fun testST() {
//        runBlocking {
//            dao.insertST(TestData.wakeLockSt)
//        }
//        val a = runBlocking { dao.loadWakeLockSt() }
//        val b = runBlocking { dao.loadWakeLockSt("test") }
//
//        Assert.assertEquals(a.size, 1)
//        Assert.assertEquals(b!!.flag, false)
//        Assert.assertEquals(b.allowTimeinterval, 10)
//    }
//}