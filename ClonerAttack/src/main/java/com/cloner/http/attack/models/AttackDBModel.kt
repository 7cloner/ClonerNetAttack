package com.cloner.http.attack.models

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import okhttp3.Cookie
import java.lang.Exception
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class AttackDBModel<T : AttackModel>
    (private val context: Context, private val clazz: Class<T>, private val version: Int = 1) :
    AttackModel() {

    private val dbHelper: SQLiteOpenHelper by lazy {
        MyDBHelper()
    }
    internal open var useTimestamp: Boolean = true
    var createdAt = ""

    init {
        if (useTimestamp) {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            createdAt = dateFormat.format(calendar.time)
        }
    }


    private fun getTableName(): String = clazz.simpleName ?: "UnknownTable"

    fun get(
        columns: List<String>, wheres: List<DBWhere>, orderBy: String = "ASC",
        limit: Int? = null
    ): List<T> {
        return fetchItems(makeQuery(columns, wheres, orderBy, limit))
    }

    fun find(columns: List<String>, wheres: List<DBWhere>, orderBy: String = "ASC"): T? {
        val items = get(columns, wheres, orderBy, 1)
        if (items.isNotEmpty()) {
            return items[0]
        }
        return null
    }

    fun delete(where: String) {
        dbHelper.writableDatabase.delete(getTableName(), where, null)
    }

    fun save() {
        val values = ContentValues()
        val fields = clazz.declaredFields
        try {
            for (field in fields) {
                val fieldName = field.name
                if (Modifier.isStatic(field.modifiers) ||
                    fieldName == "\$stable" || fieldName == "Companion") {
                    continue
                }

                field.isAccessible = true
                var fieldValue = field.get(this)
                if (!(fieldValue is Int || fieldValue is String || fieldValue is Number)) {
                    if (fieldValue is Boolean) {
                        fieldValue = if (fieldValue) 1 else 0
                    } else if (fieldValue is Cookie) {
                        fieldValue = encodeCookie(fieldValue)
                    }
                }

                values.put(fieldName, fieldValue?.toString())
            }
        } catch (_: IllegalAccessException) {
        }

        dbHelper.writableDatabase.insert(getTableName(), "", values)
    }

    @SuppressLint("Range")
    private fun fetchItems(query: String): List<T> {
        val cursor = dbHelper.readableDatabase.rawQuery(query, null)
        val items: MutableList<T> = ArrayList()
        if (cursor.moveToFirst()) {
            val fields = clazz.declaredFields
            do {
                val item = buildTInstance()
                try {
                    for (field in fields) {
                        try {
                            val fieldName = field.name
                            if (Modifier.isStatic(field.modifiers) ||
                                fieldName == "\$stable" || fieldName == "Companion") {
                                continue
                            }
                            field.isAccessible = true
                            val fieldValue = field.get(item)
                            when (fieldValue) {
                                is Int -> {
                                    field.set(item, cursor.getInt(cursor.getColumnIndex(fieldName)))
                                }

                                is Number -> {
                                    field.set(item, cursor.getDouble(cursor.getColumnIndex(fieldName)))
                                }

                                is Boolean -> {
                                    field.set(
                                        item,
                                        cursor.getInt(cursor.getColumnIndex(fieldName)) == 1
                                    )
                                }

                                is String -> {
                                    val ind = cursor.getColumnIndex(fieldName)
                                    val str = cursor.getString(ind)
                                    field.set(item, str)
                                }

                                is Cookie -> {
                                    field.set(
                                        item,
                                        decodeCookie(cursor.getString(cursor.getColumnIndex(fieldName)))
                                    )
                                }
                            }
                        } catch (_: Exception) {
                            continue
                        }
                    }
                } catch (_: IllegalAccessException) {
                    continue
                }
                items.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return items
    }

    private fun buildTInstance(): T {
        return clazz.getConstructor(Context::class.java).newInstance(context)
    }

    private fun makeQuery(
        columns: List<String>, wheres: List<DBWhere>,
        orderBy: String = "ASC", limit: Int? = null
    ): String {
        val query = StringBuilder()
        query.append("SELECT ")
        query.append(columns.joinToString(", "))
        query.append(" FROM ")
        query.append(getTableName())
        if(wheres.isNotEmpty()){
            query.append(" WHERE (")
            wheres.forEach { item ->
                query.append(item.buildQuery())
            }
            query.append(")")
        }
        query.append(" ORDER BY db_id $orderBy")
        if (limit != null && limit > 0) {
            query.append(" LIMIT $limit")
        }

        return query.toString()
    }

    internal fun getFields(): Map<String, String> {
        val fieldsMap = HashMap<String, String>()
        val instance = buildTInstance()
        val fields = clazz.declaredFields
        try {
            for (field in fields) {
                val fieldName = field.name
                if (Modifier.isStatic(field.modifiers) ||
                    fieldName == "\$stable" || fieldName == "Companion") {
                    continue
                } else if (useTimestamp && fieldName == "createAt") {
                    fieldsMap[fieldName] = "DATETIME"
                }

                field.isAccessible = true
                val fieldValue = field.get(instance)
                fieldsMap[fieldName] = when (fieldValue) {
                    is Int -> "INT"
                    is Number -> "DOUBLE"
                    is Boolean -> "BOOLEAN"
                    else -> "TEXT"
                }
            }
        } catch (_: IllegalAccessException) {
        }

        return fieldsMap
    }

    private inner class MyDBHelper() :
        SQLiteOpenHelper(context.applicationContext, "cloner_attack.db", null, version) {

        private fun buildCreateQuery(): String {
            val query = StringBuilder()
            query.append("CREATE TABLE IF NOT EXISTS `")
            query.append(getTableName())
            query.append("` (db_id INTEGER PRIMARY KEY AUTOINCREMENT")

            val fields = getFields()
            fields.keys.forEach { key ->
                query.append(", ")
                query.append(key)
                query.append(" ")
                query.append(fields[key])
            }
            query.append(")")
            return query.toString()
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(buildCreateQuery())
        }

        override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
            db.execSQL("DROP TABLE IF EXISTS " + getTableName())
            onCreate(db)
        }
    }
}