package io.github.sangpire.acam

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            // 사진 저장 위치를 저장할 상태 변수 (Uri: 파일의 주소 역할)
            var photoUri by remember { mutableStateOf<Uri?>(null) }
            
            // 사진 촬영 결과(성공/실패)를 받아오는 런처
            val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                // 촬영 취소 시 미리 생성해둔 빈 파일을 삭제
                if (!success) photoUri?.let { context.contentResolver.delete(it, null, null) }
            }

            // 카메라 권한 요청 결과를 받아오는 런처
            val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // 권한 허용 시: 빈 사진 주소를 먼저 만들고 카메라 실행
                    photoUri = createPhotoUri(context)?.also { cameraLauncher.launch(it) }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // 버튼 클릭 시 카메라 권한 요청부터 시작
                Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                    Text("Take Photo")
                }
            }
        }
    }

    // ponytail: MediaStore is the leanest way to save to gallery without FileProvider boilerplate
    /**
     * 시스템 갤러리에 사진을 저장하기 위한 빈 파일(Uri)을 생성합니다.
     */
    private fun createPhotoUri(context: Context): Uri? {
        val values = ContentValues().apply {
            // 파일 이름 설정 (시간 기반으로 중복 방지)
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            // 파일 형식 설정
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        // MediaStore를 통해 외부 저장소(갤러리)에 데이터 삽입 후 주소 반환
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}
