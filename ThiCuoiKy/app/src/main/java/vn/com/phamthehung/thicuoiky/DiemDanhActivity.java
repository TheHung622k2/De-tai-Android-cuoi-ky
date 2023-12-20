package vn.com.phamthehung.thicuoiky;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;

public class DiemDanhActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    TextView tenSK;
    EditText hoTenSV, lop;
    Button diemDanh, quayLai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Tên sự kiện");
        setContentView(R.layout.activity_diem_danh);
        timDieuKhien();

        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            fetchEventName(eventName);
        }

        diemDanh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoTen = hoTenSV.getText().toString();
                String lopHoc = lop.getText().toString();
                // lấy tg điểm danh
                LocalDateTime tgDiemDanh = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    tgDiemDanh = LocalDateTime.now();
                }
                String thoiGianDiemDanh = tgDiemDanh.toString();
                // tạo đối tượng User
                User user = new User(hoTen, lopHoc, thoiGianDiemDanh);
                // Lưu dữ liệu vào Realtime Database
                databaseReference.child(tenSK.getText().toString()).push().setValue(user);
                // Thông báo điểm danh thành công
                Toast.makeText(DiemDanhActivity.this, "Điểm danh thành công", Toast.LENGTH_SHORT).show();
            }
        });

        quayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // find Controls
    private void timDieuKhien() {
        tenSK = findViewById(R.id.tvEventName);
        hoTenSV = findViewById(R.id.edtFullName);
        lop = findViewById(R.id.edtClass);
        diemDanh = findViewById(R.id.btnDiemDanh);
        quayLai = findViewById(R.id.btnQuayLai);
    }

    // lấy tên sk từ QR code
    public void fetchEventName(String eventName) {
        tenSK.setText(eventName); // thiết lập tên sự kiện
    }
}