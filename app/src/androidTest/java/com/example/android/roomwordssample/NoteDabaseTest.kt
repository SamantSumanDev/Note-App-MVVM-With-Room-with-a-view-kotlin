package com.example.android.roomwordssample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NoteDatabaseTest {

    private lateinit var noteDatabase: NoteDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        noteDatabase = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java).build()
    }

    @After
    fun cleanup() {
        noteDatabase.close()
    }
    //unit tests for delete, update, and getAllNotes operations.


    @Test
    fun insertAndDeleteNoteTest() = runBlocking {
        val note = Note("1", "Test Description","19,Jul, 2023-22-01")
        noteDatabase.getNotesDao().insert(note)

        val allNotes = noteDatabase.getNotesDao().getAllNotes()
        val initialSize = allNotes.getOrAwaitValue().size
        assertThat(initialSize, equalTo(1))

        noteDatabase.getNotesDao().delete(note)

        val updatedSize = allNotes.getOrAwaitValue().size
        assertThat(updatedSize, equalTo(0))
    }



}

private fun <T> LiveData<T>.getOrAwaitValue(): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)
    return data as T
}

