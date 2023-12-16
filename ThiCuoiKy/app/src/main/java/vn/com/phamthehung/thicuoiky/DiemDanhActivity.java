package vn.com.phamthehung.thicuoiky;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalDateTime;

// Class user
public class DiemDanhActivity extends AppCompatActivity {
    TextView tenSK;
    EditText hoTenSV, lop;
    Button diemDanh, quayLai;
    LocalDateTime tgDiemDanh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diem_danh);
        timDieuKhien();

        String eventName = getIntent().getStringExtra("EVENT_NAME");
        if (eventName != null) {
            fetchEventName(eventName);
        }
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