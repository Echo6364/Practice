import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPUtils {
    // �ϴ�������״̬��ö����
    public enum UploadStatus {
        File_Exits(0), Create_Directory_Success(1), Create_Directory_Fail(2), Upload_From_Break_Success(
                3), Upload_From_Break_Faild(4), Download_From_Break_Success(5), Download_From_Break_Faild(
                6), Upload_New_File_Success(7), Upload_New_File_Failed(8), Delete_Remote_Success(
                9), Delete_Remote_Faild(10), Remote_Bigger_Local(11), Remote_smaller_local(12);
        /*
         * ʵ�ֶϵ�������ʹ��ö����enum��ʾ�ϴ��������ص�״̬��---- ״̬�ֱ�Ϊ �ļ����� ����Ŀ¼�ɹ� ����Ŀ¼ʧ�� ...
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
     * �����죬���ý�������ʹ�õ��������������̨
     */
    public FTPUtils() {
        this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }

    /**
     * @param hostname ������
     * @param port     �˿�
     * @param username �û���
     * @param password ����
     * @return �Ƿ����ӳɹ�
     * @throws IOException �׳��쳣
     */
    // ���� ftpclient ��connect ����
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
     * ��FTP�������������ļ�
     *
     * @param remoteDir Զ���ļ�·��
     * @param localDir  �����ļ�·�� ֻ����·�� Ϊ/?/ ����ʽ�������ļ�·��֧��:\\ Զ���ļ�·��ֻ��Ϊ/?/
     * @return �Ƿ�ɹ�
     * @throws IOException ʹ�� enterLocalPassiveMode ÿ�δ���֮ǰclient��server ��ͨһ���˿ڴ�������
     *                     ����ʹ�ö����ƴ���
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
            System.out.println("Զ���ļ���Ψһ");
            return false;
        }
        long lRemoteSize = files[0].getSize();
        if (f.exists()) {
            OutputStream out = new FileOutputStream(f, true);
            System.out.println("�����ļ���СΪ��" + f.length());
            if (f.length() >= lRemoteSize) {
                System.out.println("�����ļ���С����Զ���ļ���С��������ֹ");
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
     * �ϴ��ļ���FTP��������֧�ֶϵ�����
     *
     * @param local          �����ļ����ƣ�����·��
     * @param remoteDir
     * @param remoteFileName Զ���ļ�·����ʹ��/root/555/666
     *                       ����Linux�ϵ�·��ָ����ʽ��֧�ֶ༶Ŀ¼Ƕ�ף�֧�ֵݹ鴴�������ڵ�Ŀ¼�ṹ
     * @return �ϴ����
     * @throws IOException
     */
    public UploadStatus upload(String localDir, String remoteDir, String remoteFileName) throws IOException {
        // ����PassiveMode����
        ftpClient.enterLocalPassiveMode();
        // �����Զ��������ķ�ʽ����
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        UploadStatus result;
        // ��Զ��Ŀ¼�Ĵ���
        String local = localDir + remoteFileName;
        remoteFileName = new String(remoteFileName.getBytes("GBK"), "iso-8859-1");
        remoteDir = new String(remoteDir.getBytes("GBK"), "iso-8859-1");
        String remote = remoteDir + remoteFileName;
        if (remote.contains("/")) {
            String directory = remoteDir;
            if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) {
                // ���Զ��Ŀ¼�����ڣ���ݹ鴴��Զ�̷�����Ŀ¼
                int start = 0;
                int end = 0;
                if (directory.startsWith("/")) {
                    start = 1;
                } else {
                    start = 0;
                }
                end = directory.indexOf("/", start);  //indexOf (searchvalue,fromindex) ��start��Ϊ��ѡ����������������xxλ�ÿ�ʼ
                while (true) {
                    String subDirectory = remote.substring(start, end);
                    if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                        if (ftpClient.makeDirectory(subDirectory)) {
                            ftpClient.changeWorkingDirectory(subDirectory);
                        } else {
                            System.out.println("����Ŀ¼ʧ��");
                            return UploadStatus.Create_Directory_Fail;
                        }
                    }
                    start = end + 1;
                    end = directory.indexOf("/", start);
                    // ���Ŀ¼�Ƿ񴴽����
                    if (end <= start) {
                        break;
                    }
                }
            }
        }
        // ���Զ���ļ��Ƿ����
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
            // �����ƶ��ļ��ڶ�ȡָ�룬ʵ�ֶϵ�����
            InputStream is = new FileInputStream(f);
            if (is.skip(remoteSize) == remoteSize) {
                ftpClient.setRestartOffset(remoteSize);
                if (ftpClient.storeFile(remote, is)) {
                    return UploadStatus.Upload_From_Break_Success;
                }
            }
            // ����ϵ�����û�гɹ�����ɾ���������ϵ��ļ��������ϴ�
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
     * @param remoteDir FTP������·��
     * @throws IOException
     */
    public void listRemoteFiles(String remoteDir) throws IOException {
        // �ж��Ƿ���/��ͷ�ͽ�β
        String directory = remoteDir;
        if (remoteDir.startsWith("/") && remoteDir.endsWith("/")) {
            try {
                String files[] = ftpClient.listNames(remoteDir);
                if (files == null || files.length == 0)
                    System.out.println("û���κ��ļ�!");
                else {
                    for (int i = 0; i < files.length; i++) {
                        System.out.println(files[i]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (remoteDir.indexOf(".") > 0) {
            System.out.println("�뱣֤Ŀ¼�������ļ���");
        } else if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(directory)) {
            System.out.println("�ļ�Ŀ¼������");
        } else {
            System.out.println("�뱣֤Ŀ¼��ʽ��ȷ����/��ͷ��β");
        }
    }

    /**
     * �Ͽ���Զ�̷�����������
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
            // ��д·�����ļ�����
            String localDir = "";
            String remoteDir = "";
            String remoteFileName = "";
            String localFileName = "";
            myFtp.connect("192.168.*.*", 21, "inspurftp", "*************");
            myFtp.download("E:/", "/���Բ��Բ���/", "��װ˵��.rtf");
//			myFtp.listRemoteFiles("/abcdef.");
            myFtp.disconnect();
        } catch (IOException e) {
            System.out.println("����FTP����" + e.getMessage());
        }
    }
}
