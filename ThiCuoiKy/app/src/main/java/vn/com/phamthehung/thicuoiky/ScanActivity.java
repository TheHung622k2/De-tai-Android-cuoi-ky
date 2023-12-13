package vn.com.phamthehung.thicuoiky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
    ListenableFuture cameraProviderFuture; // biến yêu cầu một CameraProvider
    ExecutorService cameraExecutor; // ?
    PreviewView previewView; // biến UI để hiển thị trước từ camera
    MyImageAnalyzer analyzer; // biến phân tích hình ảnh từ camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        setupEventListeners(); // tlsk cho các buttons

        previewView = findViewById(R.id.viewXemTruoc);
        this.getWindow().setFlags(1024, 1024);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        analyzer = new MyImageAnalyzer(getSupportFragmentManager());

        // cameraProviderFuture: Nhận thông báo về việc cung cấp máy ảnh
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // kiểm tra xem ứng dụng có quyền truy cập camera hay không?
                    if(ActivityCompat.checkSelfPermission(ScanActivity.this, android.Manifest.permission.CAMERA)!=(PackageManager.PERMISSION_GRANTED)){
                        // nếu 'android.Manifest.permission.CAMERA' ko được cấp quyền thì y/cầu người dùng cấp quyền
                        ActivityCompat.requestPermissions(ScanActivity.this,new String[]{Manifest.permission.CAMERA},101);
                    }
                    else{ // Kiểm tra xem CameraProvider có dùng được hay không
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindPreview(processCameraProvider);
                    }
                } catch (ExecutionException | InterruptedException e) {

                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    // Chọn một máy ảnh rồi liên kết vòng đời và trường hợp sử dụng
    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build(); // B1: Tạo Preview
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(
                CameraSelector.LENS_FACING_BACK) // B2: Chỉ định tuỳ chọn LensFacing cho máy ảnh mà bạn muốn dùng.
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider()); // B4: Kết nối Preview với PreviewView

        ImageCapture imageCapture = new ImageCapture.Builder().build(); // tạo đối tượng để chụp ảnh
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder() // phân tích hình ảnh
                .setTargetResolution(new Size(1280, 720)) // đặt độ phân giải hình ảnh
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

        // hủy kết nối tất cả các use case trước khi liên kết mới để đảm bảo không có xung đột giữa chúng
        processCameraProvider.unbindAll();
        // B3: Liên kết máy ảnh đã chọn và mọi trường hợp sử dụng với vòng đời.
        // bindToLifecycle() trả về đối tượng Camera
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    // yêu cầu quyền truy cập từ người dùng
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0) {
            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            bindPreview(processCameraProvider);
        }
    }

    // thiết lập sự kiện lắng nghe cho các nút
    private void setupEventListeners() {
        ImageView backImage = findViewById(R.id.backImage);
        TextView tvBack = findViewById(R.id.tvBack);
        Button btnMyQR = findViewById(R.id.btnMyQR);
        Button btnExistenceImages = findViewById(R.id.btnExistenceImages);

        // Sự kiện lắng nghe cho ImageView và TextView
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại MainActivity khi nhấn vào ImageView
                finish();
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại MainActivity khi nhấn vào TextView
                finish();
            }
        });
    }

    // Class phân tích hình ảnh
    public class MyImageAnalyzer implements ImageAnalysis.Analyzer{
        FragmentManager fragmentManager;
        BottomDialog bd;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new BottomDialog();
        }

        // Quét mã QR
        @Override
        public void analyze(@NonNull ImageProxy image) {
            ScanBarCode(image);
        }

        private void ScanBarCode(ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions scannerOptions = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                    ).build();

            BarcodeScanner scanner = BarcodeScanning.getClient(scannerOptions);

            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            ReaderBarCodeData(barcodes);
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

        private void ReaderBarCodeData(List<Barcode> barcodes) {
            for (Barcode barcode : barcodes) {

                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                String rawValue = barcode.getRawValue();

                int valueType = barcode.getValueType();

                switch (valueType) {
                    // QR code cua Wifi
                    case Barcode.TYPE_WIFI:
                        String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                        String password = barcode.getWifi().getPassword();
                        int type = barcode.getWifi().getEncryptionType();
                        break;
                    // QR code cua link dieu huong
                    case Barcode.TYPE_URL:
                        if (!bd.isAdded()) {
                            bd.show(fragmentManager, "");
                        }
                        bd.fetchURL(Objects.requireNonNull(barcode.getUrl()).getUrl());
                        String title = barcode.getUrl().getTitle();
                        String url = barcode.getUrl().getUrl();
                        break;
                }
            }
        }
    }
}
