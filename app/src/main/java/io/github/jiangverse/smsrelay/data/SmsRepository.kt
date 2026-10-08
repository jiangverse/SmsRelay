package io.github.jiangverse.smsrelay.data

import io.github.jiangverse.smsrelay.R
import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.github.jiangverse.smsrelay.domain.SmsEnvelope
import kotlinx.coroutines.flow.MutableStateFlow

data class RelayRecord(val id: String, val sms: SmsEnvelope, val state: String, val detail: String)

class SmsRepository(private val context: Context) {
    private val helper = object : SQLiteOpenHelper(context, "relay.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE messages (id TEXT PRIMARY KEY, sender TEXT NOT NULL, body TEXT NOT NULL, timestamp INTEGER NOT NULL, subscription INTEGER NOT NULL, state TEXT NOT NULL, detail TEXT NOT NULL)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }
    val records = MutableStateFlow(list())
    @Synchronized
    fun accept(sms: SmsEnvelope, id: String = sms.fingerprint()): Boolean {
        val values = ContentValues().apply {
            put("id", id); put("sender", sms.sender); put("body", sms.body)
            put("timestamp", sms.timestamp); put("subscription", sms.subscription)
            put("state", context.getString(R.string.pending)); put("detail", "")
        }
        val inserted = helper.writableDatabase.insertWithOnConflict("messages", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        refresh()
        return inserted
    }
    fun get(id: String): RelayRecord? = helper.readableDatabase.rawQuery("SELECT * FROM messages WHERE id = ?", arrayOf(id)).use { cursor ->
        if (!cursor.moveToFirst()) null else record(cursor)
    }
    fun pending(): List<RelayRecord> = list().filter { it.state == context.getString(R.string.pending) || it.state == context.getString(R.string.retrying) }
    fun update(id: String, state: String, detail: String = "") {
        helper.writableDatabase.update("messages", ContentValues().apply { put("state", state); put("detail", detail) }, "id = ?", arrayOf(id))
        // 保留最近 200 条已完成记录，尚未送达的消息不清理。
        helper.writableDatabase.execSQL("DELETE FROM messages WHERE state NOT IN (?, ?) AND id NOT IN (SELECT id FROM messages ORDER BY timestamp DESC LIMIT 200)", arrayOf(context.getString(R.string.pending), context.getString(R.string.retrying)))
        refresh()
    }
    private fun refresh() { records.value = list() }
    private fun list(): List<RelayRecord> = helper.readableDatabase.rawQuery("SELECT * FROM messages ORDER BY timestamp DESC", null).use { cursor ->
        buildList { while (cursor.moveToNext()) add(record(cursor)) }
    }
    private fun record(cursor: android.database.Cursor) = RelayRecord(cursor.getString(0),
        SmsEnvelope(cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4)), cursor.getString(5), cursor.getString(6))
}
