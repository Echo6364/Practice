import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPUtils {
    // 上传或下载状态的枚举类
    public enum UploadStatus {
        File_Exits(0), Create_Directory_Success(1), Create_Directory_Fail(2), Upload_From_Break_Success(
                3), Upload_From_Break_Faild(4), Download_From_Break_Success(5), Download_From_Break_Faild(
                6), Upload_New_File_Success(7), Upload_New_File_Failed(8), Delete_Remote_Success(
                9), Delete_Remote_Faild(10), Remote_Bigger_Local(11), Remote_smaller_local(12);
        /*
         * 实现断点续传，使用枚举类enum表示上传或者下载的状态。---- 状态分别为 文件存在 创建目录成功 创建目录失败 ...
         */
        private int status;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        UploadStatus(int status) {
            this.status = status;
        }
    }

    private FTPClient ftpClient = new FTPClient();

    /**
     * 对象构造，设置将过程中使用的命令输出到控制台
     */
    public FTPUtils() {
        this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }

    /**
     * @param hostname 主机名
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return 是否连接成功
     * @throws IOException 抛出异常
     */
    // 连接 ftpclient 的connect 方法
    public boolean connect(String hostname, int port, String username, String password) throws IOException {
        ftpClient.connect(hostname, port);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(username, password)) {
                return true;
            }
        }
        disconnect();
        return false;
    }

    /**
     * 从FTP服务器上下载文件
     *
     * @param remoteDir 远程文件路径
     * @param localDir  本地文件路径 只包含路径 为/?/ 的形式，本地文件路径支持:\\ 远程文件路径只能为/?/
     * @return 是否成功
     * @throws IOException 使用 enterLocalPassiveMode 每次传输之前client和server 开通一个端口传输数据
     *                     传输使用二进制传输
     */

    public boolean download(String localDir, String remoteDir, String localFileName) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        String local = localDir + localFileName;
        localFileName = new String(localFileName.getBytes("GBK"), "iso-8859-1");
        remoteDir = new String(remoteDir.getBytes("GBK"), "iso-8859-1");
        String remote = remoteDir + localFileName;
        boolean result;
        File f = new File(local);
        FTPFile[] files = ftpClient.listFiles(remote);
        if (files.length != 1) {
            System.out.println("远程文件不唯一");
            return false;
        }
        long lRemoteSize = files[0].getSize();
        if (f.exists()) {
            OutputStream out = new FileOutputStream(f, true);
            System.out.println("本地文件大小为：" + f.length());
            if (f.length() >= lRemoteSize) {
                System.out.println("本地文件大小大于远程文件大小，下载中止");
                return false;
            }
            ftpClient.setRestartOffset(f.length());
            result = ftpClient.retrieveFile(remote, out);
            out.close();
        } else {
            OutputStream out = new FileOutputStream(f);
            result = ftpClient.retrieveFile(remote, out);
            out.close();
        }
        return result;
    }

    /**
     * 上传文件到FTP服务器，支持断点续传
     *
     * @param local          本地文件名称，绝对路径
     * @param remoteDir
     * @param remoteFileName 远程文件路径，使用/root/555/666
     *                       按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
     * @return 上传结果
     * @throws IOException
     */
    public UploadStatus upload(String localDir, String remoteDir, String remoteFileName) throws IOException {
        // 设置PassiveMode传输
        ftpClient.enterLocalPassiveMode();
        // 设置以二进制流的方式传输
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        UploadStatus result;
        // 对远程目录的处理
        String local = localDir + remoteFileName;
        remoteFileName = new String(remoteFileName.getBytes("GBK"), "iso-8859-1");
        remoteDir = new String(remoteDir.getBytes("GBK"), "iso-8859-1");
        String remote = remoteDir + remoteFileName;
        if (remote.contains("/")) {
            String directory = remoteDir;
            if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) {
                // 如果远程目录不存在，则递归创建远程服务器目录
                int start = 0;
                int end = 0;
                if (directory.startsWith("/")) {
                    start = 1;
                } else {
                    start = 0;
                }
                end = directory.indexOf("/", start);  //indexOf (searchvalue,fromindex) 用start作为可选的整数参数，即从xx位置开始
                while (true) {
                    String subDirectory = remote.substring(start, end);
                    if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                        if (ftpClient.makeDirectory(subDirectory)) {
                            ftpClient.changeWorkingDirectory(subDirectory);
                        } else {
                            System.out.println("创建目录失败");
                            return UploadStatus.Create_Directory_Fail;
                        }
                    }
                    start = end + 1;
                    end = directory.indexOf("/", start);
                    // 检查目录是否创建完毕
                    if (end <= start) {
                        break;
                    }
                }
            }
        }
        // 检查远程文件是否存在
        FTPFile[] files = ftpClient.listFiles(remoteFileName);
        if (files.length == 1) {
            Long remoteSize = files[0].getSize();
            File f = new File(local);
            Long localSize = f.length();
            if (remoteSize == localSize) {
                return UploadStatus.File_Exits;
            } else if (remoteSize > localSize) {
                return UploadStatus.Remote_Bigger_Local;
            }
            // 尝试移动文件内读取指针，实现断点续传
            InputStream is = new FileInputStream(f);
            if (is.skip(remoteSize) == remoteSize) {
                ftpClient.setRestartOffset(remoteSize);
                if (ftpClient.storeFile(remote, is)) {
                    return UploadStatus.Upload_From_Break_Success;
                }
            }
            // 如果断点续传没有成功，则删除服务器上的文件，重新上传
            if (!ftpClient.deleteFile(remoteFileName)) {
                return UploadStatus.Delete_Remote_Faild;
            }
            is = new FileInputStream(f);
            if (ftpClient.storeFile(remote, is)) {
                result = UploadStatus.Upload_New_File_Success;
            } else {
                result = UploadStatus.Upload_New_File_Failed;
            }
            is.close();
        } else {
            InputStream is = new FileInputStream(local);
            if (ftpClient.storeFile(remoteFileName, is)) {
                result = UploadStatus.Upload_New_File_Success;
            } else {
                result = UploadStatus.Upload_New_File_Failed;
            }
            is.close();
        }
        return result;
    }

    /**
     * @param remoteDir FTP服务器路径
     * @throws IOException
     */
    public void listRemoteFiles(String remoteDir) throws IOException {
        // 判断是否以/开头和结尾
        String directory = remoteDir;
        if (remoteDir.startsWith("/") && remoteDir.endsWith("/")) {
            try {
                String files[] = ftpClient.listNames(remoteDir);
                if (files == null || files.length == 0)
                    System.out.println("没有任何文件!");
                else {
                    for (int i = 0; i < files.length; i++) {
                        System.out.println(files[i]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (remoteDir.indexOf(".") > 0) {
            System.out.println("请保证目录不包含文件名");
        } else if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) {
            System.out.println("文件目录不存在");
        } else {
            System.out.println("请保证目录格式正确，以/开头结尾");
        }
    }

    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */

    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    public static void main(String[] args) {
        FTPUtils myFtp = new FTPUtils();
        try {
            // 填写路径，文件名称
            String localDir = "";
            String remoteDir = "";
            String remoteFileName = "";
            String localFileName = "";
            myFtp.connect("192.168.*.*", 21, "inspurftp", "*************");
            myFtp.download("E:/", "/测试测试测试/", "安装说明.rtf");
//			myFtp.listRemoteFiles("/abcdef.");
            myFtp.disconnect();
        } catch (IOException e) {
            System.out.println("连接FTP出错：" + e.getMessage());
        }
    }
}
