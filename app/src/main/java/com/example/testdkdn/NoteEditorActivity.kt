package com.example.testdkdn

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NoteEditorActivity : ComponentActivity() {
    private val database = FirebaseDatabase.getInstance().getReference("notes")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Launcher để chọn ảnh từ thư viện
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadImage(it) }
        }

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
        var imageUrl by remember { mutableStateOf<String?>(null) } // Thêm biến lưu ảnh

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (titleInitial.isEmpty()) "Thêm Ghi Chú" else "Chỉnh Sửa Ghi Chú",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 32.dp)
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        saveOrUpdateNote(titleInitial, title, description, imageUrl)
                        finish()
                    }
                ) {
                    Text(if (titleInitial.isEmpty()) "Thêm" else "Lưu thay đổi")
                }

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Text("Chọn ảnh")
                }
            }

            // Hiển thị ảnh nếu có
            imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Ảnh đã chọn",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
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

    fun getRealPathFromURI(uri: Uri): String? {
        val cursor = contentResolver.query(uri, arrayOf(android.provider.MediaStore.Images.Media.DATA), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(android.provider.MediaStore.Images.Media.DATA)
                if (columnIndex != -1) it.getString(columnIndex) else null
            } else null
        }
    }

    fun uploadImage(imageUri: Uri) {
        Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show()

        val filePath = getRealPathFromURI(imageUri) // Chuyển Uri thành đường dẫn file
        if (filePath != null) {
            CloudinaryManager.uploadImage(filePath) { imageUrl ->
                if (imageUrl != null) {
                    Toast.makeText(this, "Upload thành công!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Upload thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Không thể lấy đường dẫn ảnh!", Toast.LENGTH_SHORT).show()
        }
    }


    fun saveOrUpdateNote(oldTitle: String, newTitle: String, description: String, imageUrl: String?) {
        if (newTitle.isNotBlank()) {
            val note = mapOf("title" to newTitle, "description" to description)

            // Tạo ghi chú mới với tiêu đề mới
            database.child(userId!!).child(newTitle).setValue(note)
                .addOnSuccessListener {
                    // Xóa ghi chú cũ nếu tiêu đề thay đổi
                    if (oldTitle.isNotEmpty() && oldTitle != newTitle) {
                        deleteNote(oldTitle)
                    }
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
                Toast.makeText(this, "thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
