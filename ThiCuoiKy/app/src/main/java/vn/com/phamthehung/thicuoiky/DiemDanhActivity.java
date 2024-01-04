package vn.com.phamthehung.thicuoiky;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import java.util.Locale;

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

                String thoiGianDiemDanh = getFormattedTime();

                // Tạo đối tượng User
                User user = new User(hoTen, lopHoc, thoiGianDiemDanh);

                // Tạo một khóa duy nhất cho mỗi người dùng
                String userId = databaseReference.child(tenSK.getText().toString()).push().getKey();

                // Lưu dữ liệu vào Realtime Database với tên các nút con mong muốn
                assert userId != null;
                databaseReference.child(tenSK.getText().toString()).child(userId).child("Họ tên").setValue(user.getHoTen());
                databaseReference.child(tenSK.getText().toString()).child(userId).child("Lớp").setValue(user.getLop());
                databaseReference.child(tenSK.getText().toString()).child(userId).child("Thời gian điểm danh").setValue(user.getThoiGianDiemDanh());

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

    // Hàm để lấy thời gian đã định dạng
    private String getFormattedTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy 'vào lúc' HH:mm:ss", Locale.getDefault());
        Date now = new Date();
        return dateFormat.format(now);
    }

    // lấy tên sk từ QR code
    public void fetchEventName(String eventName) {
        tenSK.setText(eventName); // thiết lập tên sự kiện
    }
}