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
    ListenableFuture cameraProviderFuture;
    ExecutorService cameraExecutor;
    PreviewView previewView;
    MyImageAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        setupEventListeners();

        previewView = findViewById(R.id.viewXemTruoc);


        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        analyzer = new MyImageAnalyzer(getSupportFragmentManager());

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if(ActivityCompat.checkSelfPermission(ScanActivity.this, android.Manifest.permission.CAMERA)!=(PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(ScanActivity.this,new String[]{Manifest.permission.CAMERA},101);
                    }
                    else{
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindPreview(processCameraProvider);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(
                CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    private void setupEventListeners() {
        ImageView backImage = findViewById(R.id.backImage);
        TextView tvBack = findViewById(R.id.tvBack);
        Button btnMyQR = findViewById(R.id.btnMyQR);
        Button btnExistenceImages = findViewById(R.id.btnExistenceImages);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Class phân tích hình ảnh
    public class MyImageAnalyzer implements ImageAnalysis.Analyzer{
        private boolean isQRScanned = false;
        FragmentManager fragmentManager;
        BottomDialog bd;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new BottomDialog();
        }

        // Quét mã QR
        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (!isQRScanned) {
                ScanBarCode(image);
            }
        }

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
                    // QR code của Wifi (Chưa xử lý được)
                    case Barcode.TYPE_WIFI:
                        String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                        String password = barcode.getWifi().getPassword();
                        int type = barcode.getWifi().getEncryptionType();
                        break;

                    // QR code của URL
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
                            String text = barcode.getDisplayValue(); // lấy nd văn bản
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
