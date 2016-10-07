package server;

public class LockFile implements Comparable<LockFile> {
    private String serverFileName;
    private int lockClientIndex;

    public LockFile(String serverFileName, int lockClientIndex) {
        this.serverFileName = serverFileName;
        this.lockClientIndex = lockClientIndex;
    }

    LockFile(String lockFileName) {
        this.serverFileName = lockFileName.substring(0, lockFileName.indexOf('_'));
        this.lockClientIndex = Integer.parseInt(lockFileName.split("_")[1]);
    }

    public int getLockClientIndex() {
        return lockClientIndex;
    }

    public LockFile setLockClientIndex(int lockClientIndex) {
        this.lockClientIndex = lockClientIndex;
        return this;
    }

    String getLockFileName() {
        return serverFileName + "_" + lockClientIndex + "_" + ".lock";
    }

    public String getServerFileName() {
        return serverFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockFile lockFile = (LockFile) o;
        return lockClientIndex == lockFile.lockClientIndex && serverFileName.equals(lockFile.serverFileName);
    }

    @Override
    public int compareTo(LockFile o) {
        return Integer.compare(lockClientIndex, o.lockClientIndex);
    }

    @Override
    public String toString() {
        return getLockFileName();
    }

    static boolean isLockFile(String fileName) {
        return fileName.endsWith(".lock");
    }

    public void decrementClientLockIndex() {
        lockClientIndex--;
    }
}
