package com.ctech.noticeboard.ui.upload;

public class UploadInfo {
    public String fileName;
    public String fileURL;

    public UploadInfo(){}

    public UploadInfo (String name, String url){
        this.fileName = name;
        this.fileURL = url;
    }

    public String getFileName (){
        return fileName;
    }

    public String getFileURL (){
        return fileURL;
    }


}
