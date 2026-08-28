package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustodiaDao {

    // --- Members ---
    @Query("SELECT * FROM family_members ORDER BY generation ASC, name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: String): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteMemberById(id: String)

    @Query("DELETE FROM family_members")
    suspend fun clearMembers()

    // --- Documents ---
    @Query("SELECT * FROM documents ORDER BY issueDate DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE memberId = :memberId ORDER BY issueDate DESC")
    fun getDocumentsByMemberId(memberId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("DELETE FROM documents WHERE memberId = :memberId")
    suspend fun deleteDocumentsByMemberId(memberId: String)

    @Query("DELETE FROM documents")
    suspend fun clearDocuments()

    // --- Signatures ---
    @Query("SELECT * FROM signatures")
    fun getAllSignatures(): Flow<List<SignatureEntity>>

    @Query("SELECT * FROM signatures WHERE memberId = :memberId LIMIT 1")
    fun getSignatureByMemberId(memberId: String): Flow<SignatureEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(signature: SignatureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignatures(signatures: List<SignatureEntity>)

    @Query("DELETE FROM signatures WHERE memberId = :memberId")
    suspend fun deleteSignatureByMemberId(memberId: String)

    @Query("DELETE FROM signatures")
    suspend fun clearSignatures()

    // --- Medical Entries ---
    @Query("SELECT * FROM medical_entries ORDER BY date DESC")
    fun getAllMedicalEntries(): Flow<List<MedicalEntryEntity>>

    @Query("SELECT * FROM medical_entries WHERE memberId = :memberId ORDER BY date DESC")
    fun getMedicalEntriesByMemberId(memberId: String): Flow<List<MedicalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalEntry(entry: MedicalEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalEntries(entries: List<MedicalEntryEntity>)

    @Update
    suspend fun updateMedicalEntry(entry: MedicalEntryEntity)

    @Query("DELETE FROM medical_entries WHERE id = :id")
    suspend fun deleteMedicalEntryById(id: String)

    @Query("DELETE FROM medical_entries WHERE memberId = :memberId")
    suspend fun deleteMedicalEntriesByMemberId(memberId: String)

    @Query("DELETE FROM medical_entries")
    suspend fun clearMedicalEntries()

    // --- Backups ---
    @Query("SELECT * FROM drive_backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<DriveBackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: DriveBackupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackups(backups: List<DriveBackupEntity>)

    @Query("DELETE FROM drive_backups WHERE id = :id")
    suspend fun deleteBackupById(id: String)

    @Query("DELETE FROM drive_backups")
    suspend fun clearBackups()

    // Direct snapshot fetching for backup packaging
    @Query("SELECT * FROM family_members")
    suspend fun getMembersSnapshot(): List<MemberEntity>

    @Query("SELECT * FROM documents")
    suspend fun getDocumentsSnapshot(): List<DocumentEntity>

    @Query("SELECT * FROM signatures")
    suspend fun getSignaturesSnapshot(): List<SignatureEntity>

    @Query("SELECT * FROM medical_entries")
    suspend fun getMedicalEntriesSnapshot(): List<MedicalEntryEntity>

    // Atomically restore full state
    @Transaction
    suspend fun restoreFullDatabase(
        members: List<MemberEntity>,
        docs: List<DocumentEntity>,
        sigs: List<SignatureEntity>,
        meds: List<MedicalEntryEntity>
    ) {
        clearMembers()
        clearDocuments()
        clearSignatures()
        clearMedicalEntries()

        if (members.isNotEmpty()) insertMembers(members)
        if (docs.isNotEmpty()) insertDocuments(docs)
        if (sigs.isNotEmpty()) insertSignatures(sigs)
        if (meds.isNotEmpty()) insertMedicalEntries(meds)
    }
}
