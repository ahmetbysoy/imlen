package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "precision_cursor_db"
                ).addCallback(DatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDocuments(database.documentDao())
                    }
                }
            }

            suspend fun populateInitialDocuments(dao: DocumentDao) {
                dao.insertDocument(
                    DocumentEntity(
                        title = "Hassas Metin Düzenleme & Test Paragrafı",
                        category = "Pratik",
                        content = """Dokunmatik ekranlarda metin düzenlemek artık işkence olmaktan çıkıyor!

Geleneksel dokunmatik kontrollerde parmağımızla tıkladığımız kelimenin tam arasına imleci yerleştirmek oldukça zordur. Özellikle "ş", "ı", "ğ", "ü", "ö", "ç" gibi Türkçe karakterlerin ve noktalama işaretlerinin (parantez [köşeli], tırnak "alıntı", soru işareti?) arasına imleç oturtmak büyük hassasiyet gerektirir.

Mini imleç kontrol paneli sayesinde:
1. Tek karakterlik sol/sağ adımlama (◀ 1 ve 1 ▶)
2. Kelime bazlı hızlı atlama (⏮ Kelime ve Kelime ⏭)
3. Hassas dokunmatik mikro-trackpad kaydırıcı
4. Anlık tek tuşla kelime ve cümle seçimi

Bu metin üzerinde herhangi bir yere tıklayın ve yukarıda açılan mini kontrol barı ile imleci dilediğiniz harfin önüne/arkasına kaydırın!""",
                        cursorPosition = 0
                    )
                )

                dao.insertDocument(
                    DocumentEntity(
                        title = "Teknik Kod & JSON Örneği",
                        category = "Kod",
                        content = """{
  "api_endpoint": "https://api.gateway.internal/v2/transactions",
  "status": "ACTIVE_PROCESSING",
  "payload": {
    "account_id": "ACC_9948201",
    "currency": "TRY",
    "amount": 254800.50,
    "security_hash": "a8f9e7c41b63d20a77e5",
    "flags": ["URGENT", "VERIFIED", "NO_DELAY"]
  },
  "metrics": {
    "execution_time_ms": 14.82,
    "retry_count": 0
  }
}""",
                        cursorPosition = 0
                    )
                )

                dao.insertDocument(
                    DocumentEntity(
                        title = "Sözleşme & Hukuki Şablon Maddeleri",
                        category = "Hukuk",
                        content = """MADDE 4 — TARAFLARIN YÜKÜMLÜLÜKLERİ VE HASSASİYET ŞARTLARI
4.1. İşbu sözleşme kapsamında sunulan tüm dijital hizmetler, 6698 sayılı Kişisel Verilerin Korunması Kanunu (KVKK) ve ilgili mevzuat hükümlerine tam uyumlu olarak ifa edilecektir.
4.2. Taraflar, sisteme girilen her türlü metinsel verinin doğruluğundan ve veri bütünlüğünden bizzat sorumludur.
4.3. Herhangi bir uyuşmazlık halinde İstanbul Anadolu Adliyesi Mahkemeleri ve İcra Daireleri yetkilidir.""",
                        cursorPosition = 0
                    )
                )
            }
        }
    }
}
