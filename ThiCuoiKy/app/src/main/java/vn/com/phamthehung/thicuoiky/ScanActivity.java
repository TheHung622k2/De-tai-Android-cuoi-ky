package vn.com.phamthehung.thicuoiky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {
    ListenableFuture cameraProviderFuture; // đợi và lấy CameraProvider.
    ExecutorService cameraExecutor; // chạy các task liên quan đến Camera trên một luồng riêng biệt.
    PreviewView previewView; // hiển thị ảnh trước của camera.
    MyImageAnalyzer analyzer; // phân tích hình ảnh từ camera.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        setupEventListeners();

        previewView = findViewById(R.id.viewXemTruoc);

        //  Tạo một ExecutorService với một luồng duy nhất (thực hiện các tác vụ liên quan đến camera trên một luồng riêng biệt)
        cameraExecutor = Executors.newSingleThreadExecutor();
        // Tạo một ListenableFuture để lấy đối tượng CameraProvider.
        // CameraProvider là một đối tượng cung cấp quyền truy cập và quản lý các thiết bị camera trên thiết bị.
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Khởi tạo một đối tượng của lớp MyImageAnalyzer để xử lý và phân tích hình ảnh từ camera
        analyzer = new MyImageAnalyzer(getSupportFragmentManager());

        // Đăng ký một Listener để lắng nghe sự kiện khi ListenableFuture (cameraProviderFuture) có sẵn (khi đối tượng CameraProvider được khởi tạo).
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                // kiểm tra xem ứng dụng có quyền truy cập camera hay không. Nếu không, yêu cầu người dùng cấp quyền (requestPermissions).
                try {
                    if (ActivityCompat.checkSelfPermission(ScanActivity.this, android.Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                    }
                    // Nếu có quyền truy cập, lấy đối tượng ProcessCameraProvider và gọi phương thức bindPreview để liên kết các use case của CameraX (Preview, ImageCapture, ImageAnalysis) và hiển thị hình ảnh camera trên giao diện người dùng.
                    else {
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindPreview(processCameraProvider);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //Liên kết các use case của CameraX
    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build(); // Tạo một đối tượng Preview giúp hiển thị hình ảnh từ camera trực tiếp lên giao diện người dùng.
        // Tạo một đối tượng CameraSelector để chọn camera cụ thể
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(
                        CameraSelector.LENS_FACING_BACK) // mặt sau camera
                .build();
        // hiển thị hình ảnh camera lên previewView (đối tượng PreviewView).
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Tạo một đối tượng ImageCapture để chụp ảnh từ camera.
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        // phân tích hình ảnh từ camera
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        // Gán một Analyzer cho ImageAnalysis. analyzer là một đối tượng của lớp MyImageAnalyzer được khởi tạo trước đó. MyImageAnalyzer được sử dụng để xử lý và phân tích hình ảnh từ camera.
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

        // Hủy liên kết tất cả các use case trước đó của CameraProvider đảm bảo rằng khi thay đổi các cấu hình camera, không còn bất kỳ use case nào đang chạy.
        processCameraProvider.unbindAll();
        // Liên kết các use case với CameraProvider và vòng đời của activity (this).
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    private void setupEventListeners() {
        Button btnBack = findViewById(R.id.btnQuayLai);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Class phân tích hình ảnh từ camera
    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private boolean isQRScanned = false;
        FragmentManager fragmentManager;
        BottomDialog bd;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new BottomDialog();
        }

        // Quét mã QR (Được gọi khi một frame mới từ camera được cung cấp)
        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (!isQRScanned) {
                ScanBarCode(image);
            }
        }

        // quét mã vạch từ hình ảnh được cung cấp.
        private void ScanBarCode(ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions scannerOptions = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                    )
                    .enableAllPotentialBarcodes() // Optional
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(scannerOptions);

            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            ReaderBarCodeData(barcodes); // Xử lý kết quả quét từ mã vạch, nhận diện các loại mã vạch khác nhau
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Neu doc QR that bai
                            Toast.makeText(ScanActivity.this, "Doc ma vach that bai!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });
        }

        // Xử lý kết quả quét từ mã vạch, nhận diện các loại mã vạch khác nhau
        private void ReaderBarCodeData(List<Barcode> barcodes) {
            for (Barcode barcode : barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();
                String rawValue = barcode.getRawValue();
                int valueType = barcode.getValueType();

                switch (valueType) {
                    // QR code của link điều hướng
                    case Barcode.TYPE_URL:
                        if (!bd.isAdded()) {
                            bd.show(fragmentManager, "");
                        }
                        bd.fetchURL(Objects.requireNonNull(barcode.getUrl()).getUrl());
                        String title = barcode.getUrl().getTitle();
                        String url = barcode.getUrl().getUrl();
                        break;

                    // QR code của việc điểm danh
                    case Barcode.TYPE_TEXT:
                        if (!isQRScanned) {
                            String text = barcode.getDisplayValue(); // lấy nd text
                            Intent intent = new Intent(ScanActivity.this, DiemDanhActivity.class);
                            intent.putExtra("EVENT_NAME", text);
                            startActivity(intent);
                            isQRScanned = true; // đặt thành true để dừng việc quét ảnh
                        }
                        break;
                }
            }
        }
    }
}