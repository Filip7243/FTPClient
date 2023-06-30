package ftp.client.ftpclient;

public class UploadedFile {

    private int id;
    private String name;
    private String extension;
    private double size;
    private String date;

    public UploadedFile(int id, String name, String extension, double size, String date) {
        this.id = id;
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
