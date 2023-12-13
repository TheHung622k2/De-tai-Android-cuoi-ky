package vn.com.phamthehung.thicuoiky;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Class này được sử dụng cho việc hiển thị một bottom sheet (cửa sổ hiển thị từ dưới đáy của màn hình)
public class BottomDialog extends BottomSheetDialogFragment {
    TextView duongDan;
    ImageView close;
    Button truyCap;
    String fetchURL; // biến để lưu địa chỉ URL của mã QR

    // Phương thức được gọi khi bottom sheet đc tạo
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // dùng inflater để chuyển đổi layout xml thành một đối tượng View
        // container: ViewGroup cha mà view sẽ được gắn vào bottom dialog
        // false: ko gắn tức thì vào container vì bd sẽ tự thêm view vào
        View view = inflater.inflate(R.layout.activity_bottom_dialog, container, false);

        duongDan = view.findViewById(R.id.tvURL);
        close = view.findViewById(R.id.imgClose);
        truyCap = view.findViewById(R.id.btnVisit);

        // đặt text của Tv 'duongDan' = text hiện tại của fetchURL
        // Khi bd được hiển thị, URL sẽ hiển thị trên TextView 'duongDan'
        duongDan.setText(fetchURL);

        truyCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tạo một đối tượng Intent với hành động VIEW. Hđ này đc dùng để mở nội dung dạng view
                Intent intent = new Intent("android.intent.action.VIEW");
                // truyền địa chỉ URL cho intent
                intent.setData(Uri.parse(fetchURL));
                startActivity(intent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // đóng bottom dialog
            }
        });
        return view;
    }

    // lấy địa chỉ URL từ QR code
    public void fetchURL(String url) {
        ExecutorService executorService = Executors.newSingleThreadExecutor(); // xử lý?
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                fetchURL = url;
            }
        });
    }
}