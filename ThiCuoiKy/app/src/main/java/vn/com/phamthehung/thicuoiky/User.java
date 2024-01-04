package vn.com.phamthehung.thicuoiky;

public class User {
    String hoTen, lop;
    String thoiGianDiemDanh;

    public User() {
    }

    public User(String hoTen, String lop, String thoiGianDiemDanh) {
        this.hoTen = hoTen;
        this.lop = lop;
        this.thoiGianDiemDanh = thoiGianDiemDanh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getThoiGianDiemDanh() {
        return thoiGianDiemDanh;
    }

    public void setThoiGianDiemDanh(String thoiGianDiemDanh) {
        this.thoiGianDiemDanh = thoiGianDiemDanh;
    }
}