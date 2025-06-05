package com.example.imageeditorapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.imageeditorapp.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isDrawingSquare = false

    // ActivityResultLauncher để xử lý kết quả chọn ảnh từ thư viện
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                // Sử dụng Glide để tải ảnh vào ImageView
                Glide.with(this).load(imageUri).into(binding.imageView)
                // Đảm bảo ẩn hình vuông vẽ nếu đang hiển thị
                binding.drawingView.enableDrawing(false)
                isDrawingSquare = false
            }
        }
    }

    // ActivityResultLauncher để xử lý yêu cầu quyền truy cập
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền được cấp, tiếp tục chọn ảnh
            openImagePicker()
        } else {
            // Quyền bị từ chối, hiển thị thông báo
            Toast.makeText(this, "Cần quyền truy cập bộ nhớ để tải ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ban đầu ẩn thanh SeekBar và hình vuông xem trước
        binding.colorSeekBar.visibility = View.GONE
        binding.colorPreviewSquare.visibility = View.GONE

        // Khởi tạo SeekBar với gradient màu
        setupColorSeekBar()

        // Button 1: Tải ảnh lên
        binding.buttonUploadImage.setOnClickListener {
            checkPermissionsAndPickImage()
        }

        // Button 2: Vẽ hình vuông
        binding.buttonDrawSquare.setOnClickListener {
            isDrawingSquare = !isDrawingSquare // Bật/tắt chế độ vẽ
            binding.drawingView.enableDrawing(isDrawingSquare)

            if (isDrawingSquare) {
                Toast.makeText(this, "Đã bật chế độ vẽ hình vuông", Toast.LENGTH_SHORT).show()
                // Đặt màu mặc định cho hình vuông khi bắt đầu vẽ
                binding.drawingView.setSquareColor(Color.YELLOW)
                // Ẩn SeekBar nếu đang hiển thị khi bắt đầu vẽ hình vuông mới
                binding.colorSeekBar.visibility = View.GONE
                binding.colorPreviewSquare.visibility = View.GONE
            } else {
                Toast.makeText(this, "Đã tắt chế độ vẽ hình vuông", Toast.LENGTH_SHORT).show()
                // Ẩn SeekBar khi tắt chế độ vẽ
                binding.colorSeekBar.visibility = View.GONE
                binding.colorPreviewSquare.visibility = View.GONE
            }
        }

        // Button 3: Hiển thị thanh Seekbar bảng màu
        binding.buttonColorPicker.setOnClickListener {
            if (isDrawingSquare) {
                binding.colorSeekBar.visibility = if (binding.colorSeekBar.visibility == View.GONE) View.VISIBLE else View.GONE
                binding.colorPreviewSquare.visibility = binding.colorSeekBar.visibility
                if (binding.colorSeekBar.visibility == View.VISIBLE) {
                    updateColorPreviewThumb(binding.colorSeekBar.progress)
                }
            } else {
                Toast.makeText(this, "Vui lòng bật chế độ vẽ hình vuông trước", Toast.LENGTH_SHORT).show()
                binding.colorSeekBar.visibility = View.GONE
                binding.colorPreviewSquare.visibility = View.GONE
            }
        }

        binding.colorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val color = generateColorFromHue(progress.toFloat())
                binding.drawingView.setSquareColor(color)
                updateColorPreviewThumb(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkPermissionsAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Quyền đã được cấp, mở trình chọn ảnh
                openImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Giải thích tại sao cần quyền (nếu người dùng từ chối trước đó)
                Toast.makeText(this, "Ứng dụng cần quyền đọc bộ nhớ để tải ảnh", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                // Yêu cầu quyền
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImage.launch(intent)
    }

    private fun setupColorSeekBar() {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.RED
            )
        )
        gradientDrawable.cornerRadius = 0f
        binding.colorSeekBar.progressDrawable = gradientDrawable
    }


    private fun updateColorPreviewThumb(progress: Int) {
        val color = generateColorFromHue(progress.toFloat())
        val thumbDrawable = binding.colorSeekBar.thumb as? GradientDrawable
        thumbDrawable?.setColor(color)

        // Cập nhật màu của hình vuông preview
        binding.colorPreviewSquare.setBackgroundColor(color)

        // Di chuyển hình vuông preview theo thumb của seekbar
        // Tính toán vị trí của thumb
        val thumbOffset = binding.colorSeekBar.paddingLeft + binding.colorSeekBar.paddingRight
        val availableWidth = binding.colorSeekBar.width - thumbOffset
        val thumbX = (binding.colorSeekBar.progress.toFloat() / binding.colorSeekBar.max) * availableWidth

        // Đặt margin left cho hình vuông preview
        val layoutParams = binding.colorPreviewSquare.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = thumbX.roundToInt() - (binding.colorPreviewSquare.width / 2) // Căn giữa thumb
        binding.colorPreviewSquare.requestLayout()
    }

    // Hàm này chuyển đổi giá trị Hue thành màu RGB
    private fun generateColorFromHue(hue: Float): Int {
        val saturation = 1.0f
        val value = 1.0f
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
}
