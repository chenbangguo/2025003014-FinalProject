package com.example.courseschedule.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.courseschedule.data.dao.AssignmentDao
import com.example.courseschedule.data.dao.CourseDao
import com.example.courseschedule.data.dao.NotificationDao
import com.example.courseschedule.data.entity.AssignmentEntity
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CourseEntity::class, AssignmentEntity::class, NotificationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "course_schedule.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build().also { INSTANCE = it }
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedCourses(database.courseDao())
                    seedAssignments(database.assignmentDao())
                    seedNotifications(database.notificationDao())
                }
            }
        }
    }
}

private suspend fun seedCourses(dao: CourseDao) {
    val courses = listOf(
        CourseEntity(name = "高等数学", teacher = "李教授", classroom = "教一 301", dayOfWeek = 1, startPeriod = 1, duration = 2, weeks = "1-16", color = 0xFFE53935.toInt(), note = "带教材和练习册"),
        CourseEntity(name = "大学英语", teacher = "王老师", classroom = "教二 205", dayOfWeek = 1, startPeriod = 3, duration = 2, weeks = "1-16", color = 0xFF1E88E5.toInt()),
        CourseEntity(name = "数据结构", teacher = "张教授", classroom = "教三 102", dayOfWeek = 2, startPeriod = 1, duration = 3, weeks = "1-16", color = 0xFF43A047.toInt(), note = "机房上课"),
        CourseEntity(name = "线性代数", teacher = "赵老师", classroom = "教一 401", dayOfWeek = 2, startPeriod = 5, duration = 2, weeks = "1-8,10-16", color = 0xFFFB8C00.toInt()),
        CourseEntity(name = "体育课", teacher = "陈教练", classroom = "体育馆", dayOfWeek = 3, startPeriod = 3, duration = 2, weeks = "1-16", color = 0xFF8E24AA.toInt(), note = "穿运动服"),
        CourseEntity(name = "移动开发", teacher = "刘教授", classroom = "教三 508", dayOfWeek = 3, startPeriod = 6, duration = 3, weeks = "1-16", color = 0xFF00ACC1.toInt(), note = "带笔记本电脑"),
        CourseEntity(name = "操作系统", teacher = "周教授", classroom = "教一 201", dayOfWeek = 4, startPeriod = 1, duration = 2, weeks = "1-16", color = 0xFFD81B60.toInt()),
        CourseEntity(name = "数据库原理", teacher = "吴老师", classroom = "教二 303", dayOfWeek = 4, startPeriod = 5, duration = 2, weeks = "1-12", color = 0xFF3949AB.toInt(), note = "实验课在14-16周"),
        CourseEntity(name = "马克思主义原理", teacher = "孙教授", classroom = "教一 101", dayOfWeek = 5, startPeriod = 1, duration = 3, weeks = "1-16", color = 0xFF6D4C41.toInt()),
        CourseEntity(name = "大学物理", teacher = "杨教授", classroom = "教二 401", dayOfWeek = 5, startPeriod = 5, duration = 2, weeks = "1-16", color = 0xFF546E7A.toInt(), note = "带实验报告"),
    )
    courses.forEach { dao.insert(it) }
}

private suspend fun seedAssignments(dao: AssignmentDao) {
    val now = System.currentTimeMillis()
    val day = 86400000L
    val assignments = listOf(
        AssignmentEntity(courseId = 1, title = "高等数学作业 Ch3", description = "完成习题3.1-3.5", dueDate = now + 2 * day, isCompleted = false, priority = 1),
        AssignmentEntity(courseId = 3, title = "数据结构实验报告", description = "实现二叉树遍历算法并撰写实验报告", dueDate = now + 3 * day, isCompleted = false, priority = 2),
        AssignmentEntity(courseId = 6, title = "移动开发大作业", description = "设计并实现完整Android应用", dueDate = now + 14 * day, isCompleted = false, priority = 2),
        AssignmentEntity(courseId = 8, title = "数据库ER图设计", description = "设计学生选课系统的ER图", dueDate = now + 1 * day, isCompleted = false, priority = 1),
        AssignmentEntity(courseId = 2, title = "英语作文", description = "写一篇关于AI的英语短文300词", dueDate = now + 4 * day, isCompleted = false, priority = 0),
        AssignmentEntity(courseId = 4, title = "线性代数习题", description = "完成第三章矩阵运算", dueDate = now - 1 * day, isCompleted = true, priority = 0),
    )
    assignments.forEach { dao.insert(it) }
}

private suspend fun seedNotifications(dao: NotificationDao) {
    val now = System.currentTimeMillis()
    val notifications = listOf(
        NotificationEntity(title = "📢 期中考试安排", content = "高等数学期中考试定于第8周周一上午。请同学们提前复习第一至四章内容，考试地点：教一301。", type = "exam", createdAt = now - 86400000 * 3, isPinned = true),
        NotificationEntity(title = "🎉 校园科技节活动", content = "第10周将举办校园科技节，欢迎各位同学报名参加移动应用开发竞赛，报名截止第9周周五。", type = "activity", createdAt = now - 86400000 * 5, isPinned = true),
        NotificationEntity(title = "📋 数据结构的机房调整", content = "从第3周起，数据结构课程机房由教三102调整为教三205，请同学们相互转告。", type = "notice", createdAt = now - 86400000 * 7, isPinned = false),
        NotificationEntity(title = "⚠ 实验报告提交提醒", content = "请各位同学于本周五前提交大学物理实验报告，逾期将影响平时成绩。", type = "notice", createdAt = now - 86400000 * 1, isPinned = false),
        NotificationEntity(title = "🏆 移动开发大作业通知", content = "期末大作业要求已发布，请查看GitHub仓库和实验报告模板。截止时间：第18周结束前。", type = "notice", createdAt = now, isPinned = false),
    )
    notifications.forEach { dao.insert(it) }
}
