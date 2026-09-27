package com.noteremind.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/** 本地数据仓库：JSON 文件持久化 + StateFlow 驱动 UI */
object AppRepo {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private lateinit var dataFile: File

    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    fun init(context: Context) {
        dataFile = File(context.filesDir, "reminder_data.json")
        if (dataFile.exists()) {
            runCatching {
                _data.value = json.decodeFromString<AppData>(dataFile.readText())
            }
        }
    }

    private fun persist() {
        runCatching { dataFile.writeText(json.encodeToString(_data.value)) }
    }

    fun update(block: (AppData) -> AppData) {
        _data.value = block(_data.value)
        persist()
    }

    fun current(): AppData = _data.value

    fun newId(): String = UUID.randomUUID().toString()

    // ---------- 提醒事项 ----------

    fun upsertReminder(item: ReminderItem) {
        update { d ->
            val list = d.reminders.toMutableList()
            val idx = list.indexOfFirst { it.id == item.id }
            if (idx >= 0) list[idx] = item else list.add(item)
            d.copy(reminders = list)
        }
    }

    fun deleteReminder(id: String) {
        update { d -> d.copy(reminders = d.reminders.filterNot { it.id == id }) }
    }

    fun saveReminderOrder(orderedIds: List<String>) {
        update { d ->
            val map = orderedIds.withIndex().associate { (i, id) -> id to i }
            d.copy(reminders = d.reminders.map { it.copy(order = map[it.id] ?: it.order) })
        }
    }

    // ---------- 人生轨迹归属（多人） ----------

    fun upsertOwner(owner: TrackOwner) {
        update { d ->
            val list = d.owners.toMutableList()
            val idx = list.indexOfFirst { it.id == owner.id }
            if (idx >= 0) list[idx] = owner else list.add(owner)
            d.copy(owners = list)
        }
    }

    fun ownerById(id: String): TrackOwner? = _data.value.owners.find { it.id == id }

    // ---------- 人生轨迹 ----------

    fun addLifeEvent(event: LifeEvent) {
        update { d -> d.copy(lifeEvents = d.lifeEvents + event) }
    }

    fun updateLifeEvent(event: LifeEvent) {
        update { d ->
            d.copy(lifeEvents = d.lifeEvents.map { if (it.id == event.id) event else it })
        }
    }

    fun deleteLifeEvent(id: String) {
        update { d -> d.copy(lifeEvents = d.lifeEvents.filterNot { it.id == id }) }
    }

    // ---------- 纪念日 ----------

    fun upsertAnniversary(item: Anniversary) {
        update { d ->
            val list = d.anniversaries.toMutableList()
            val idx = list.indexOfFirst { it.id == item.id }
            if (idx >= 0) list[idx] = item else list.add(item)
            d.copy(anniversaries = list)
        }
    }

    fun deleteAnniversary(id: String) {
        update { d -> d.copy(anniversaries = d.anniversaries.filterNot { it.id == id }) }
    }

    // ---------- 生日 ----------

    fun setBirthday(millis: Long?, lunar: Boolean) {
        update { d -> d.copy(birthdayMillis = millis, birthdayLunar = lunar) }
    }

    // ---------- 备份 / 恢复 ----------

    fun exportJson(): String = json.encodeToString(_data.value)

    fun importJson(text: String): Boolean {
        return runCatching {
            val parsed = json.decodeFromString<AppData>(text)
            _data.value = parsed
            persist()
            true
        }.getOrDefault(false)
    }
}
