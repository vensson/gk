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

        val initialTitle = intent.getStringExtra("title") ?: ""
        val initialDescription = intent.getStringExtra("description") ?: ""

        setContent {
            NoteEditorScreen(initialTitle, initialDescription)
        }
    }

    @Composable
    fun NoteEditorScreen(titleInitial: String, descriptionInitial: String) {
        var title by remember { mutableStateOf(titleInitial) }
        var description by remember { mutableStateOf(descriptionInitial) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (titleInitial.isEmpty()) "Thêm Ghi Chú" else "Chỉnh Sửa Ghi Chú",
                style = MaterialTheme.typography.headlineMedium
                , modifier = Modifier.padding(top = 32.dp)
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
                    saveOrUpdateNote(title, description)
                    finish()
                }
            ) {
                Text(if (titleInitial.isEmpty()) "Thêm" else "Lưu thay đổi")
            }

            if (titleInitial.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        deleteNote(title)
                        finish()
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            }
        }
    }

    fun saveOrUpdateNote(title: String, description: String) {
        if (title.isNotBlank()) {
            val note = mapOf("title" to title, "description" to description)
            database.child(userId!!).child(title).setValue(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun deleteNote(title: String) {
        database.child(userId!!).child(title).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
