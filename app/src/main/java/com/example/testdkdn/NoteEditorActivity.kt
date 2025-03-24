package com.example.testdkdn

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NoteEditorActivity : ComponentActivity() {
    private val database = FirebaseDatabase.getInstance().getReference("notes")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val noteId = intent.getStringExtra("noteId")
        val initialTitle = intent.getStringExtra("title") ?: ""
        val initialDescription = intent.getStringExtra("description") ?: ""

        setContent {
            NoteEditorScreen(noteId, initialTitle, initialDescription)
        }
    }

    @Composable
    fun NoteEditorScreen(noteId: String?, titleInitial: String, descriptionInitial: String) {
        var title by remember { mutableStateOf(titleInitial) }
        var description by remember { mutableStateOf(descriptionInitial) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (noteId == null) "Thêm Ghi Chú" else "Chỉnh Sửa Ghi Chú",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Nội dung") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (noteId == null) saveNote(title, description)
                    else updateNote(noteId, title, description)
                    finish()
                }
            ) {
                Text(if (noteId == null) "Thêm" else "Lưu thay đổi")
            }

            if (noteId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        deleteNote(noteId)
                        finish()
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            }
        }
    }

    fun saveNote(title: String, description: String) {
        val noteId = database.child(userId!!).push().key ?: return
        val note = Note(noteId, title, description)

        database.child(userId).child(noteId).setValue(note)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateNote(noteId: String, title: String, description: String) {
        val updatedNote = Note(noteId, title, description)
        database.child(userId!!).child(noteId).setValue(updatedNote)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteNote(noteId: String) {
        database.child(userId!!).child(noteId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


