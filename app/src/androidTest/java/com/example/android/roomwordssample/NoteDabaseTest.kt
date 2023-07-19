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

    fun <T> getLiveDataValue(liveData: LiveData<T>): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
        // Wait for the value to be set or timeout after 2 seconds
        latch.await(2, TimeUnit.SECONDS)
        return data as T
    }

    @Test
    fun insertAndDeleteNoteTest() = runBlocking {
        val note = Note("1", "Test Description","19,Jul, 2023-22-01")
        noteDatabase.getNotesDao().insert(note)

        val allNotes = noteDatabase.getNotesDao().getAllNotes().value
        assertThat(allNotes!!.size, equalTo(1))
        assertThat(allNotes[0].id, equalTo(note.id))
        assertThat(allNotes[0].noteTitle, equalTo(note.noteTitle))
        assertThat(allNotes[0].noteDescription, equalTo(note.noteDescription))
        assertThat(allNotes[0].timeStamp, equalTo(note.timeStamp))

        noteDatabase.getNotesDao().delete(note)

        val deletedNote = noteDatabase.getNotesDao().delete(note)
        assertThat(deletedNote, equalTo(null))
    }

    //unit tests for delete, update, and getAllNotes operations.
}
