package com.luminor.tavernquest.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE daily_contract_new (
                id TEXT NOT NULL PRIMARY KEY, templateId TEXT NOT NULL, heroId TEXT NOT NULL,
                date TEXT NOT NULL, status TEXT NOT NULL, acceptedAt INTEGER, completedAt INTEGER, startedAt INTEGER)""")
            // A v1 installation has one player. Prefer the recorded completion owner when present.
            db.execSQL("""INSERT INTO daily_contract_new
                SELECT d.id,d.templateId,COALESCE(
                    (SELECT heroId FROM quest_completion WHERE dailyContractId=d.id LIMIT 1),
                    (SELECT id FROM hero ORDER BY createdAt LIMIT 1),''),
                    d.date,d.status,d.acceptedAt,d.completedAt,NULL FROM daily_contract d""")
            db.execSQL("DROP TABLE daily_contract")
            db.execSQL("ALTER TABLE daily_contract_new RENAME TO daily_contract")
            db.execSQL("CREATE INDEX index_daily_contract_date ON daily_contract(date)")
            db.execSQL("CREATE INDEX index_daily_contract_heroId ON daily_contract(heroId)")
            db.execSQL("""CREATE TABLE check_in (
                id TEXT NOT NULL PRIMARY KEY, dailyContractId TEXT NOT NULL, heroId TEXT NOT NULL,
                notes TEXT NOT NULL, proofPhotoPath TEXT, completedAt INTEGER NOT NULL,
                title TEXT NOT NULL, category TEXT NOT NULL, xpEarned INTEGER NOT NULL,
                startedAt INTEGER, durationSeconds INTEGER NOT NULL, activityDate TEXT NOT NULL, syncStatus TEXT NOT NULL)""")
            // Old activity has no recorded timer; never invent elapsed time or republish old XP.
            db.execSQL("""INSERT INTO check_in
                SELECT q.id,q.dailyContractId,q.heroId,q.notes,q.proofPhotoPath,q.completedAt,
                COALESCE(t.title,'Missão concluída'),COALESCE(t.category,'STRENGTH_DISCIPLINE'),
                COALESCE((SELECT amount FROM xp_ledger WHERE sourceType='QUEST' AND sourceId=q.dailyContractId),0),
                NULL,0,date(q.completedAt/1000,'unixepoch','localtime'),'LOCAL_ONLY'
                FROM quest_completion q LEFT JOIN daily_contract d ON d.id=q.dailyContractId
                LEFT JOIN contract_template t ON t.id=d.templateId""")
            db.execSQL("CREATE UNIQUE INDEX index_check_in_dailyContractId ON check_in(dailyContractId)")
            db.execSQL("CREATE INDEX index_check_in_heroId_activityDate ON check_in(heroId,activityDate)")
            db.execSQL("DROP TABLE quest_completion")
            db.execSQL("""CREATE TABLE user_stats (heroId TEXT NOT NULL PRIMARY KEY,
                totalCheckIns INTEGER NOT NULL,totalXp INTEGER NOT NULL,activeDays INTEGER NOT NULL,activeSeconds INTEGER NOT NULL)""")
            db.execSQL("""CREATE TABLE user_activity_day (heroId TEXT NOT NULL,date TEXT NOT NULL,
                checkInCount INTEGER NOT NULL,totalXp INTEGER NOT NULL,activeSeconds INTEGER NOT NULL,
                primaryCategory TEXT,thumbnailUrl TEXT,PRIMARY KEY(heroId,date))""")
            db.execSQL("""CREATE TABLE pending_sync (checkInId TEXT NOT NULL PRIMARY KEY,
                createdAt INTEGER NOT NULL,attempts INTEGER NOT NULL,lastError TEXT)""")
            db.execSQL("""INSERT INTO user_stats SELECT heroId,COUNT(*),SUM(xpEarned),COUNT(DISTINCT activityDate),SUM(durationSeconds)
                FROM check_in GROUP BY heroId""")
            db.execSQL("""INSERT INTO user_activity_day SELECT heroId,activityDate,COUNT(*),SUM(xpEarned),SUM(durationSeconds),MAX(category),MAX(proofPhotoPath)
                FROM check_in GROUP BY heroId,activityDate""")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS tavern_feed (
                tavernId TEXT NOT NULL,
                checkInId TEXT NOT NULL,
                publishedAt INTEGER NOT NULL,
                PRIMARY KEY(tavernId, checkInId),
                FOREIGN KEY(checkInId) REFERENCES check_in(id) ON DELETE CASCADE
            )""")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tavern_feed_checkInId ON tavern_feed(checkInId)")
            // Preserve existing history for memberships that already existed when v3 was installed.
            db.execSQL("""INSERT OR IGNORE INTO tavern_feed(tavernId, checkInId, publishedAt)
                SELECT m.tavernId, c.id, c.completedAt
                FROM tavern_member m INNER JOIN check_in c ON c.heroId = m.heroId
                WHERE c.completedAt >= m.joinedAt""")
        }
    }
}
