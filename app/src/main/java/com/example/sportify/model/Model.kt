package com.example.sportify.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.sportify.base.EmptyCallback
import com.example.sportify.base.GamesCallback
import com.example.sportify.base.StudentsCallback
import com.example.sportify.model.dao.AppLocalDb
import com.example.sportify.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executer = Executors.newSingleThreadExecutor()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllStudents(callback: StudentsCallback) {
        executer.execute {
            val students = database.studentDao().getAllStudent()

            mainHandler.post {
                callback(students)
            }
        }
    }

    fun getAllGames(callback: GamesCallback) {
        executer.execute {
            val games = database.gamesDao().getAllGames()

            mainHandler.post {
                callback(games)
            }
        }
    }

    fun addStudent(student: Student, callback: EmptyCallback) {
        executer.execute {
            database.studentDao().insertAll(student)

            mainHandler.post {
                callback()
            }
        }
    }

    fun addGame(game: Game, callback: EmptyCallback) {
        executer.execute {
            database.gamesDao().insertAll(game)

            mainHandler.post {
                callback()
            }
        }
    }

    fun delete(student: Student, callback: EmptyCallback) {
        executer.execute {
            database.studentDao().delete(student)

            mainHandler.post {
                callback()
            }
        }
    }
}